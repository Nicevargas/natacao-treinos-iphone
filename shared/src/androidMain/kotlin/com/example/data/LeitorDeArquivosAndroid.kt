package com.example.data

import android.content.Context

/** No Android os programas de treino ficam em assets, dentro do próprio APK. */
fun leitorDeArquivosDe(context: Context): LeitorDeArquivos {
    val assets = context.applicationContext.assets
    return LeitorDeArquivos { nome -> assets.open(nome).bufferedReader().use { it.readText() } }
}
