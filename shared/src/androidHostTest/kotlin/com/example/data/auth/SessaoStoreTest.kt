package com.example.data.auth

import com.example.data.Guardados
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Um aparelho de mentira: guarda em memória o que o celular guardaria em disco. */
private class GuardadosDeMentira(inicial: Map<String, Any> = emptyMap()) : Guardados {
    val conteudo = inicial.toMutableMap()
    override fun texto(chave: String): String? = conteudo[chave] as? String
    override fun salvarTexto(chave: String, valor: String) { conteudo[chave] = valor }
    override fun numero(chave: String, padrao: Long): Long = conteudo[chave] as? Long ?: padrao
    override fun salvarNumero(chave: String, valor: Long) { conteudo[chave] = valor }
    override fun remover(chave: String) { conteudo.remove(chave) }
    override fun limparTudo() { conteudo.clear() }
}

/**
 * A sessão é o que mantém a pessoa logada entre um dia e outro. Estes testes
 * seguram as duas coisas que quebrariam isso sem ninguém perceber: mudar o nome
 * do arquivo e mudar o nome das chaves.
 */
class SessaoStoreTest {

    private val sessao = Sessao("tok", "ref", 4_600, "u1", "ana@exemplo.com")

    @Test
    fun `o nome do arquivo nao muda, senao todo mundo é deslogado na atualizacao`() {
        assertEquals("aquagenda_sessao", SessaoStore.ARQUIVO)
    }

    @Test
    fun `salvar e reabrir o app devolve a mesma sessao`() {
        val aparelho = GuardadosDeMentira()
        SessaoStore(aparelho).salvar(sessao)

        // Abrir de novo é um store novo lendo o mesmo aparelho.
        assertEquals(sessao, SessaoStore(aparelho).atual())
    }

    @Test
    fun `sessao gravada pela versao anterior continua valendo`() {
        // Exatamente as chaves que a versão com SharedPreferences gravava.
        val aparelho = GuardadosDeMentira(
            mapOf(
                "access_token" to "tok",
                "refresh_token" to "ref",
                "expira_em" to 4_600L,
                "user_id" to "u1",
                "email" to "ana@exemplo.com"
            )
        )

        assertEquals(sessao, SessaoStore(aparelho).atual())
    }

    @Test
    fun `sair apaga tudo e o app volta para a tela de entrar`() {
        val aparelho = GuardadosDeMentira()
        val store = SessaoStore(aparelho)
        store.salvar(sessao)

        store.limpar()

        assertNull(store.atual())
        assertTrue(aparelho.conteudo.isEmpty())
        assertNull(SessaoStore(aparelho).atual())
    }

    @Test
    fun `gravacao pela metade nao vira sessao quebrada`() {
        // Sem o refresh token não dá para renovar: melhor pedir login de novo.
        val aparelho = GuardadosDeMentira(mapOf("access_token" to "tok", "user_id" to "u1"))

        assertNull(SessaoStore(aparelho).atual())
    }

    @Test
    fun `a sessao nova fica visivel para quem acompanha o fluxo`() {
        val store = SessaoStore(GuardadosDeMentira())

        store.salvar(sessao)

        assertEquals(sessao, store.sessao.value)
    }
}
