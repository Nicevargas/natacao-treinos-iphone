package com.example.data.auth

import com.example.data.agoraEmSegundos
import com.example.data.Resultado
import com.example.data.supabase.ehDemora
import com.example.data.supabase.ehSemRede

/**
 * Esqueci minha senha, sem sair do app:
 *
 *   1. enviarCodigo     o Supabase manda um e-mail com um código
 *   2. verificarCodigo  o código vira uma sessão de recuperação
 *   3. trocarSenha      com essa sessão, grava a senha nova
 *
 * O código só vale uma vez. Por isso quem chama guarda a sessão do passo 2:
 * se o passo 3 recusar a senha (fraca ou igual à antiga), dá para tentar
 * outra sem pedir um código novo.
 */
class RecuperacaoDeSenha(
    private val api: AuthApi,
    private val agoraSegundos: () -> Long = { agoraEmSegundos() }
) {

    suspend fun enviarCodigo(email: String): Resultado<Unit> = protegido {
        val r = api.recover(RecoverBody(email.trim().lowercase()))
        if (r.sucesso) {
            Resultado.Ok(Unit)
        } else {
            Resultado.Falha(MensagensAuth.deErroDeAuth(r.codigo, r.erro))
        }
    }

    suspend fun verificarCodigo(email: String, codigo: String): Resultado<Sessao> = protegido {
        val r = api.verify(VerifyOtpBody(type = "recovery", email = email.trim().lowercase(), token = codigo.trim()))
        val sessao = r.corpo?.paraSessao(agoraSegundos())
        when {
            r.sucesso && sessao != null -> Resultado.Ok(sessao)
            r.sucesso -> Resultado.Falha("Não foi possível validar o código. Peça um novo.")
            else -> Resultado.Falha(MensagensAuth.deErroDeAuth(r.codigo, r.erro))
        }
    }

    suspend fun trocarSenha(sessao: Sessao, novaSenha: String): Resultado<Unit> = protegido {
        val r = api.updateUser("Bearer ${sessao.accessToken}", UpdatePasswordBody(novaSenha))
        if (r.sucesso) {
            Resultado.Ok(Unit)
        } else {
            Resultado.Falha(MensagensAuth.deErroDeAuth(r.codigo, r.erro))
        }
    }

    private suspend fun <T> protegido(bloco: suspend () -> Resultado<T>): Resultado<T> =
        try {
            bloco()
        } catch (e: Exception) {
            when {
                e.ehDemora() -> Resultado.Falha(MensagensAuth.DEMOROU)
                e.ehSemRede() -> Resultado.Falha(MensagensAuth.SEM_REDE)
                else -> Resultado.Falha("Não foi possível concluir. Tente de novo.")
            }
        }
}
