package com.example

import android.content.Context
import androidx.compose.ui.text.TextMeasurer
import com.example.data.compartilhar.FormatoDoCartao
import com.example.data.compartilhar.ResumoDoTreino
import com.example.data.guardadosDe
import com.example.data.leitorDeArquivosDe
import com.example.data.lembrete.AgendaDeLembretesAndroid
import com.example.model.Workout
import com.example.ui.compartilhar.compartilharTreino
import com.example.ui.compartilhar.compartilharTreinoParaFazer

/**
 * As peças do Android para o app. Recebe a Activity, e não só o contexto do
 * aplicativo, porque o menu de compartilhar abre por cima dela.
 */
fun pecasDoAndroid(activity: Context): PecasDoSistema {
    val aplicativo = activity.applicationContext
    return PecasDoSistema(
        arquivos = leitorDeArquivosDe(aplicativo),
        guardados = { arquivo -> guardadosDe(aplicativo, arquivo) },
        lembretes = AgendaDeLembretesAndroid(aplicativo),
        compartilhador = object : Compartilhador {
            override fun treinoConcluido(resumo: ResumoDoTreino, formato: FormatoDoCartao, medidor: TextMeasurer) =
                compartilharTreino(activity, resumo, formato, medidor)

            override fun convite(treino: Workout, codigo: String, mensagem: String, medidor: TextMeasurer) =
                compartilharTreinoParaFazer(activity, treino, codigo, mensagem, medidor)
        }
    )
}
