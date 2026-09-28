package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.recursos.Res
import com.example.recursos.natacao_criativa_logo

/** Logo "Natação Criativa Treinos" no topo. Embarcado no app: aparece mesmo sem internet. O ícone do app é outro (natacao_criativa_icone). */
@Composable
fun AquagendaLogo(
    modifier: Modifier = Modifier,
    height: Dp = 36.dp
) {
    Image(
        painter = painterResource(Res.drawable.natacao_criativa_logo),
        contentDescription = "Natação Criativa Treinos",
        contentScale = ContentScale.Fit,
        alignment = Alignment.CenterStart,
        modifier = modifier
            .height(height)
            .testTag("logo")
    )
}
