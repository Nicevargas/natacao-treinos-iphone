package com.example.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


// ---- Corpos das chamadas ao Supabase Auth (GoTrue) ----

@Serializable
data class SignUpBody(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    // Vira raw_user_meta_data: full_name e training_level do perfil.
    @SerialName("data") val data: Map<String, String>
)

@Serializable
data class PasswordGrantBody(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class RefreshGrantBody(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class RecoverBody(
    @SerialName("email") val email: String
)

@Serializable
data class VerifyOtpBody(
    @SerialName("type") val type: String,
    @SerialName("email") val email: String,
    @SerialName("token") val token: String
)

@Serializable
data class UpdatePasswordBody(
    @SerialName("password") val password: String
)

@Serializable
data class AuthUserDto(
    @SerialName("id") val id: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("identities") val identities: List<JsonElement>? = null
)

/**
 * Resposta de login, renovação e cadastro. No cadastro que exige confirmação
 * por e-mail não vem sessão: o usuário vem solto no topo (id, email, identities).
 */
@Serializable
data class SessionDto(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    @SerialName("expires_at") val expiresAt: Long? = null,
    @SerialName("user") val user: AuthUserDto? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("identities") val identities: List<JsonElement>? = null
)

@Serializable
data class AuthErrorDto(
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("msg") val msg: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("error") val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null
)

/** Erro do PostgREST: {"code":"42501","message":"..."}. */
@Serializable
data class ApiErrorDto(
    @SerialName("code") val code: String? = null,
    @SerialName("message") val message: String? = null
)

// ---- Domínio ----

data class Sessao(
    val accessToken: String,
    val refreshToken: String,
    val expiraEm: Long, // epoch em segundos
    val userId: String,
    val email: String
)

sealed interface ResultadoAuth {
    data object Entrou : ResultadoAuth
    data object ConfirmarEmail : ResultadoAuth
    data class Erro(val mensagem: String) : ResultadoAuth
}

fun SessionDto.paraSessao(agoraSegundos: Long): Sessao? {
    val acesso = accessToken ?: return null
    val renovacao = refreshToken ?: return null
    val usuario = user?.id ?: return null
    return Sessao(
        accessToken = acesso,
        refreshToken = renovacao,
        expiraEm = expiresAt ?: (agoraSegundos + (expiresIn ?: 3600)),
        userId = usuario,
        email = user.email.orEmpty()
    )
}
