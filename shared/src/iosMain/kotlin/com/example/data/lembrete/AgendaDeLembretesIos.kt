package com.example.data.lembrete

import com.example.data.ciclo.DataCivil
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

/**
 * O aviso de treino no iPhone: uma notificação local, agendada no próprio
 * sistema para as 7h do dia combinado.
 *
 * Diferente do Android, o iPhone guarda o aviso mesmo que o celular reinicie,
 * então não precisa lembrar o dia nem reprogramar nada.
 */
class AgendaDeLembretesIos : AgendaDeLembretes {

    private val central = UNUserNotificationCenter.currentNotificationCenter()

    override fun agendar(dia: Long) {
        val (ano, mes, d) = DataCivil.civil(dia)
        val quando = NSDateComponents().apply {
            year = ano.toLong()
            month = mes.toLong()
            day = d.toLong()
            hour = LembreteDeTreino.HORA_DO_AVISO.toLong()
            minute = 0
        }
        val conteudo = UNMutableNotificationContent().apply {
            setTitle(LembreteDeTreino.TITULO_DO_AVISO)
            setBody(LembreteDeTreino.TEXTO_DO_AVISO)
            setSound(UNNotificationSound.defaultSound())
        }
        val gatilho = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(quando, repeats = false)
        // Mesmo identificador de propósito: combinar outro dia substitui o aviso anterior.
        val pedido = UNNotificationRequest.requestWithIdentifier(IDENTIFICADOR, conteudo, gatilho)
        central.removePendingNotificationRequestsWithIdentifiers(listOf(IDENTIFICADOR))
        central.addNotificationRequest(pedido, withCompletionHandler = null)
    }

    /** O iPhone não perde o aviso ao reiniciar: nada a reprogramar. */
    override fun reprogramar() = Unit

    private companion object {
        const val IDENTIFICADOR = "lembrete_de_treino"
    }
}
