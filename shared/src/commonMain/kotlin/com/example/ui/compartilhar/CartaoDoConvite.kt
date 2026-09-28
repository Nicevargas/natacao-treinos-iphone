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
import com.example.data.compartilhar.FormatoDoCartao
import com.example.data.compartilhar.TextosDoTreino
import com.example.model.TrainingLevel
import com.example.model.Workout

/**
 * Imagem do treino para fazer, que vai junto com o link ao compartilhar.
 * Mostra as fases com as séries e zonas; o código fica em destaque no rodapé.
 *
 * Desenhada com o Canvas do Compose, que existe nos dois sistemas.
 */
object CartaoDoConvite {

    private val FUNDO_TOPO = Color(0xFF071A33)
    private val FUNDO_BASE = Color(0xFF00315C)
    private val AZUL = Color(0xFF0076D1)
    private val CIANO = Color(0xFF00A3FF)
    private val MAGENTA = Color(0xFFE30071)
    private val AMARELO = Color(0xFFFFB800)
    private val VERDE = Color(0xFF48CA1B)
    private val BRANCO = Color(0xFFFFFFFF)
    private val BRANCO_SUAVE = Color(0xB3FFFFFF)
    private val CARTAO = Color(0x1AFFFFFF)

    private val formato = FormatoDoCartao.FEED

    private fun corDoNivel(nivel: TrainingLevel): Color = when (nivel) {
        TrainingLevel.INICIANTE -> Color(0xFF8DC63F)
        TrainingLevel.INTERMEDIARIO -> Color(0xFFF7B90B)
        TrainingLevel.AVANCADO -> Color(0xFFE01B62)
    }

    // Mesmas cores do app e do carrossel.
    private fun corDoBloco(titulo: String): Color = when (titulo.trim().lowercase()) {
        "ativação", "ativacao", "aquecimento" -> AZUL
        "preparação", "preparacao", "preparatória", "preparatoria" -> AMARELO
        "desenvolvimento", "principal" -> MAGENTA
        "consolidação", "consolidacao" -> VERDE
        else -> CIANO
    }

    private fun corDaZona(sigla: String): Color = when (sigla.trim().uppercase()) {
        "A0" -> Color(0xFF8FB4D8)
        "A1" -> VERDE
        "A2" -> AMARELO
        "A3" -> Color(0xFFFF8A00)
        "AN" -> MAGENTA
        "AA" -> Color(0xFF9B7BFF)
        else -> BRANCO_SUAVE
    }

    fun desenhar(treino: Workout, codigo: String, medidor: TextMeasurer): ImageBitmap {
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

            val margem = 80f
            val largura = w - 2 * margem
            var y = 90f

            y = texto(medidor, "NATAÇÃO CRIATIVA", margem, y, 32f, CIANO, negrito = true, espaco = 0.3f)
            y += 28f
            y = texto(medidor, "TREINO PARA VOCÊ NADAR", margem, y, 42f, MAGENTA, negrito = true, espaco = 0.12f)
            y += 12f
            y = texto(medidor, treino.focus ?: treino.title, margem, y, 92f, BRANCO, negrito = true, larguraMax = largura)
            y += 14f

            // Metros e tempo, com o nível ao lado.
            val metros = "${TextosDoTreino.metros(treino.totalDistanceMeters)} · ~${treino.estimatedMinutes} min"
            val fimMetros = texto(medidor, metros, margem, y, 60f, CIANO, negrito = true, larguraMax = largura * 0.62f)
            val rotulo = TextosDoTreino.nivel(treino.level)
            val larguraChip = medidor.measure(rotulo, estilo(32f, FUNDO_TOPO, negrito = true)).size.width + 52f
            val topoChip = y + 4f
            arredondado(w - margem - larguraChip, topoChip, larguraChip.toFloat(), 62f, 31f, corDoNivel(treino.level))
            texto(medidor, rotulo, w - margem - larguraChip + 26f, topoChip + 14f, 32f, FUNDO_TOPO, negrito = true)
            y = maxOf(fimMetros, topoChip + 62f) + 36f

            // Fases: cada uma num cartão, com as séries e as zonas.
            val topoRodape = h - 200f
            val fases = treino.phases.filter { it.sets.isNotEmpty() || it.distanceMeters > 0 }
            val vao = 16f
            val alturaFase = if (fases.isEmpty()) {
                0f
            } else {
                ((topoRodape - 30f - y - vao * (fases.size - 1)) / fases.size).coerceAtMost(150f)
            }
            fases.forEach { fase ->
                arredondado(margem, y, largura, alturaFase, 24f, CARTAO)
                arredondado(margem + 22f, y + 22f, 10f, alturaFase - 44f, 5f, corDoBloco(fase.title))

                val x = margem + 56f
                val larguraTexto = largura - 56f - 28f
                val titulo = "${fase.title} · ${TextosDoTreino.metros(fase.distanceMeters)}"
                val tamanhoTitulo = if (alturaFase < 110f) 36f else 42f
                val fimTitulo = texto(
                    medidor, titulo, x, y + 20f, tamanhoTitulo, BRANCO,
                    negrito = true, larguraMax = larguraTexto * 0.62f
                )

                // Zonas da fase como etiquetas coloridas, à direita.
                var direita = margem + largura - 24f
                fase.sets.mapNotNull { it.zona?.takeIf(String::isNotBlank) }.distinct().reversed().forEach { zona ->
                    val lz = medidor.measure(zona, estilo(28f, FUNDO_TOPO, negrito = true)).size.width + 28f
                    arredondado(direita - lz, y + 22f, lz.toFloat(), 44f, 12f, corDaZona(zona))
                    texto(medidor, zona, direita - lz + 14f, y + 29f, 28f, FUNDO_TOPO, negrito = true)
                    direita -= lz + 10f
                }

                val series = fase.sets.map { s -> s.header.ifBlank { "${s.repsDistance}m ${s.description}".trim() } }
                if (series.isNotEmpty() && y + alturaFase - fimTitulo > 44f) {
                    texto(medidor, series.joinToString(" + "), x, fimTitulo + 8f, 32f, BRANCO_SUAVE, larguraMax = larguraTexto)
                }
                y += alturaFase + vao
            }

            // Rodapé: onde fazer e o código.
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(AZUL, MAGENTA),
                    start = Offset(margem, 0f),
                    end = Offset(margem + largura, 0f)
                ),
                topLeft = Offset(margem, topoRodape),
                size = Size(largura, h - 70f - topoRodape),
                cornerRadius = CornerRadius(28f, 28f)
            )
            texto(
                medidor, "Faça no app Natação Criativa", margem + 40f, topoRodape + 24f, 40f, BRANCO,
                negrito = true, larguraMax = largura - 80f
            )
            texto(medidor, "Código do treino: $codigo", margem + 40f, topoRodape + 80f, 36f, BRANCO, larguraMax = largura - 80f)
        }

        return imagem
    }

    private fun estilo(tamanho: Float, cor: Color, negrito: Boolean, espaco: Float = 0f) = TextStyle(
        color = cor,
        fontSize = tamanho.sp,
        fontWeight = if (negrito) FontWeight.Bold else FontWeight.Normal,
        letterSpacing = (tamanho * espaco).sp
    )

    private fun DrawScope.arredondado(x: Float, y: Float, largura: Float, altura: Float, raio: Float, cor: Color) {
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
            while (medida.size.width > larguraMax && atual > 28f) {
                atual -= 2f
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
}
