package com.example.ui.compartilhar

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController

/**
 * A entrega do cartão no iPhone: virar imagem e abrir a folha de compartilhar
 * do sistema (WhatsApp, Instagram, salvar na galeria...).
 */

/** O cartão desenhado pelo Compose, como imagem que o iPhone entende. */
@OptIn(ExperimentalForeignApi::class)
internal fun ImageBitmap.paraUIImage(): UIImage? {
    val png = Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes ?: return null
    val dados = png.usePinned { fixo ->
        NSData.create(bytes = fixo.addressOf(0), length = png.size.toULong())
    }
    return UIImage.imageWithData(dados)
}

/** A tela por cima da qual a folha de compartilhar abre. */
private fun telaAtual(): UIViewController? {
    var tela = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (tela?.presentedViewController != null) tela = tela.presentedViewController
    return tela
}

/**
 * Abre a folha de compartilhar com a imagem e o texto. O Instagram ignora o texto
 * que vem junto; por isso ele também vai para a área de transferência, como no
 * Android, e é só colar.
 */
internal fun compartilharNoIos(imagem: ImageBitmap, texto: String) {
    UIPasteboard.generalPasteboard.string = texto
    val itens = listOfNotNull(imagem.paraUIImage(), texto)
    val folha = UIActivityViewController(activityItems = itens, applicationActivities = null)
    telaAtual()?.presentViewController(folha, animated = true, completion = null)
}
