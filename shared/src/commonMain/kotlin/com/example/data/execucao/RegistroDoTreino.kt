package com.example.data.execucao

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.data.compartilhar.ResumoDoTreino
import com.example.model.TrainingLevel

/** Linha de public.treinos_realizados. user_id e id vêm do banco. */
@Serializable
data class TreinoRealizadoDto(
    @SerialName("id") val id: String? = null,
    @SerialName("workout_id") val workoutId: String? = null,
    @SerialName("treino_ciclo_id") val treinoCicloId: String? = null,
    @SerialName("titulo") val titulo: String,
    @SerialName("foco") val foco: String? = null,
    @SerialName("nivel") val nivel: String,
    @SerialName("data_treino") val dataTreino: String,
    @SerialName("metros_planejados") val metrosPlanejados: Int,
    @SerialName("metros_feitos") val metrosFeitos: Int,
    @SerialName("series_planejadas") val seriesPlanejadas: Int,
    @SerialName("series_feitas") val seriesFeitas: Int,
    @SerialName("duracao_segundos") val duracaoSegundos: Int,
    @SerialName("intensidade") val intensidade: Int? = null,
    @SerialName("complexidade") val complexidade: Int? = null,
    @SerialName("observacao") val observacao: String? = null,
    // Treino de um plano: qual plano, semana e número do treino.
    @SerialName("plano_id") val planoId: String? = null,
    @SerialName("plano_semana") val planoSemana: Int? = null,
    @SerialName("plano_treino") val planoTreino: Int? = null,
    // Só na leitura; no registro o banco preenche.
    @SerialName("created_at") val criadoEm: String? = null
)

object RegistroDoTreino {

    private val UUID = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    fun montar(
        roteiro: RoteiroDeTreino,
        progresso: ProgressoExecucao,
        duracaoSegundos: Long,
        intensidade: Int?,
        complexidade: Int?,
        observacao: String,
        dataIso: String
    ): TreinoRealizadoDto {
        val treino = roteiro.workout
        return TreinoRealizadoDto(
            // As chaves estrangeiras só aceitam ids que existem no banco: sugestão
            // do ciclo antigo ("ciclo_d14_...") ou do Método NC ("nc_d01_..."), ou
            // treino de Meus treinos (uuid). O treino de exemplo embarcado não é nenhum.
            workoutId = treino.id.takeIf { !treino.isSuggestion && UUID.matches(it) },
            treinoCicloId = treino.id.takeIf { treino.isSuggestion && (it.startsWith("ciclo_") || it.startsWith("nc_")) },
            titulo = treino.title.take(200),
            foco = treino.focus,
            nivel = treino.level.name,
            dataTreino = dataIso,
            metrosPlanejados = roteiro.metrosTotais,
            metrosFeitos = roteiro.metrosFeitos(progresso).coerceIn(0, roteiro.metrosTotais),
            seriesPlanejadas = roteiro.seriesTotais,
            seriesFeitas = roteiro.seriesFeitas(progresso),
            duracaoSegundos = duracaoSegundos.coerceIn(0L, 86_400L).toInt(),
            intensidade = intensidade?.coerceIn(0, 10),
            complexidade = complexidade?.coerceIn(0, 10),
            observacao = observacao.trim().take(500).ifEmpty { null },
            planoId = treino.plano?.planoId,
            planoSemana = treino.plano?.semana,
            planoTreino = treino.plano?.treino
        )
    }

    fun resumo(registro: TreinoRealizadoDto, roteiro: RoteiroDeTreino): ResumoDoTreino {
        val treino = roteiro.workout
        return ResumoDoTreino(
            titulo = registro.titulo,
            foco = registro.foco,
            nivel = TrainingLevel.entries.firstOrNull { it.name == registro.nivel } ?: treino.level,
            dataIso = registro.dataTreino,
            metrosFeitos = registro.metrosFeitos,
            metrosPlanejados = registro.metrosPlanejados,
            seriesFeitas = registro.seriesFeitas,
            seriesPlanejadas = registro.seriesPlanejadas,
            duracaoSegundos = registro.duracaoSegundos.toLong(),
            intensidade = registro.intensidade,
            complexidade = registro.complexidade,
            cicloDia = treino.cycleDay,
            doCarrossel = treino.isSuggestion
        )
    }
}
