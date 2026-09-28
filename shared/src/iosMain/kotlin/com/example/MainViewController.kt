package com.example

import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.example.data.Registro
import com.example.data.auth.AuthRepository
import com.example.data.auth.SessaoStore
import com.example.data.compartilhar.FormatoDoCartao
import com.example.data.compartilhar.LinkDeTreino
import com.example.data.compartilhar.ResumoDoTreino
import com.example.data.compartilhar.TextosDoTreino
import com.example.data.guardadosDoIos
import com.example.data.leitorDeArquivosDoIos
import com.example.data.lembrete.AgendaDeLembretesIos
import com.example.model.Workout
import com.example.ui.compartilhar.CartaoDoConvite
import com.example.ui.compartilhar.CartaoDoTreino
import com.example.ui.compartilhar.compartilharNoIos
import com.example.ui.theme.MyApplicationTheme
import platform.UIKit.UIViewController

/**
 * A porta de entrada do app no iPhone. O projeto do Xcode (iosApp) chama esta
 * função e mostra a tela que ela devolve: as mesmas telas do Android.
 */
@Suppress("FunctionName") // Nome em maiúscula, como o Swift espera de um "construtor" de tela.
fun MainViewController(): UIViewController {
    iniciarNoIos()
    return ComposeUIViewController {
        // Fotos da internet: no iPhone o carregador precisa saber que a rede é pelo Ktor.
        setSingletonImageLoaderFactory { contexto ->
            ImageLoader.Builder(contexto)
                .components { add(KtorNetworkFetcherFactory()) }
                .crossfade(true)
                .build()
        }
        val pecas = remember { pecasDoIos() }
        MyApplicationTheme {
            AquagendaRaiz(pecas = pecas)
        }
    }
}

/** O Swift chama quando um link natacaocriativa:// abre o app. */
fun receberLink(url: String) {
    LinkDeTreino.receber(url)
}

private var iniciado = false

/** O mesmo que a MainActivity faz ao abrir: onde fica a sessão e para onde vão os avisos. */
private fun iniciarNoIos() {
    if (iniciado) return
    iniciado = true
    AuthRepository.init(guardadosDoIos(SessaoStore.ARQUIVO))
    Registro.saida = Registro.Saida { grave, marca, mensagem, causa ->
        val nivel = if (grave) "ERRO" else "AVISO"
        println("[$nivel] $marca: $mensagem${causa?.let { " (${it.message})" }.orEmpty()}")
    }
}

/** As peças do iPhone para o app, espelhando pecasDoAndroid(). */
private fun pecasDoIos() = PecasDoSistema(
    arquivos = leitorDeArquivosDoIos(),
    guardados = { arquivo -> guardadosDoIos(arquivo) },
    lembretes = AgendaDeLembretesIos(),
    compartilhador = object : Compartilhador {
        override fun treinoConcluido(resumo: ResumoDoTreino, formato: FormatoDoCartao, medidor: TextMeasurer) =
            compartilharNoIos(CartaoDoTreino.desenhar(resumo, formato, medidor), TextosDoTreino.legenda(resumo))

        override fun convite(treino: Workout, codigo: String, mensagem: String, medidor: TextMeasurer) =
            compartilharNoIos(CartaoDoConvite.desenhar(treino, codigo, medidor), mensagem)
    }
)
