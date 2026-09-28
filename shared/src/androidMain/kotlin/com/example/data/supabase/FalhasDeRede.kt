package com.example.data.supabase

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.io.IOException

/**
 * Nem toda falha de rede é igual: "o servidor demorou" e "você está sem internet"
 * viram mensagens diferentes na tela. Cada biblioteca de rede tem os seus tipos de
 * exceção, então o resto do app pergunta aqui em vez de conhecê-los.
 */

/** O servidor até foi alcançado, mas não respondeu a tempo. */
internal fun Throwable.ehDemora(): Boolean =
    this is HttpRequestTimeoutException || this is SocketTimeoutException || this is ConnectTimeoutException

/** Não deu para falar com o servidor: sem internet, DNS, recusa de conexão. */
internal fun Throwable.ehSemRede(): Boolean = this is IOException
