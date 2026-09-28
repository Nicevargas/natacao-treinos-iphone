package com.example.data

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * A hora de agora, em milissegundos desde 1970.
 *
 * O System.currentTimeMillis() é do Java e não existe no iPhone; o relógio do
 * Kotlin existe nos dois. Para medir duração (o cronômetro do treino), use o
 * relógio monotônico, que não muda quando o celular acerta a hora.
 */
@OptIn(ExperimentalTime::class)
fun agoraEmMillis(): Long = Clock.System.now().toEpochMilliseconds()

/** A hora de agora em segundos, como o Supabase conta a validade do token. */
fun agoraEmSegundos(): Long = agoraEmMillis() / 1000
