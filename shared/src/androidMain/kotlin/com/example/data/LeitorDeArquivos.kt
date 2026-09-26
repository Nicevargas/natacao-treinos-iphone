package com.example.data

/**
 * Lê os arquivos embarcados no app: os programas de treino em JSON, que são a
 * cópia do carrossel usada quando não há rede.
 *
 * A leitura é imediata, e não suspensa, de propósito: o treino do dia aparece no
 * primeiro quadro da tela, sem esperar nada. Cada sistema busca no seu lugar
 * (assets no Android, o pacote do app no iPhone).
 */
fun interface LeitorDeArquivos {

    /** O conteúdo do arquivo, pelo nome ("programa_nc.json"). */
    fun ler(nome: String): String
}
