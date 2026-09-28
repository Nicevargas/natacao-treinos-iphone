package com.example.ui.sistema

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

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

/** No iPhone, a autorização para notificar é pedida uma vez; o sistema lembra a resposta. */
@Composable
actual fun lembrarPedidoDeAviso(): () -> Unit = {
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        UNAuthorizationOptionAlert or UNAuthorizationOptionSound
    ) { _, _ -> }
}
