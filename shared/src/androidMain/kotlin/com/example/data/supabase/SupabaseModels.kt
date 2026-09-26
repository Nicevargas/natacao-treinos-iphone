package com.example.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.model.CompletedSetRecord
import com.example.model.Corretivo
import com.example.model.PhaseStatus
import com.example.model.TrainingLevel
import com.example.model.Workout
import com.example.model.WorkoutPhase
import com.example.model.WorkoutSet

@Serializable
data class WorkoutDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("subtitle") val subtitle: String? = null,
    @SerialName("tag") val tag: String? = "Treino Principal",
    @SerialName("workout_date") val workoutDate: String? = null,
    @SerialName("total_distance_meters") val totalDistanceMeters: Int? = null,
    @SerialName("estimated_minutes") val estimatedMinutes: Int? = null,
    @SerialName("calories") val calories: Int? = null,
    @SerialName("level") val level: String? = "INTERMEDIARIO",
    @SerialName("is_completed") val isCompleted: Boolean? = false,
    @SerialName("phases") val phases: List<WorkoutPhaseDto>? = null,
    // Campos de public.treinos_sugeridos (o ciclo do carrossel).
    @SerialName("ciclo_dia") val cicloDia: Int? = null,
    @SerialName("bloco") val bloco: String? = null,
    @SerialName("foco") val foco: String? = null,
    @SerialName("motivational_tip") val motivationalTip: String? = null,
    @SerialName("is_suggestion") val isSuggestion: Boolean? = false,
    // Método NC (ciclo metodo-nc, desde 15/09/2026).
    @SerialName("objetivo") val objetivo: String? = null,
    @SerialName("zona") val zona: String? = null,
    @SerialName("ajuste") val ajuste: String? = null
)

@Serializable
data class WorkoutPhaseDto(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("distanceMeters") val distanceMeters: Int? = null,
    @SerialName("percentage") val percentage: Int? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("sets") val sets: List<WorkoutSetDto>? = null
)

@Serializable
data class WorkoutSetDto(
    @SerialName("id") val id: String? = null,
    @SerialName("repsDescription") val repsDescription: String? = null,
    @SerialName("stroke") val stroke: String? = null,
    @SerialName("interval") val interval: String? = null,
    @SerialName("intensity") val intensity: String? = null,
    @SerialName("restSeconds") val restSeconds: Int? = null,
    @SerialName("equipment") val equipment: String? = null,
    @SerialName("isDone") val isDone: Boolean? = false,
    @SerialName("serie") val serie: String? = null,
    @SerialName("details") val details: List<String>? = null,
    @SerialName("distanceMeters") val distanceMeters: Int? = null,
    @SerialName("zona") val zona: String? = null,
    @SerialName("pse") val pse: String? = null,
    @SerialName("corretivo") val corretivo: CorretivoDto? = null
)

@Serializable
data class CorretivoDto(
    @SerialName("nome") val nome: String? = null,
    @SerialName("objetivo") val objetivo: String? = null,
    @SerialName("dica") val dica: String? = null
)

/**
 * Corpo de INSERT/PATCH em public.workouts: só colunas da tabela. O WorkoutDto
 * de leitura traz campos da sugestão (is_suggestion, ciclo_dia...) que o
 * PostgREST recusaria. id e user_id vêm do banco (gen_random_uuid, auth.uid()).
 */
@Serializable
data class WorkoutWriteDto(
    @SerialName("title") val title: String,
    @SerialName("subtitle") val subtitle: String?,
    @SerialName("tag") val tag: String,
    @SerialName("workout_date") val workoutDate: String,
    @SerialName("total_distance_meters") val totalDistanceMeters: Int,
    @SerialName("estimated_minutes") val estimatedMinutes: Int,
    @SerialName("calories") val calories: Int,
    @SerialName("level") val level: String,
    @SerialName("phases") val phases: List<WorkoutPhaseDto>
)

/** Corpo do upsert do próprio perfil. O e-mail o banco copia da conta. */
@Serializable
data class ProfileWriteDto(
    @SerialName("id") val id: String,
    @SerialName("full_name") val fullName: String?,
    @SerialName("preferred_pool_meters") val preferredPoolMeters: Int,
    @SerialName("training_level") val trainingLevel: String
)

@Serializable
data class TreinosSugeridosParams(
    @SerialName("p_data") val data: String,
    @SerialName("p_level") val level: String?,
    // Só vai no modo águas abertas (null não é enviado): o banco sem a migração de
    // águas abertas continua respondendo o treino de piscina.
    @SerialName("p_modo") val modo: String? = null
)

@Serializable
data class SwimSetRecordDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("workout_id") val workoutId: String? = null,
    @SerialName("set_number") val setNumber: Int? = null,
    @SerialName("rep_description") val repDescription: String? = null,
    @SerialName("distance_meters") val distanceMeters: Int? = 100,
    @SerialName("time_formatted") val timeFormatted: String? = null,
    @SerialName("time_millis") val timeMillis: Long? = null,
    @SerialName("pace_per_100m") val pacePer100m: String? = null,
    @SerialName("split_difference") val splitDifference: String? = null
)

