package com.example.ui.compartilhar

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.text.TextMeasurer
import androidx.core.content.FileProvider
import com.example.data.compartilhar.FormatoDoCartao
import com.example.data.compartilhar.ResumoDoTreino
import com.example.data.compartilhar.TextosDoTreino
import com.example.model.Workout
import java.io.File
import java.io.FileOutputStream

/**
 * A entrega do cartão no Android: virar arquivo e abrir o menu de compartilhar.
 * O desenho em si é comum aos dois sistemas (CartaoDoTreino e CartaoDoConvite);
 * só esta parte é que muda de aparelho para aparelho.
 */

/** Grava o PNG na pasta de cache compartilhável; apaga os antigos. */
private fun salvar(context: Context, imagem: ImageBitmap, prefixo: String): File {
    val pasta = File(context.cacheDir, "compartilhar").apply { mkdirs() }
    pasta.listFiles()?.forEach { it.delete() }
    val arquivo = File(pasta, "${prefixo}_${System.currentTimeMillis()}.png")
    FileOutputStream(arquivo).use { imagem.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    return arquivo
}

private fun enviar(context: Context, arquivo: File, legenda: String, titulo: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.compartilhar", arquivo)

    // O Instagram ignora o texto que vem junto; por isso a legenda também vai
    // para a área de transferência, e é só colar.
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
        ?.setPrimaryClip(ClipData.newPlainText("Legenda do treino", legenda))

    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, legenda)
        clipData = ClipData.newRawUri("Treino", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val escolha = Intent.createChooser(envio, titulo)
    if (context !is Activity) escolha.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(escolha)
}

/** Abre o menu de compartilhar com a imagem do treino concluído e a legenda. */
fun compartilharTreino(
    context: Context,
    resumo: ResumoDoTreino,
    formato: FormatoDoCartao,
    medidor: TextMeasurer
) {
    val arquivo = salvar(context, CartaoDoTreino.desenhar(resumo, formato, medidor), "treino")
    enviar(context, arquivo, TextosDoTreino.legenda(resumo), "Publicar treino")
}

/** Abre o menu de compartilhar com o convite: a imagem do treino e o código para abrir no app. */
fun compartilharTreinoParaFazer(
    context: Context,
    treino: Workout,
    codigo: String,
    mensagem: String,
    medidor: TextMeasurer
) {
    val arquivo = salvar(context, CartaoDoConvite.desenhar(treino, codigo, medidor), "convite")
    enviar(context, arquivo, mensagem, "Enviar treino")
}
