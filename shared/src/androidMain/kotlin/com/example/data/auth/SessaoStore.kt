package com.example.data.auth

import com.example.data.Guardados
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sessão salva no aparelho, para não pedir login toda vez que o app abre.
 *
 * Guardada no espaço privado do app, fora do backup na nuvem e da transferência
 * entre aparelhos (res/xml/backup_rules.xml e data_extraction_rules.xml), para o
 * token não sair do celular.
 */
class SessaoStore(private val guardados: Guardados) {

    private val _sessao = MutableStateFlow(ler())
    val sessao: StateFlow<Sessao?> = _sessao.asStateFlow()

    fun atual(): Sessao? = _sessao.value

    @Synchronized
    fun salvar(sessao: Sessao) {
        guardados.salvarTexto(ACESSO, sessao.accessToken)
        guardados.salvarTexto(RENOVACAO, sessao.refreshToken)
        guardados.salvarNumero(EXPIRA_EM, sessao.expiraEm)
        guardados.salvarTexto(USUARIO, sessao.userId)
        guardados.salvarTexto(EMAIL, sessao.email)
        _sessao.value = sessao
    }

    @Synchronized
    fun limpar() {
        guardados.limparTudo()
        _sessao.value = null
    }

    private fun ler(): Sessao? {
        val acesso = guardados.texto(ACESSO) ?: return null
        val renovacao = guardados.texto(RENOVACAO) ?: return null
        val usuario = guardados.texto(USUARIO) ?: return null
        return Sessao(
            accessToken = acesso,
            refreshToken = renovacao,
            expiraEm = guardados.numero(EXPIRA_EM, 0L),
            userId = usuario,
            email = guardados.texto(EMAIL).orEmpty()
        )
    }

    companion object {
        /** Mesmo nome de antes: quem já está logado continua logado ao atualizar. */
        const val ARQUIVO = "aquagenda_sessao"
        private const val ACESSO = "access_token"
        private const val RENOVACAO = "refresh_token"
        private const val EXPIRA_EM = "expira_em"
        private const val USUARIO = "user_id"
        private const val EMAIL = "email"
    }
}
