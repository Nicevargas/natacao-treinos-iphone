package com.example.data.compartilhar

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * O link que abre o app agora chega como texto, e não mais como Intent do
 * Android: é assim que o iPhone também vai entregar. Estes testes provam que os
 * links de verdade continuam caindo no treino certo.
 */
class LinkDeTreinoTest {

    @After
    fun limpar() {
        LinkDeTreino.consumido()
    }

    @Test
    fun `link do app abre o treino`() {
        LinkDeTreino.receber("natacaocriativa://treino/a1b2c3d4e5")

        assertEquals("a1b2c3d4e5", LinkDeTreino.pendente.value)
    }

    @Test
    fun `link da pagina, aberto pelo navegador, tambem abre`() {
        LinkDeTreino.receber(CodigoDoTreino.link("a1b2c3d4e5"))

        assertEquals("a1b2c3d4e5", LinkDeTreino.pendente.value)
    }

    @Test
    fun `app aberto pelo icone, sem link, nao inventa treino`() {
        LinkDeTreino.receber(null)

        assertNull(LinkDeTreino.pendente.value)
    }

    @Test
    fun `link de outro assunto nao apaga o treino que ja estava esperando`() {
        LinkDeTreino.receber("natacaocriativa://treino/a1b2c3d4e5")

        LinkDeTreino.receber("natacaocriativa://auth?code=xyz")

        assertEquals("a1b2c3d4e5", LinkDeTreino.pendente.value)
    }

    @Test
    fun `depois de abrir, o treino sai da fila`() {
        LinkDeTreino.receber("natacaocriativa://treino/a1b2c3d4e5")

        LinkDeTreino.consumido()

        assertNull(LinkDeTreino.pendente.value)
    }
}
