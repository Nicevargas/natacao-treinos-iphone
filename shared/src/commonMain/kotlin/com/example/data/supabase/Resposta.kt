package com.example.data.supabase

/**
 * O que voltou de uma chamada ao Supabase, sem depender da biblioteca de rede.
 * Substitui o Response do Retrofit, que só existia no Android.
 */
class Resposta<T>(
    val codigo: Int,
    val corpo: T?,
    /** Corpo da resposta quando deu errado, cru, para as mensagens em português. */
    val erro: String?
) {
    val sucesso: Boolean get() = codigo in 200..299
}
