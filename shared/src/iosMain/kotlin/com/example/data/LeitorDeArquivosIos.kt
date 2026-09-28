package com.example.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/**
 * No iPhone os programas de treino vão dentro do pacote do app. O projeto do
 * Xcode (iosApp/project.yml) inclui os JSON de shared/src/commonMain/recursos,
 * os mesmos que o Android usa.
 */
@OptIn(ExperimentalForeignApi::class)
fun leitorDeArquivosDoIos(): LeitorDeArquivos = LeitorDeArquivos { nome ->
    val base = nome.substringBeforeLast('.')
    val extensao = nome.substringAfterLast('.', "")
    val caminho = NSBundle.mainBundle.pathForResource(base, ofType = extensao)
        ?: error("$nome não está no pacote do app")
    NSString.stringWithContentsOfFile(caminho, encoding = NSUTF8StringEncoding, error = null)
        ?: error("Não foi possível ler $nome")
}
