package com.example.data.supabase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * As chamadas ao banco (PostgREST). Os caminhos e os parâmetros são os mesmos de
 * antes; mudou só quem faz a chamada, do Retrofit para o Ktor, que roda nos dois
 * sistemas. O RLS do Supabase é quem decide o que cada usuário enxerga.
 */
class SupabaseApi(private val http: HttpClient) {

    suspend fun pingWorkouts(): Resposta<Unit> =
        pedir(HttpMethod.Get, "rest/v1/workouts", mapOf("select" to "id", "limit" to "1"))

    /** Treino do ciclo do carrossel para a data e o nível (função SQL no Supabase). */
    suspend fun getTreinosSugeridos(params: TreinosSugeridosParams): Resposta<List<WorkoutDto>> =
        pedir(HttpMethod.Post, "rest/v1/rpc/treinos_sugeridos", corpo = params)

    // ---- Meus treinos. O RLS só deixa ver e mexer nos do usuário logado. ----

    suspend fun getMyWorkouts(
        userFilter: String, // "eq.<uuid>"
        select: String = "*",
        order: String = "workout_date.desc,created_at.desc"
    ): Resposta<List<WorkoutDto>> =
        pedir(HttpMethod.Get, "rest/v1/workouts", mapOf("user_id" to userFilter, "select" to select, "order" to order))

    suspend fun createWorkout(workout: WorkoutWriteDto): Resposta<List<WorkoutDto>> =
        pedir(HttpMethod.Post, "rest/v1/workouts", corpo = workout, prefer = RETORNAR)

    suspend fun updateWorkout(idFilter: String, workout: WorkoutWriteDto): Resposta<List<WorkoutDto>> =
        pedir(HttpMethod.Patch, "rest/v1/workouts", mapOf("id" to idFilter), corpo = workout, prefer = RETORNAR)

    // Com RLS, apagar o que não é seu responde sucesso sem apagar nada;
    // pedindo as linhas de volta, a lista vazia denuncia.
    suspend fun deleteWorkout(idFilter: String): Resposta<List<WorkoutDto>> =
        pedir(HttpMethod.Delete, "rest/v1/workouts", mapOf("id" to idFilter), prefer = RETORNAR)

    // ---- Perfil ----

    suspend fun getProfile(idFilter: String, select: String = "*"): Resposta<List<ProfileDto>> =
        pedir(HttpMethod.Get, "rest/v1/profiles", mapOf("id" to idFilter, "select" to select))

    suspend fun upsertProfile(profile: ProfileWriteDto): Resposta<List<ProfileDto>> =
        pedir(
            HttpMethod.Post, "rest/v1/profiles", corpo = profile,
            prefer = "resolution=merge-duplicates,return=representation"
        )

    suspend fun deleteMyAccount(): Resposta<Unit> =
        pedir(HttpMethod.Post, "rest/v1/rpc/excluir_minha_conta", corpo = emptyMap<String, String>())

    // ---- Treinos realizados ("Concluir treino") ----

    suspend fun registrarTreino(
        registro: com.example.data.execucao.TreinoRealizadoDto
    ): Resposta<List<com.example.data.execucao.TreinoRealizadoDto>> =
        pedir(HttpMethod.Post, "rest/v1/treinos_realizados", corpo = registro, prefer = RETORNAR)

    // O RLS devolve só os do usuário logado; sem filtro de user_id aqui.
    suspend fun listarTreinosRealizados(
        select: String = "*",
        order: String = "data_treino.desc,created_at.desc",
        limite: Int = 1000
    ): Resposta<List<com.example.data.execucao.TreinoRealizadoDto>> =
        pedir(
            HttpMethod.Get, "rest/v1/treinos_realizados",
            mapOf("select" to select, "order" to order, "limit" to limite.toString())
        )

    // ---- PAR-Q. O RLS só deixa ver e registrar os do usuário logado. ----

    suspend fun ultimoParQ(
        select: String = "*",
        order: String = "respondido_em.desc",
        limite: Int = 1
    ): Resposta<List<com.example.data.parq.ParQRespostaDto>> =
        pedir(
            HttpMethod.Get, "rest/v1/parq_respostas",
            mapOf("select" to select, "order" to order, "limit" to limite.toString())
        )

    suspend fun registrarParQ(
        resposta: com.example.data.parq.ParQRespostaDto
    ): Resposta<List<com.example.data.parq.ParQRespostaDto>> =
        pedir(HttpMethod.Post, "rest/v1/parq_respostas", corpo = resposta, prefer = RETORNAR)

    // ---- Treino compartilhado por link. Criar é do dono; abrir é pela função, com o código. ----

    suspend fun compartilharTreino(
        treino: com.example.data.compartilhar.NovoTreinoCompartilhadoDto,
        select: String = "codigo,titulo"
    ): Resposta<List<com.example.data.compartilhar.TreinoCompartilhadoDto>> =
        pedir(
            HttpMethod.Post, "rest/v1/treinos_compartilhados", mapOf("select" to select),
            corpo = treino, prefer = RETORNAR
        )

