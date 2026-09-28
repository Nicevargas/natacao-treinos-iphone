package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AquaOutline
import com.example.ui.theme.AquaPrimary

/**
 * O botão com contorno do app ("Voltar", "Editar perfil"), com borda cinza e
 * texto azul.
 *
 * O visual é fixado aqui de propósito. O Material 3 mudou o padrão desse botão
 * (borda quase invisível e texto cinza), e com isso o "Voltar" sumia na tela.
 * Todo botão com contorno usa este, para uma atualização da biblioteca não mudar
 * o app sem ninguém perceber.
 */
@Composable
fun BotaoContornado(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AquaPrimary),
        border = BorderStroke(1.dp, if (enabled) AquaOutline else AquaOutline.copy(alpha = 0.3f)),
        contentPadding = contentPadding,
        content = content
    )
}
