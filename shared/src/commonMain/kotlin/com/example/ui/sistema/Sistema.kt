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
