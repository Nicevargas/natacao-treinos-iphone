package com.example.data.supabase

import android.util.Log
import com.example.data.ciclo.DataCivil
import com.example.model.CompletedSetRecord
import com.example.model.ModoDeTreino
import com.example.model.TrainingLevel
import com.example.model.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseRepository {
    private const val TAG = "SupabaseRepository"

    fun getInitialStatus(): SupabaseStatus {
        return if (SupabaseClient.isConfigured) {
            SupabaseStatus.CONNECTING
        } else {
            SupabaseStatus.CONFIG_NEEDED
        }
    }

    suspend fun testConnection(): SupabaseStatus = withContext(Dispatchers.IO) {
        if (!SupabaseClient.isConfigured) {
            return@withContext SupabaseStatus.CONFIG_NEEDED
        }
        val api = SupabaseClient.api ?: return@withContext SupabaseStatus.CONFIG_NEEDED
        try {
            val response = api.pingWorkouts()
            if (response.sucesso) {
                Log.i(TAG, "Supabase connection verified successfully! Status code: ${response.codigo}")
                SupabaseStatus.CONNECTED
            } else {
                Log.w(TAG, "Supabase returned response code: ${response.codigo}")
                SupabaseStatus.OFFLINE_LOCAL
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase connection failed, falling back to local mode", e)
            SupabaseStatus.OFFLINE_LOCAL
        }
    }

    /**
     * Treino sugerido para a data e o nível, vindo de public.treinos_sugeridos.
     * Null sem Supabase ou em falha: quem chama fica com a cópia embarcada.
     */
    suspend fun getTreinoSugerido(
        epochDay: Long,
        level: TrainingLevel,
        modo: ModoDeTreino = ModoDeTreino.PISCINA
    ): Workout? = withContext(Dispatchers.IO) {
        val api = SupabaseClient.api ?: return@withContext null
        try {
            val params = TreinosSugeridosParams(
                data = DataCivil.paraIso(epochDay),
                level = level.name,
                modo = modo.valorNoBanco.takeIf { modo != ModoDeTreino.PISCINA }
            )
            val response = api.getTreinosSugeridos(params)
            if (response.sucesso) {
                response.corpo?.firstOrNull()?.toDomain()
            } else {
                Log.w(TAG, "treinos_sugeridos returned HTTP ${response.codigo}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying treinos_sugeridos, keeping bundled cycle", e)
            null
        }
    }

    suspend fun getSwimSetRecords(): List<CompletedSetRecord> = withContext(Dispatchers.IO) {
        val api = SupabaseClient.api ?: return@withContext emptyList()
        try {
            val response = api.getSwimSetRecords()
            if (response.sucesso && !response.corpo.isNullOrEmpty()) {
                val domainLaps = response.corpo!!.map { it.toDomain() }
                Log.i(TAG, "Retrieved ${domainLaps.size} swim set records from Supabase")
                domainLaps
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching swim sets from Supabase", e)
            emptyList()
        }
    }

    suspend fun recordSwimSet(
        record: CompletedSetRecord,
        workoutId: String?,
        repDescription: String,
        distanceMeters: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val api = SupabaseClient.api ?: return@withContext false
        try {
            val dto = record.toDto(workoutId = workoutId, repDescription = repDescription, distanceMeters = distanceMeters)
            val response = api.insertSwimSetRecord(dto)
            if (response.sucesso) {
                Log.i(TAG, "Swim set S${record.setNumber} successfully stored in Supabase!")
                true
            } else {
                Log.w(TAG, "Failed to save swim set in Supabase. HTTP ${response.codigo}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception storing swim set in Supabase", e)
            false
        }
    }
}
