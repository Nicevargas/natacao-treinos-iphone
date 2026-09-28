package com.example.data.ranking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.data.Resultado
import com.example.data.supabase.chamarApi

/** Uma linha de public.ranking_nadadores(). Só nome escolhido e números: nada sensível. */
@Serializable
data class LinhaDoRanking(
    @SerialName("posicao") val posicao: Int,
    @SerialName("nome") val nome: String,
    @SerialName("pontos") val pontos: Int,
    @SerialName("metros") val metros: Int,
    @SerialName("treinos") val treinos: Int,
    @SerialName("semanas") val semanas: Int,
    @SerialName("sou_eu") val souEu: Boolean
)

/** Parâmetros da função. Nulo fica de fora do JSON e vale o padrão do banco (sem filtro). */
@Serializable
data class ParametrosDoRanking(
    @SerialName("p_periodo") val periodo: String,
    @SerialName("p_faixa") val faixa: String? = null,
    @SerialName("p_sexo") val sexo: String? = null,
    @SerialName("p_horario") val horario: String? = null,
    @SerialName("p_cidade") val cidade: String? = null,
    @SerialName("p_local") val local: String? = null,
    @SerialName("p_limite") val limite: Int = 100
)

/** Os dados do ranking no perfil, lidos e gravados só pela própria pessoa (RLS). */
@Serializable
data class ParticipacaoDto(
    @SerialName("ranking_publico") val publico: Boolean = false,
    @SerialName("ranking_nome") val nome: String? = null,
    @SerialName("ano_nascimento") val anoNascimento: Int? = null,
    @SerialName("sexo") val sexo: String? = null,
    @SerialName("cidade") val cidade: String? = null,
    @SerialName("local_treino") val local: String? = null
)

enum class PeriodoDoRanking(val chave: String, val rotulo: String) {
    SEMANA("semana", "Semana"), MES("mes", "Mês"), ANO("ano", "Ano"), TUDO("tudo", "Geral")
}

object OpcoesDoRanking {
    val FAIXAS = listOf("ate-17" to "Até 17", "18-29" to "18–29", "30-39" to "30–39", "40-49" to "40–49", "50-59" to "50–59", "60+" to "60+")
    val SEXOS = listOf("F" to "Feminino", "M" to "Masculino", "OUTRO" to "Outro")
    val HORARIOS = listOf("manha" to "Manhã", "tarde" to "Tarde", "noite" to "Noite")

    /** "Ana Maria dos Santos" -> "Ana S.": o nome que o app sugere para o ranking. */
    fun nomeSugerido(nomeCompleto: String): String {
        val partes = nomeCompleto.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            partes.isEmpty() -> ""
            partes.size == 1 -> partes[0].take(40)
            else -> "${partes.first()} ${partes.last().first().uppercaseChar()}.".take(40)
        }
    }
}

object RankingRepository {

    suspend fun participacao(userId: String): Resultado<ParticipacaoDto> =
        chamarApi({ it.lerParticipacaoNoRanking("eq.$userId") }) { lista ->
            Resultado.Ok(lista?.firstOrNull() ?: ParticipacaoDto())
        }

    suspend fun salvar(userId: String, participacao: ParticipacaoDto): Resultado<ParticipacaoDto> =
        chamarApi({ it.salvarParticipacaoNoRanking("eq.$userId", participacao) }) { lista ->
            lista?.firstOrNull()?.let { Resultado.Ok(it) }
                ?: Resultado.Falha("Seus dados do ranking não foram salvos. Tente de novo.")
        }

    suspend fun listar(parametros: ParametrosDoRanking): Resultado<List<LinhaDoRanking>> =
        chamarApi({ it.rankingNadadores(parametros) }) { lista -> Resultado.Ok(lista.orEmpty()) }
}
