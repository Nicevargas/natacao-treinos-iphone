package com.example.data.auth

import com.example.data.Resultado
import com.example.data.supabase.JsonDoApp
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Esqueci minha senha contra um Supabase Auth de mentira: confere o que vai e o
 * que volta. O servidor falso é o do próprio Ktor, que funciona também no iPhone
 * (o MockWebServer só existe no Android).
 */
class RecuperacaoDeSenhaTest {

    private val pedidos = mutableListOf<HttpRequestData>()

    /** Um Supabase de mentira que sempre responde a mesma coisa. */
    private fun recuperacaoQueRecebe(
        status: HttpStatusCode = HttpStatusCode.OK,
        corpo: String = "{}",
        tempoDeResposta: Long = 0,
        semRede: Boolean = false
    ): RecuperacaoDeSenha {
        val motor = MockEngine { pedido ->
            pedidos += pedido
            if (semRede) throw IOException("sem rede")
            if (tempoDeResposta > 0) delay(tempoDeResposta)
            if (status.value >= 400) {
                respondError(status, corpo, headersOf("Content-Type", "application/json"))
            } else {
                respond(corpo, status, headersOf("Content-Type", "application/json"))
            }
        }
        val http = HttpClient(motor) {
            expectSuccess = false
            install(ContentNegotiation) { json(JsonDoApp) }
            install(HttpTimeout) { requestTimeoutMillis = 1_000 }
            defaultRequest { url("https://projeto.supabase.co/") }
        }
        return RecuperacaoDeSenha(AuthApi(http), agoraSegundos = { 1_000 })
    }

    private fun ultimoPedido() = pedidos.last()

    private fun corpoEnviado(): String = (ultimoPedido().body as TextContent).text

    private fun caminho(): String = ultimoPedido().url.encodedPath

    @Test
    fun `envia o pedido de codigo com o e-mail normalizado`() = runTest {
        val r = recuperacaoQueRecebe().enviarCodigo("  Ana@Exemplo.com ")

        assertEquals(Resultado.Ok(Unit), r)
        assertEquals("POST", ultimoPedido().method.value)
        assertEquals("/auth/v1/recover", caminho())
        assertEquals("""{"email":"ana@exemplo.com"}""", corpoEnviado())
    }

    @Test
    fun `limite de envio vira mensagem`() = runTest {
        val recuperacao = recuperacaoQueRecebe(
            HttpStatusCode.TooManyRequests,
            """{"error_code":"over_email_send_rate_limit"}"""
        )

        val r = recuperacao.enviarCodigo("ana@exemplo.com")

        assertEquals(Resultado.Falha("Muitas tentativas. Espere alguns minutos e tente de novo."), r)
    }

    @Test
    fun `codigo valido vira sessao de recuperacao`() = runTest {
        val recuperacao = recuperacaoQueRecebe(
            corpo = """{"access_token":"tok-rec","refresh_token":"ref-rec","expires_in":3600,
                       "user":{"id":"u1","email":"ana@exemplo.com"}}"""
        )

        val r = recuperacao.verificarCodigo("ana@exemplo.com", " 123456 ")

        assertEquals(Resultado.Ok(Sessao("tok-rec", "ref-rec", 4_600, "u1", "ana@exemplo.com")), r)
        assertEquals("/auth/v1/verify", caminho())
        assertEquals("""{"type":"recovery","email":"ana@exemplo.com","token":"123456"}""", corpoEnviado())
    }

    @Test
    fun `codigo vencido vira mensagem`() = runTest {
        val recuperacao = recuperacaoQueRecebe(
            HttpStatusCode.Forbidden,
            """{"code":403,"error_code":"otp_expired","msg":"Token has expired or is invalid"}"""
        )

        val r = recuperacao.verificarCodigo("ana@exemplo.com", "000000")

        assertEquals(Resultado.Falha("Código inválido ou vencido. Confira os números ou peça um novo."), r)
    }

    @Test
    fun `troca a senha com o token da recuperacao`() = runTest {
        val recuperacao = recuperacaoQueRecebe(corpo = """{"id":"u1","email":"ana@exemplo.com"}""")
        val sessao = Sessao("tok-rec", "ref-rec", 4_600, "u1", "ana@exemplo.com")

        val r = recuperacao.trocarSenha(sessao, "senhaNova1")

        assertEquals(Resultado.Ok(Unit), r)
        assertEquals("PUT", ultimoPedido().method.value)
        assertEquals("/auth/v1/user", caminho())
        assertEquals("Bearer tok-rec", ultimoPedido().headers["Authorization"])
        assertEquals("""{"password":"senhaNova1"}""", corpoEnviado())
    }

    @Test
    fun `senha igual a antiga e recusada com mensagem propria`() = runTest {
        val recuperacao = recuperacaoQueRecebe(
            HttpStatusCode.UnprocessableEntity,
            """{"error_code":"same_password","msg":"New password should be different from the old password."}"""
        )

        val r = recuperacao.trocarSenha(Sessao("t", "r", 0, "u1", "a@b.com"), "mesmaSenha")

        assertEquals(Resultado.Falha("A nova senha precisa ser diferente da anterior."), r)
    }

    @Test
    fun `servidor que nao responde a tempo vira demora, nao falta de internet`() = runTest {
        val recuperacao = recuperacaoQueRecebe(tempoDeResposta = 5_000)

        val r = recuperacao.enviarCodigo("ana@exemplo.com")

        assertEquals(Resultado.Falha(MensagensAuth.DEMOROU), r)
    }

    @Test
    fun `sem rede vira mensagem e nao estoura`() = runTest {
        val recuperacao = recuperacaoQueRecebe(semRede = true)

        val r = recuperacao.enviarCodigo("ana@exemplo.com")

        assertTrue(r is Resultado.Falha)
        assertEquals(MensagensAuth.SEM_REDE, (r as Resultado.Falha).mensagem)
    }
}
