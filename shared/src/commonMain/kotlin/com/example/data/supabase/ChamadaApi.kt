package com.example.data.supabase

import kotlinx.coroutines.IO
import com.example.data.Registro
import com.example.data.Resultado
import com.example.data.auth.MensagensAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Faz a chamada no Supabase e traduz falha de rede, HTTP e RLS em mensagem para a tela. */
internal suspend fun <T, R> chamarApi(
    chamada: suspend (SupabaseApi) -> Resposta<T>,
    sucesso: (T?) -> Resultado<R>
): Resultado<R> = withContext(Dispatchers.IO) {
    val api = SupabaseClient.api ?: return@withContext Resultado.Falha(MensagensAuth.SEM_CONFIGURACAO)
    try {
        val resposta = chamada(api)
        if (resposta.sucesso) {
            sucesso(resposta.corpo)
        } else {
            Resultado.Falha(MensagensAuth.deErroDaApi(resposta.codigo, resposta.erro))
        }
    } catch (e: Exception) {
        when {
            e.ehDemora() -> Resultado.Falha(MensagensAuth.DEMOROU)
            e.ehSemRede() -> Resultado.Falha(MensagensAuth.SEM_REDE)
            else -> {
                Registro.erro("ChamadaApi", "Falha inesperada na chamada ao Supabase", e)
                Resultado.Falha("Não foi possível concluir. Tente de novo.")
            }
        }
    }
}
