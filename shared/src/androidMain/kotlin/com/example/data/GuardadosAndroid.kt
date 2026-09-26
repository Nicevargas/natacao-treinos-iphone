package com.example.data

import android.content.Context

/**
 * A versão Android do [Guardados]: SharedPreferences, o mesmo lugar de sempre.
 *
 * O nome do arquivo é o mesmo de antes de propósito. Quem já tem o app instalado
 * continua logado depois de atualizar, em vez de cair na tela de entrar.
 */
private class GuardadosDoAndroid(context: Context, arquivo: String) : Guardados {

    private val prefs = context.applicationContext.getSharedPreferences(arquivo, Context.MODE_PRIVATE)

    override fun texto(chave: String): String? = prefs.getString(chave, null)

    override fun salvarTexto(chave: String, valor: String) {
        prefs.edit().putString(chave, valor).apply()
    }

    override fun numero(chave: String, padrao: Long): Long = prefs.getLong(chave, padrao)

    override fun salvarNumero(chave: String, valor: Long) {
        prefs.edit().putLong(chave, valor).apply()
    }

    override fun remover(chave: String) {
        prefs.edit().remove(chave).apply()
    }

    override fun limparTudo() {
        prefs.edit().clear().apply()
    }
}

fun guardadosDe(context: Context, arquivo: String): Guardados = GuardadosDoAndroid(context, arquivo)
