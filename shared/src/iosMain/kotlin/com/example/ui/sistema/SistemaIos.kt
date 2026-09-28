package com.example.ui.sistema

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

/** O iPhone não tem botão de voltar do sistema: as telas já trazem o X e as setas. */
@Composable
actual fun AoVoltar(ativo: Boolean, aoVoltar: () -> Unit) = Unit

/** No iPhone, desligar o "bloqueio automático" enquanto a tela estiver aberta. */
@Composable
actual fun ManterTelaAcesa() {
    DisposableEffect(Unit) {
        UIApplication.sharedApplication.idleTimerDisabled = true
        onDispose { UIApplication.sharedApplication.idleTimerDisabled = false }
    }
}
