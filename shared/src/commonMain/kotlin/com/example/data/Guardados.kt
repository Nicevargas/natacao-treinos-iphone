package com.example.data

/**
 * O pouco que o app guarda no próprio aparelho: a sessão de quem está logado, o
 * dia do lembrete e a data do último PAR-Q. Nada disso é histórico de treino,
 * que mora no Supabase.
 *
 * Cada sistema guarda à sua maneira (SharedPreferences no Android,
 * NSUserDefaults no iPhone), então o resto do app fala com esta porta e não
 * conhece nenhum dos dois.
 */
interface Guardados {

    fun texto(chave: String): String?

    fun salvarTexto(chave: String, valor: String)

    fun numero(chave: String, padrao: Long): Long

    fun salvarNumero(chave: String, valor: Long)

    fun remover(chave: String)

    /** Apaga tudo deste arquivo. Usado ao sair da conta. */
    fun limparTudo()
}
