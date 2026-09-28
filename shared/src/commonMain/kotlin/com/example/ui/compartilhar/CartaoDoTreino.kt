package com.example.ui.compartilhar

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.example.data.ciclo.DataCivil
import com.example.data.compartilhar.FormatoDoCartao
import com.example.data.compartilhar.ResumoDoTreino
import com.example.data.compartilhar.TextosDoTreino
import com.example.model.TrainingLevel

/**
 * Imagem do treino concluído, para publicar nas redes.
 *
 * Desenhada ponto a ponto com o Canvas do Compose, que existe nos dois sistemas.
 * As medidas estão em pixels, e por isso o desenho usa densidade 1: o cartão sai
 * do mesmo tamanho em qualquer aparelho, que é o que as redes esperam.
 */
object CartaoDoTreino {

    private val FUNDO_TOPO = Color(0xFF071A33)
    private val FUNDO_BASE = Color(0xFF00315C)
    private val AZUL = Color(0xFF0076D1)
    private val CIANO = Color(0xFF00A3FF)
    private val MAGENTA = Color(0xFFE30071)
    private val BRANCO = Color(0xFFFFFFFF)
    private val BRANCO_SUAVE = Color(0xB3FFFFFF)
    private val TRILHA = Color(0x33FFFFFF)

    private val DIAS = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")

    // Cores dos níveis no carrossel.
    private fun corDoNivel(nivel: TrainingLevel): Color = when (nivel) {
        TrainingLevel.INICIANTE -> Color(0xFF8DC63F)
        TrainingLevel.INTERMEDIARIO -> Color(0xFFF7B90B)
        TrainingLevel.AVANCADO -> Color(0xFFE01B62)
    }

