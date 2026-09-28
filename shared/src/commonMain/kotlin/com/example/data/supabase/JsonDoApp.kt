package com.example.data.supabase

import kotlinx.serialization.json.Json

/**
 * Um único JSON para todo o app, com o mesmo comportamento que o Moshi tinha:
 *
 * - campo novo vindo do servidor não quebra o app (ignoreUnknownKeys);
 * - campo nulo não é enviado, para o banco aplicar o valor padrão (explicitNulls = false);
 * - o resto é enviado mesmo quando é igual ao padrão (encodeDefaults).
 */
val JsonDoApp: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
    isLenient = true
}