    suspend fun abrirTreinoCompartilhado(
        params: com.example.data.compartilhar.AbrirTreinoParams
    ): Resposta<List<com.example.data.compartilhar.TreinoCompartilhadoDto>> =
        pedir(HttpMethod.Post, "rest/v1/rpc/abrir_treino_compartilhado", corpo = params)

    // ---- Ranking. Cada um lê e grava só os próprios dados; a lista vem da função. ----

    suspend fun lerParticipacaoNoRanking(
        idFilter: String,
        select: String = CAMPOS_DO_RANKING
    ): Resposta<List<com.example.data.ranking.ParticipacaoDto>> =
        pedir(HttpMethod.Get, "rest/v1/profiles", mapOf("id" to idFilter, "select" to select))

    suspend fun salvarParticipacaoNoRanking(
        idFilter: String,
        participacao: com.example.data.ranking.ParticipacaoDto,
        select: String = CAMPOS_DO_RANKING
    ): Resposta<List<com.example.data.ranking.ParticipacaoDto>> =
        pedir(
            HttpMethod.Patch, "rest/v1/profiles", mapOf("id" to idFilter, "select" to select),
            corpo = participacao, prefer = RETORNAR
        )

    suspend fun rankingNadadores(
        parametros: com.example.data.ranking.ParametrosDoRanking
    ): Resposta<List<com.example.data.ranking.LinhaDoRanking>> =
        pedir(HttpMethod.Post, "rest/v1/rpc/ranking_nadadores", corpo = parametros)

    // ---- Plano de treino. O RLS só deixa ver, criar e apagar os do usuário logado. ----

    suspend fun planoAtual(
        select: String = "*",
        order: String = "criado_em.desc",
        limite: Int = 1
    ): Resposta<List<com.example.data.plano.PlanoDto>> =
        pedir(
            HttpMethod.Get, "rest/v1/planos_treino",
            mapOf("select" to select, "order" to order, "limit" to limite.toString())
        )

    suspend fun criarPlano(plano: com.example.data.plano.PlanoDto): Resposta<List<com.example.data.plano.PlanoDto>> =
        pedir(HttpMethod.Post, "rest/v1/planos_treino", corpo = plano, prefer = RETORNAR)

    suspend fun trocarTreinosDoPlano(
        idFilter: String,
        trocas: com.example.data.plano.TrocasDoPlanoDto
    ): Resposta<List<com.example.data.plano.PlanoDto>> =
        pedir(HttpMethod.Patch, "rest/v1/planos_treino", mapOf("id" to idFilter), corpo = trocas, prefer = RETORNAR)

    suspend fun excluirPlano(idFilter: String): Resposta<List<com.example.data.plano.PlanoDto>> =
        pedir(HttpMethod.Delete, "rest/v1/planos_treino", mapOf("id" to idFilter), prefer = RETORNAR)

    // ---- Séries cronometradas ----

    suspend fun getSwimSetRecords(
        select: String = "*",
        order: String = "set_number.asc"
    ): Resposta<List<SwimSetRecordDto>> =
        pedir(HttpMethod.Get, "rest/v1/swim_set_records", mapOf("select" to select, "order" to order))

    suspend fun insertSwimSetRecord(record: SwimSetRecordDto): Resposta<List<SwimSetRecordDto>> =
        pedir(HttpMethod.Post, "rest/v1/swim_set_records", corpo = record, prefer = RETORNAR)

    /**
     * Uma chamada só. Sucesso devolve o corpo já convertido; erro devolve o texto
     * cru, que MensagensAuth traduz. Exceção de rede sobe para quem chamou tratar.
     */
    private suspend inline fun <reified T> pedir(
        metodo: HttpMethod,
        caminho: String,
        consulta: Map<String, String> = emptyMap(),
        corpo: Any? = null,
        prefer: String? = null
    ): Resposta<T> {
        val resposta = http.request(caminho) {
            method = metodo
            url { consulta.forEach { (nome, valor) -> parameters.append(nome, valor) } }
            if (prefer != null) header("Prefer", prefer)
            if (corpo != null) {
                contentType(ContentType.Application.Json)
                setBody(corpo)
            }
        }
        if (!resposta.status.isSuccess()) {
            return Resposta(resposta.status.value, null, resposta.bodyAsText())
        }
        val valor: T = if (T::class == Unit::class) Unit as T else resposta.body()
        return Resposta(resposta.status.value, valor, null)
    }

    companion object {
        private const val RETORNAR = "return=representation"
        private const val CAMPOS_DO_RANKING =
            "ranking_publico,ranking_nome,ano_nascimento,sexo,cidade,local_treino"
    }
}
