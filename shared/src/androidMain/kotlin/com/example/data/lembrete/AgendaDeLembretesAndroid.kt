package com.example.data.lembrete

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.data.guardadosDe
import com.example.shared.R

/** O aviso de treino no Android: alarme do sistema e notificação. */
class AgendaDeLembretesAndroid(private val context: Context) : AgendaDeLembretes {

    private val guardados = guardadosDe(context, LembreteDeTreino.ARQUIVO)

    override fun agendar(dia: Long) {
        guardados.salvarNumero(LembreteDeTreino.CHAVE_DIA, dia)
        programarAlarme(dia)
    }

    /** Depois de reiniciar o celular o Android apaga os alarmes: o do dia combinado volta. */
    override fun reprogramar() {
        val dia = guardados.numero(LembreteDeTreino.CHAVE_DIA, Long.MIN_VALUE)
        if (dia != Long.MIN_VALUE && LembreteDeTreino.horarioDoAviso(dia) > System.currentTimeMillis()) {
            programarAlarme(dia)
        }
    }

    private fun programarAlarme(dia: Long) {
        val alarmes = context.getSystemService(AlarmManager::class.java) ?: return
        val quando = LembreteDeTreino.horarioDoAviso(dia)
        if (quando <= System.currentTimeMillis()) return
        // Inexato de propósito: não precisa de permissão especial e economiza bateria.
        alarmes.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando, intencaoDoAlarme(context))
    }

    /** Mostra a notificação e esquece o dia combinado. Chamada pelo alarme. */
    fun avisar() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(
                NotificationChannel(CANAL, "Lembretes de treino", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val abrir = PendingIntent.getActivity(
            context,
            0,
            // O módulo shared não enxerga a MainActivity (fica no módulo do app): abre pela entrada do app.
            (context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificacao = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle(LembreteDeTreino.TITULO_DO_AVISO)
            .setContentText(LembreteDeTreino.TEXTO_DO_AVISO)
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(ID_NOTIFICACAO, notificacao) }
        guardados.remover(LembreteDeTreino.CHAVE_DIA)
    }

    private companion object {
        const val CANAL = "lembretes_de_treino"
        const val ID_NOTIFICACAO = 7001

        fun intencaoDoAlarme(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, LembreteReceiver::class.java).setAction(LembreteReceiver.ACAO_AVISAR),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class LembreteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val agenda = AgendaDeLembretesAndroid(context)
        when (intent.action) {
            ACAO_AVISAR -> agenda.avisar()
            Intent.ACTION_BOOT_COMPLETED -> agenda.reprogramar()
        }
    }

    companion object {
        const val ACAO_AVISAR = "com.example.LEMBRETE_DE_TREINO"
    }
}
