package com.example.data.parq

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.data.Resultado
import com.example.data.ciclo.DataCivil
import com.example.data.supabase.chamarApi

/** Linha de public.parq_respostas. id, user_id e as datas vêm do banco. */
@Serializable
data class ParQRespostaDto(
    @SerialName("id") val id: String? = null,
    @SerialName("versao") val versao: String,
    @SerialName("respostas") val respostas: List<Boolean>,
    @SerialName("algum_sim") val algumSim: Boolean,
    @SerialName("declaracao_aceita") val declaracaoAceita: Boolean,
    @SerialName("termo_aceito") val termoAceito: Boolean,
    // Só na leitura: o gatilho do banco preenche com a data de Brasília.
    @SerialName("respondido_no_dia") val respondidoNoDia: String? = null
) {
    fun dia(): Long? = respondidoNoDia?.let { runCatching { DataCivil.deIso(it) }.getOrNull() }
}

object ParQRepository {

    /** A resposta mais recente de quem está logado, ou nulo se nunca respondeu. */
    suspend fun ultima(): Resultado<ParQRespostaDto?> =
        chamarApi({ it.ultimoParQ() }) { lista -> Resultado.Ok(lista?.firstOrNull()) }

    suspend fun registrar(respostas: List<Boolean>, declaracao: Boolean, termo: Boolean): Resultado<ParQRespostaDto> {
        val algumSim = respostas.any { it }
        val corpo = ParQRespostaDto(
            versao = ParQ.VERSAO,
            respostas = respostas,
            algumSim = algumSim,
            declaracaoAceita = declaracao,
            // Sem SIM, o termo nem aparece: não registrar um aceite que não houve.
            termoAceito = algumSim && termo
        )
        return chamarApi({ it.registrarParQ(corpo) }) { lista ->
            lista?.firstOrNull()?.let { Resultado.Ok(it) }
                ?: Resultado.Falha("Suas respostas não foram salvas. Tente de novo.")
        }
    }
}
