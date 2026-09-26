package com.example.data

/**
 * Anotações de diagnóstico do app: token recusado, cadastro sem resposta, falha
 * inesperada. Não aparecem para quem usa; servem para entender um problema
 * depois.
 *
 * O android.util.Log não existe no iPhone, então o app escreve aqui e cada
 * sistema liga a sua saída ao abrir. Sem ninguém ligado, não escreve nada — é
 * o que acontece nos testes.
 */
object Registro {

    fun interface Saida {
        fun escrever(grave: Boolean, marca: String, mensagem: String, causa: Throwable?)
    }

    @Volatile
    var saida: Saida = Saida { _, _, _, _ -> }

    fun aviso(marca: String, mensagem: String, causa: Throwable? = null) {
        saida.escrever(false, marca, mensagem, causa)
    }

    fun erro(marca: String, mensagem: String, causa: Throwable? = null) {
        saida.escrever(true, marca, mensagem, causa)
    }
}
