package com.example.ui.sistema

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
actual fun AoVoltar(ativo: Boolean, aoVoltar: () -> Unit) {
    BackHandler(enabled = ativo, onBack = aoVoltar)
}

@Composable
actual fun ManterTelaAcesa() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
