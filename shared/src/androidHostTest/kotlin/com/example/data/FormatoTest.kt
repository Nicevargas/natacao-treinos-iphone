package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A formatação passou a ser escrita à mão, porque o String.format com Locale só
 * existe no Android. Estes testes são a prova de que a saída continua a mesma
 * que aparecia nas telas e nos cartões.
 */
class FormatoTest {

    @Test
    fun `zeros a esquerda como nas datas e no cronometro`() {
        assertEquals("07", Formato.comZeros(7, 2))
        assertEquals("2026", Formato.comZeros(2026, 4))
        assertEquals("00", Formato.comZeros(0, 2))
        // Já vem maior que o pedido: não corta.
        assertEquals("125", Formato.comZeros(125, 2))
    }

    @Test
    fun `milhar com ponto, como se escreve aqui`() {
        assertEquals("0", Formato.milhar(0))
        assertEquals("940", Formato.milhar(940))
        assertEquals("1.940", Formato.milhar(1940))
        assertEquals("12.500", Formato.milhar(12500))
        assertEquals("1.234.567", Formato.milhar(1234567))
    }

    @Test
    fun `decimal com virgula e uma casa`() {
        assertEquals("8,5", Formato.decimal(8.46))
        assertEquals("8,0", Formato.decimal(8.0))
        assertEquals("0,0", Formato.decimal(0.04))
        assertEquals("10,0", Formato.decimal(9.99))
        assertEquals("-2,5", Formato.decimal(-2.45))
    }

    @Test
    fun `decimal sem casas arredonda para inteiro`() {
        assertEquals("9", Formato.decimal(8.6, casas = 0))
        assertEquals("8", Formato.decimal(8.4, casas = 0))
    }
}
