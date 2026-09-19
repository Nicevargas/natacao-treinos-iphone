package com.example.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AquaPrimary

/**
 * Endereço oficial, na hospedagem do domínio natacaocriativa.com.br. É o mesmo
 * informado na Google Play. O texto-fonte está em docs/privacidade.html, neste repositório.
 */
const val URL_POLITICA_DE_PRIVACIDADE = "https://treinos.natacaocriativa.com.br/privacidade.html"

/** Abre a política no navegador. Aparece na tela de entrar e no cartão da conta. */
@Composable
fun LinkDaPolitica(modifier: Modifier = Modifier) {
    val navegador = LocalUriHandler.current
    TextButton(
        // Sem navegador instalado o openUri lança exceção; o toque só não faz nada.
        onClick = { runCatching { navegador.openUri(URL_POLITICA_DE_PRIVACIDADE) } },
        modifier = modifier.testTag("politica_de_privacidade")
    ) {
        Text(
            text = "Política de privacidade",
            color = AquaPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline
        )
    }
}