@Serializable
data class ProfileDto(
    @SerialName("id") val id: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("preferred_pool_meters") val preferredPoolMeters: Int? = 25,
    @SerialName("training_level") val trainingLevel: String? = "INTERMEDIARIO"
)

// "8x100" -> 800; "400" -> 400.
private fun metrosDe(repsDescription: String): Int {
    val partes = repsDescription.lowercase().removeSuffix("m").split("x")
    return when (partes.size) {
        2 -> (partes[0].trim().toIntOrNull() ?: 1) * (partes[1].trim().toIntOrNull() ?: 0)
        1 -> partes[0].trim().toIntOrNull() ?: 0
        else -> 0
    }
}

// Extension functions for DTO mapping
fun WorkoutDto.toDomain(): Workout {
    val domainLevel = TrainingLevel.entries.firstOrNull { it.name.equals(level, ignoreCase = true) }
        ?: TrainingLevel.INTERMEDIARIO

    val domainPhases = phases?.mapIndexed { index, phaseDto ->
        val phaseStatus = when (phaseDto.status?.uppercase()) {
            "COMPLETED" -> PhaseStatus.COMPLETED
            "CURRENT", "ACTIVE" -> PhaseStatus.ACTIVE
            else -> PhaseStatus.PENDING
        }

        WorkoutPhase(
            id = phaseDto.id ?: "phase_$index",
            title = phaseDto.title ?: "Fase de Nado",
            summary = phaseDto.summary ?: "",
            distanceMeters = phaseDto.distanceMeters ?: 400,
            percentage = phaseDto.percentage ?: 25,
            status = phaseStatus,
            sets = phaseDto.sets?.mapIndexed { setIndex, setDto ->
                val reps = setDto.repsDescription ?: "1x100"
                WorkoutSet(
                    id = setDto.id ?: "set_${index}_$setIndex",
                    repsDistance = reps,
                    description = setDto.stroke ?: "Crawl",
                    // Série contínua do carrossel não tem intervalo; não inventar um.
                    intervalTarget = setDto.interval ?: "",
                    intensity = setDto.intensity ?: "Z2 (70%)",
                    restSeconds = setDto.restSeconds ?: 30,
                    equipmentName = setDto.equipment,
                    isCompleted = setDto.isDone ?: false,
                    header = setDto.serie ?: "${reps}m ${setDto.stroke.orEmpty()}".trim(),
                    details = setDto.details.orEmpty(),
                    distanceMeters = setDto.distanceMeters ?: metrosDe(reps),
                    zona = setDto.zona?.takeIf { it.isNotBlank() },
                    pse = setDto.pse?.takeIf { it.isNotBlank() },
                    corretivo = setDto.corretivo?.takeIf { !it.nome.isNullOrBlank() }?.let {
                        Corretivo(it.nome.orEmpty(), it.objetivo.orEmpty(), it.dica.orEmpty())
                    }
                )
            } ?: emptyList()
        )
    } ?: emptyList()

    val workout = Workout(
        id = id ?: "workout_custom",
        title = title ?: "Treino de Natação",
        subtitle = subtitle ?: "",
        tag = tag ?: "Treino Principal",
        totalDistanceMeters = totalDistanceMeters ?: 2000,
        estimatedMinutes = estimatedMinutes ?: 50,
        calories = calories ?: 450,
        level = domainLevel,
        phases = domainPhases,
        workoutDate = workoutDate,
        isSuggestion = isSuggestion ?: false,
        focus = foco,
        cycleDay = cicloDia,
        objetivo = objetivo?.takeIf { it.isNotBlank() },
        zona = zona?.takeIf { it.isNotBlank() },
        ajuste = ajuste?.takeIf { it.isNotBlank() }
    )
    return motivationalTip?.let { workout.copy(motivationalTip = it) } ?: workout
}

fun SwimSetRecordDto.toDomain(): CompletedSetRecord {
    return CompletedSetRecord(
        setNumber = setNumber ?: 1,
        timeFormatted = timeFormatted ?: "00:00.0",
        pacePer100m = pacePer100m ?: "1'22\"/100m",
        splitDifference = splitDifference ?: "Base",
        timeMillis = timeMillis ?: 0L
    )
}

fun CompletedSetRecord.toDto(
    workoutId: String? = null,
    repDescription: String = "8x100m Crawl",
    distanceMeters: Int = 100
): SwimSetRecordDto {
    return SwimSetRecordDto(
        workoutId = workoutId,
        setNumber = setNumber,
        repDescription = repDescription,
        distanceMeters = distanceMeters,
        timeFormatted = timeFormatted,
        timeMillis = timeMillis,
        pacePer100m = pacePer100m,
        splitDifference = splitDifference
    )
}
