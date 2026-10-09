package com.example

import com.example.data.ranking.MotivoDaDenuncia
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Os motivos do app têm que ser os que o banco aceita; senão a denúncia vira "outro". */
class DenunciaDoRankingTest {

    private val sql = File("../supabase/migrations/20261009000001_denuncia_no_ranking.sql").readText()

    @Test
    fun `cada motivo do app existe no banco`() {
        MotivoDaDenuncia.entries.forEach { motivo ->
            assertTrue("O banco não aceita o motivo ${motivo.chave}", sql.contains("'${motivo.chave}'"))
        }
    }

    @Test
    fun `as chaves dos motivos sao unicas`() {
        assertEquals(MotivoDaDenuncia.entries.size, MotivoDaDenuncia.entries.map { it.chave }.toSet().size)
    }

    @Test
    fun `quem denuncia deixa de ver o nome no ranking`() {
        assertTrue(sql.contains("d.denunciante = auth.uid() AND d.denunciado = p.id"))
    }
}
