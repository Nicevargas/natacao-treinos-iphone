package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.data.Registro
import com.example.data.auth.AuthRepository
import com.example.data.auth.SessaoStore
import com.example.data.compartilhar.LinkDeTreino
import com.example.data.guardadosDe
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Antes de qualquer chamada ao Supabase: é daqui que sai o token do usuário.
        // O app liga as peças do aparelho: onde guardar a sessão e para onde vão os avisos.
        AuthRepository.init(guardadosDe(applicationContext, SessaoStore.ARQUIVO))
        Registro.saida = Registro.Saida { grave, marca, mensagem, causa ->
            if (grave) Log.e(marca, mensagem, causa) else Log.w(marca, mensagem, causa)
        }
        // Ícones escuros na barra de status sempre: o app é claro mesmo com o celular no modo escuro.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        // Link de treino compartilhado (natacaocriativa://treino/<código>) que abriu o app.
        LinkDeTreino.receber(intent?.data?.toString())
        setContent {
            MyApplicationTheme {
                AquagendaRaiz(pecas = remember { pecasDoAndroid(this) })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        LinkDeTreino.receber(intent?.data?.toString())
    }
}
