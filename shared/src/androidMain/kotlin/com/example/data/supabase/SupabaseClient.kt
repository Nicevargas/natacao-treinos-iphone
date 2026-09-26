package com.example.data.supabase

import android.util.Log
import com.example.data.auth.AuthApi
import com.example.data.auth.RefreshGrantBody
import com.example.data.auth.SessaoStore
import com.example.data.auth.paraSessao
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Qual Authorization vai em cada chamada.
 *
 * Login, cadastro, renovação e recuperação de senha vão com a chave pública; o
 * resto vai com o token de quem está logado, que é o que o RLS enxerga como
 * auth.uid(). Se quem chamou já pôs um Authorization (troca de senha com a
 * sessão de recuperação), ele é mantido.
 */
internal fun escolherAuthorization(
    caminho: String,
    explicito: String?,
    tokenDaSessao: String?,
    chaveAnon: String
): String {
    if (explicito != null) return explicito
    val usaSessao = !caminho.contains("/auth/v1/") || caminho.endsWith("/logout")
    return "Bearer ${tokenDaSessao?.takeIf { usaSessao } ?: chaveAnon}"
}

enum class SupabaseStatus {
    CONNECTED,
    CONFIG_NEEDED,
    CONNECTING,
    OFFLINE_LOCAL
}

object SupabaseClient {
    private const val TAG = "SupabaseClient"

    val supabaseUrl: String = Configuracao.SUPABASE_URL.trim()
    val supabaseAnonKey: String = Configuracao.SUPABASE_ANON_KEY.trim()

    val isConfigured: Boolean by lazy {
        supabaseUrl.isNotBlank() &&
                !supabaseUrl.contains("placeholder", ignoreCase = true) &&
                supabaseAnonKey.isNotBlank() &&
                !supabaseAnonKey.contains("placeholder", ignoreCase = true)
    }

    /** Preenchida por AuthRepository.init. Sem sessão, as chamadas vão com a chave pública. */
    @Volatile
    var sessaoStore: SessaoStore? = null

    private val baseUrl: String
        get() = if (supabaseUrl.endsWith("/")) supabaseUrl else "$supabaseUrl/"

    /** Uma renovação de cada vez: dez chamadas levando 401 juntas não viram dez refreshes. */
    private val renovando = Mutex()

    private fun montar(comRenovacao: Boolean): HttpClient = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) { json(JsonDoApp) }
        install(Logging) { level = LogLevel.INFO }
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            // Cadastro e "esqueci minha senha" só respondem depois que o Supabase
            // manda o e-mail pelo SMTP, e isso pode levar vários segundos.
            requestTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }
        defaultRequest {
            url(baseUrl)
            header("apikey", supabaseAnonKey)
            accept(ContentType.Application.Json)
            val explicito = headers[HttpHeaders.Authorization]
            headers.remove(HttpHeaders.Authorization)
            header(
                HttpHeaders.Authorization,
                escolherAuthorization(
                    caminho = caminhoDe(url.encodedPathSegments),
                    explicito = explicito,
                    tokenDaSessao = sessaoStore?.atual()?.accessToken,
                    chaveAnon = supabaseAnonKey
                )
            )
        }
    }.also { cliente ->
        if (comRenovacao) instalarRenovacao(cliente)
    }

    /**
     * O token de acesso vence em ~1h. Num 401, troca pelo refresh token e repete a
     * chamada uma vez. Refresh recusado encerra a sessão (o app volta para o
     * login); falta de rede não encerra, só deixa a chamada falhar.
     */
    private fun instalarRenovacao(cliente: HttpClient) {
        cliente.plugin(HttpSend).intercept { pedido ->
            val chamada = execute(pedido)
            val caminho = caminhoDe(pedido.url.encodedPathSegments)
            if (chamada.response.status != HttpStatusCode.Unauthorized || caminho.contains("/auth/v1/")) {
                return@intercept chamada
            }
            val store = sessaoStore ?: return@intercept chamada
            val tokenUsado = pedido.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
            val novoToken = renovar(store, tokenUsado) ?: return@intercept chamada

            pedido.headers.remove(HttpHeaders.Authorization)
            pedido.headers.append(HttpHeaders.Authorization, "Bearer $novoToken")
            execute(pedido)
        }
    }

    private suspend fun renovar(store: SessaoStore, tokenUsado: String?): String? = renovando.withLock {
        val atual = store.atual() ?: return null
        // Outra chamada já renovou enquanto esta esperava na fila.
        if (atual.accessToken != tokenUsado) return atual.accessToken

        val api = authApi ?: return null
        return try {
            val r = api.refresh(RefreshGrantBody(atual.refreshToken))
            val nova = r.corpo?.paraSessao(agoraEmSegundos())
            when {
                r.sucesso && nova != null -> {
                    store.salvar(nova)
                    nova.accessToken
                }
                r.codigo in 400..499 -> {
                    Log.w(TAG, "Refresh token recusado (HTTP ${r.codigo}); encerrando a sessão")
                    store.limpar()
                    null
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Sem rede para renovar a sessão", e)
            null
        }
    }

    /** "/auth/v1/token" a partir das partes do caminho, como escolherAuthorization espera. */
    private fun caminhoDe(partes: List<String>): String =
        partes.filter { it.isNotEmpty() }.joinToString(separator = "/", prefix = "/")

    private fun agoraEmSegundos(): Long = System.currentTimeMillis() / 1000

    // Cliente sem renovação: usado pela própria renovação e pelas telas de conta.
    private val clienteBase: HttpClient? by lazy { if (isConfigured) montar(comRenovacao = false) else avisar() }
    private val clienteComRenovacao: HttpClient? by lazy { if (isConfigured) montar(comRenovacao = true) else avisar() }

    private fun avisar(): HttpClient? {
        Log.w(TAG, "Supabase credentials are not configured. Falling back to local offline mode.")
        return null
    }

    val api: SupabaseApi? by lazy { clienteComRenovacao?.let { SupabaseApi(it) } }

    val authApi: AuthApi? by lazy { clienteBase?.let { AuthApi(it) } }
}
