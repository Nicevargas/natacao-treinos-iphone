package com.example.data

import platform.Foundation.NSUserDefaults

/**
 * A versão iPhone do [Guardados]: NSUserDefaults, o equivalente do
 * SharedPreferences. Cada "arquivo" do Android vira um domínio separado, com o
 * mesmo nome, para limparTudo() apagar só o que é dele.
 */
private class GuardadosDoIos(private val arquivo: String) : Guardados {

    private val padroes = NSUserDefaults(suiteName = arquivo)

    override fun texto(chave: String): String? = padroes.stringForKey(chave)

    override fun salvarTexto(chave: String, valor: String) {
        padroes.setObject(valor, forKey = chave)
    }

    override fun numero(chave: String, padrao: Long): Long =
        if (padroes.objectForKey(chave) == null) padrao else padroes.integerForKey(chave)

    override fun salvarNumero(chave: String, valor: Long) {
        padroes.setInteger(valor, forKey = chave)
    }

    override fun remover(chave: String) {
        padroes.removeObjectForKey(chave)
    }

    override fun limparTudo() {
        padroes.removePersistentDomainForName(arquivo)
    }
}

fun guardadosDoIos(arquivo: String): Guardados = GuardadosDoIos(arquivo)
