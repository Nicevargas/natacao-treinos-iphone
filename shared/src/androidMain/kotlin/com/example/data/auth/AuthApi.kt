package com.example.data.auth

import com.example.data.supabase.Resposta
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/** Supabase Auth (GoTrue) pela mesma URL do projeto. */
class AuthApi(private val http: HttpClient) {

    suspend fun signUp(body: SignUpBody): Resposta<SessionDto> =
        pedir(HttpMethod.Post, "auth/v1/signup", corpo = body)

    suspend fun signIn(body: PasswordGrantBody): Resposta<SessionDto> =
        pedir(HttpMethod.Post, "auth/v1/token", mapOf("grant_type" to "password"), corpo = body)

    /** Antes era síncrona, por causa do Authenticator do OkHttp; agora é suspensa como as outras. */
    suspend fun refresh(body: RefreshGrantBody): Resposta<SessionDto> =
        pedir(HttpMethod.Post, "auth/v1/token", mapOf("grant_type" to "refresh_token"), corpo = body)

    suspend fun signOut(): Resposta<Unit> =
        pedir(HttpMethod.Post, "auth/v1/logout")

    // ---- Esqueci minha senha ----

    /** Manda o e-mail de recuperação. Responde sucesso mesmo se o e-mail não tiver conta. */
    suspend fun recover(body: RecoverBody): Resposta<Unit> =
        pedir(HttpMethod.Post, "auth/v1/recover", corpo = body)

    /** Troca o código do e-mail por uma sessão de recuperação. */
    suspend fun verify(body: VerifyOtpBody): Resposta<SessionDto> =
        pedir(HttpMethod.Post, "auth/v1/verify", corpo = body)

    /** Grava a senha nova; o Authorization é o da sessão de recuperação, não o salvo no aparelho. */
    suspend fun updateUser(bearer: String, body: UpdatePasswordBody): Resposta<AuthUserDto> =
        pedir(HttpMethod.Put, "auth/v1/user", corpo = body, autorizacao = bearer)

    private suspend inline fun <reified T> pedir(
        metodo: HttpMethod,
        caminho: String,
        consulta: Map<String, String> = emptyMap(),
        corpo: Any? = null,
        autorizacao: String? = null
    ): Resposta<T> {
        val resposta = http.request(caminho) {
            method = metodo
            url { consulta.forEach { (nome, valor) -> parameters.append(nome, valor) } }
            if (autorizacao != null) header(HttpHeaders.Authorization, autorizacao)
            if (corpo != null) {
                contentType(ContentType.Application.Json)
                setBody(corpo)
            }
        }
        if (!resposta.status.isSuccess()) {
            return Resposta(resposta.status.value, null, resposta.bodyAsText())
        }
        val valor: T = if (T::class == Unit::class) Unit as T else resposta.body()
        return Resposta(resposta.status.value, valor, null)
    }
}