    fun desenhar(r: ResumoDoTreino, formato: FormatoDoCartao, medidor: TextMeasurer): ImageBitmap {
        val w = formato.largura.toFloat()
        val h = formato.altura.toFloat()
        val imagem = ImageBitmap(formato.largura, formato.altura)

        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(imagem), Size(w, h)) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(FUNDO_TOPO, FUNDO_BASE),
                    start = Offset.Zero,
                    end = Offset(w * 0.4f, h)
                )
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(AZUL, CIANO, MAGENTA),
                    start = Offset.Zero,
                    end = Offset(w, 0f)
                ),
                size = Size(w, 16f)
            )

            val margem = 90f
            val largura = w - 2 * margem
            val stories = formato == FormatoDoCartao.STORIES
            // No stories o topo e a base ficam sob os controles do Instagram; o feed
            // (4:5) é mais baixo e precisa ser compacto para caber as duas notas.
            var y = if (stories) 320f else 110f

            y = texto(medidor, "NATAÇÃO CRIATIVA", margem, y, 34f, CIANO, negrito = true, espaco = 0.3f)
            y += 36f
            y = texto(
                medidor, if (r.completo) "TREINO CONCLUÍDO" else "TREINO REGISTRADO",
                margem, y, 46f, MAGENTA, negrito = true, espaco = 0.12f
            )
            y += 16f
            y = texto(medidor, r.foco ?: r.titulo, margem, y, 104f, BRANCO, negrito = true, larguraMax = largura)
            y += 8f
            y = texto(medidor, dataLonga(r.dataIso), margem, y, 40f, BRANCO_SUAVE)

            y += if (stories) 130f else 50f
            y = texto(
                medidor, TextosDoTreino.metros(r.metrosFeitos), margem, y,
                if (stories) 230f else 190f, BRANCO, negrito = true, larguraMax = largura
            )
            y += 4f
            val subtitulo = if (r.completo) {
                "em ${TextosDoTreino.duracao(r.duracaoSegundos)}"
            } else {
                "de ${TextosDoTreino.metros(r.metrosPlanejados)} · ${TextosDoTreino.duracao(r.duracaoSegundos)}"
            }
            y = texto(medidor, subtitulo, margem, y, 60f, CIANO, negrito = true, larguraMax = largura)

            if (!r.completo && r.metrosPlanejados > 0) {
                y += 28f
                val fracao = (r.metrosFeitos.toFloat() / r.metrosPlanejados).coerceIn(0f, 1f)
                barra(margem, y, largura, 20f, 10f, TRILHA)
                barra(margem, y, largura * fracao, 20f, 10f, CIANO)
                y += 20f
            }

            y += 50f
            val rotulo = TextosDoTreino.nivel(r.nivel)
            val estiloChip = estilo(38f, FUNDO_TOPO, negrito = true)
            val larguraChip = medidor.measure(rotulo, estiloChip).size.width + 64f
            barra(margem, y, larguraChip.toFloat(), 76f, 38f, corDoNivel(r.nivel))
            texto(medidor, rotulo, margem + 32f, y + 14f, 38f, FUNDO_TOPO, negrito = true)
            y += 76f

            if (r.intensidade != null || r.complexidade != null) {
                y += if (stories) 110f else 44f
                val entreNotas = if (stories) 36f else 24f
                r.intensidade?.let { y = barraDeNota(medidor, "Intensidade", it, margem, y, largura) + entreNotas }
                r.complexidade?.let { y = barraDeNota(medidor, "Complexidade", it, margem, y, largura) + entreNotas }
            }

            val rodape = if (r.doCarrossel) {
                "CADA DIA 1 TREINO · @natacaocriativa"
            } else {
                "Treino criado no app Natação Criativa"
            }
            // O rodapé fica embaixo, mas nunca por cima do que já foi desenhado.
            val topoRodape = maxOf(h - if (stories) 330f else 110f, y + 24f)
            texto(medidor, rodape, margem, topoRodape, 38f, BRANCO, negrito = true, larguraMax = largura)
        }

        return imagem
    }

    private fun dataLonga(iso: String): String = runCatching {
        val dia = DataCivil.deIso(iso)
        "${DIAS[DataCivil.diaDaSemana(dia)]}, ${DataCivil.paraBr(dia)}"
    }.getOrDefault(iso)

    private fun estilo(tamanho: Float, cor: Color, negrito: Boolean, espaco: Float = 0f) = TextStyle(
        color = cor,
        fontSize = tamanho.sp,
        fontWeight = if (negrito) FontWeight.Bold else FontWeight.Normal,
        letterSpacing = (tamanho * espaco).sp
    )

    private fun DrawScope.barra(x: Float, y: Float, largura: Float, altura: Float, raio: Float, cor: Color) {
        drawRoundRect(
            color = cor,
            topLeft = Offset(x, y),
            size = Size(largura, altura),
            cornerRadius = CornerRadius(raio, raio)
        )
    }

    /** Escreve com o topo da letra em [topo] e devolve onde a linha termina. Encolhe para caber. */
    private fun DrawScope.texto(
        medidor: TextMeasurer,
        conteudo: String,
        x: Float,
        topo: Float,
        tamanho: Float,
        cor: Color,
        negrito: Boolean = false,
        larguraMax: Float? = null,
        espaco: Float = 0f
    ): Float {
        var atual = tamanho
        var linha = conteudo
        var medida = medidor.measure(linha, estilo(atual, cor, negrito, espaco))
        if (larguraMax != null) {
            while (medida.size.width > larguraMax && atual > 40f) {
                atual -= 4f
                medida = medidor.measure(linha, estilo(atual, cor, negrito, espaco))
            }
            while (medida.size.width > larguraMax && linha.length > 2) {
                linha = linha.dropLast(2) + "…"
                medida = medidor.measure(linha, estilo(atual, cor, negrito, espaco))
            }
        }
        // Linha que começaria abaixo da imagem não é desenhada: o Compose recusa
        // altura negativa, enquanto o Canvas do Android desenhava no invisível.
        if (topo < size.height) {
            drawText(medidor, linha, topLeft = Offset(x, topo), style = estilo(atual, cor, negrito, espaco))
        }
        return topo + medida.size.height
    }

    private fun DrawScope.barraDeNota(
        medidor: TextMeasurer,
        nome: String,
        nota: Int,
        x: Float,
        topo: Float,
        largura: Float
    ): Float {
        val fim = texto(medidor, "$nome: $nota/10", x, topo, 40f, BRANCO, negrito = true)
        val y = fim + 14f
        val vao = 10f
        val segmento = (largura - 9 * vao) / 10
        for (i in 0 until 10) {
            barra(x + i * (segmento + vao), y, segmento, 22f, 11f, if (i < nota) MAGENTA else TRILHA)
        }
        return y + 22f
    }
}
