package com.example.data.lembrete

import com.example.data.ciclo.DataCivil
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * "Quando você vai voltar a nadar?": a pessoa escolhe o dia depois de salvar o
 * treino e o celular avisa às 7h desse dia. Fica só no aparelho, sem banco.
 *
 * Aqui está a parte que vale nos dois sistemas: que dias oferecer, como
 * escrevê-los e a que horas avisar. Quem sabe mandar o aviso é a
 * [AgendaDeLembretes] de cada sistema.
 */
object LembreteDeTreino {

    const val HORA_DO_AVISO = 7

    /** Mesmo arquivo de sempre, para o dia combinado sobreviver à atualização. */
    const val ARQUIVO = "lembrete_de_treino"
    const val CHAVE_DIA = "dia"

    private val DIAS = listOf("segunda", "terça", "quarta", "quinta", "sexta", "sábado", "domingo")

    /** Os 7 dias a partir de amanhã. */
    fun proximosDias(hoje: Long): List<Long> = (1L..7L).map { hoje + it }

    /** "na quarta, 16/09" / "no sábado, 19/09" */
    fun rotulo(dia: Long): String {
        val nome = DIAS[DataCivil.diaDaSemana(dia)]
        val artigo = if (nome == "sábado" || nome == "domingo") "no" else "na"
        return "$artigo $nome, ${DataCivil.paraBr(dia).take(5)}"
    }

    /** Instante do aviso: [hora]h do [dia], no fuso do celular. */
    @OptIn(ExperimentalTime::class)
    fun horarioDoAviso(
        dia: Long,
        hora: Int = HORA_DO_AVISO,
        fuso: TimeZone = TimeZone.currentSystemDefault()
    ): Long {
        val (ano, mes, d) = DataCivil.civil(dia)
        return LocalDateTime(ano, mes, d, hora, 0).toInstant(fuso).toEpochMilliseconds()
    }

    /** Texto do aviso, igual nos dois sistemas. */
    const val TITULO_DO_AVISO = "Hoje é dia de nadar!"
    const val TEXTO_DO_AVISO = "Você combinou de voltar hoje. O treino do dia já está no app."
}

/**
 * Quem de fato agenda o aviso no aparelho. No Android é o AlarmManager; no
 * iPhone serão as notificações locais do sistema.
 */
interface AgendaDeLembretes {

    /** Guarda o dia combinado e programa o aviso das 7h. */
    fun agendar(dia: Long)

    /**
     * Reprograma o aviso guardado, se ainda estiver no futuro. O Android apaga os
     * alarmes quando o celular reinicia; o iPhone não precisa disso.
     */
    fun reprogramar()
}
