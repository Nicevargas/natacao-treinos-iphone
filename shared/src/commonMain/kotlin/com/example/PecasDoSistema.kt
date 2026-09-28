package com.example

import androidx.compose.ui.text.TextMeasurer
import com.example.data.Guardados
import com.example.data.LeitorDeArquivos
import com.example.data.compartilhar.FormatoDoCartao
import com.example.data.compartilhar.ResumoDoTreino
import com.example.data.lembrete.AgendaDeLembretes
import com.example.model.Workout

/**
 * Tudo o que o app precisa do aparelho, entregue por quem o abre: a MainActivity
 * no Android e o MainViewController no iPhone. As telas e os ViewModels só
 * conhecem estas portas, nunca o sistema.
 */
class PecasDoSistema(
    /** Os programas de treino embarcados (assets no Android, o pacote do app no iPhone). */
    val arquivos: LeitorDeArquivos,
    /** O que o app guarda no aparelho, por nome de arquivo. */
    val guardados: (arquivo: String) -> Guardados,
    /** Onde o aviso das 7h é agendado. */
    val lembretes: AgendaDeLembretes,
    /** Quem entrega os cartões ao menu de compartilhar do sistema. */
    val compartilhador: Compartilhador
)

/** Entregar o cartão pronto ao menu de compartilhar de cada sistema. */
interface Compartilhador {

    /** A imagem do treino concluído, com a legenda. */
    fun treinoConcluido(resumo: ResumoDoTreino, formato: FormatoDoCartao, medidor: TextMeasurer)

    /** O convite: a imagem do treino para fazer, com o link e o código. */
    fun convite(treino: Workout, codigo: String, mensagem: String, medidor: TextMeasurer)
}
