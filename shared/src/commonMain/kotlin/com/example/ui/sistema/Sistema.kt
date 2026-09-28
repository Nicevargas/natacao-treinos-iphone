package com.example.ui.sistema

import androidx.compose.runtime.Composable

/**
 * O que a tela pede ao sistema e que cada um faz do seu jeito.
 */

/**
 * O botão "voltar" do Android (ou o gesto de voltar). O iPhone não tem esse
 * botão: lá, voltar é pelo X ou pelas setas que cada tela já mostra.
 */
@Composable
expect fun AoVoltar(ativo: Boolean = true, aoVoltar: () -> Unit)

/**
 * Deixa a tela acesa enquanto este trecho estiver visível. Na piscina, com a
 * mão molhada, a tela não pode apagar no meio da série.
 */
@Composable
expect fun ManterTelaAcesa()

/**
 * Prepara o pedido de permissão para o aviso de treino e devolve quem o faz. No
 * Android 13+ e no iPhone, notificar exige a pessoa autorizar; o pedido sai na
 * hora em que ela escolhe o dia, que é quando faz sentido perguntar.
 */
@Composable
expect fun lembrarPedidoDeAviso(): () -> Unit
