package com.example.data

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Números em português, sem depender do Java.
 *
 * O String.format com Locale só existe no Android; o iPhone precisa da mesma
 * saída ("1.940 m", "8,5"), então as poucas formatações do app moram aqui.
 */
object Formato {

    /** 7 com 2 dígitos vira "07". Usado em datas e cronômetros. */
    fun comZeros(valor: Int, digitos: Int): String {
        val sinal = if (valor < 0) "-" else ""
        return sinal + abs(valor).toString().padStart(digitos, '0')
    }

    /** 1940 vira "1.940": ponto a cada três casas, como se escreve aqui. */
    fun milhar(valor: Int): String {
        val sinal = if (valor < 0) "-" else ""
        return sinal + abs(valor).toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
    }

    /** 8.46 vira "8,5": arredonda e troca o ponto pela vírgula. */
    fun decimal(valor: Double, casas: Int = 1): String {
        var fator = 1L
        repeat(casas) { fator *= 10 }
        val arredondado = (abs(valor) * fator).roundToLong()
        val inteiro = arredondado / fator
        val resto = arredondado % fator
        val sinal = if (valor < 0 && arredondado != 0L) "-" else ""
        if (casas == 0) return sinal + inteiro.toString()
        return sinal + inteiro.toString() + "," + resto.toString().padStart(casas, '0')
    }
}
