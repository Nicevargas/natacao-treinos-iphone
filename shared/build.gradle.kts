import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Código comum do app: telas, ViewModels, regras e acesso ao Supabase.
// Hoje só com o alvo Android; o iPhone entra como mais um alvo deste módulo.
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.compose.multiplatform)
}

// ------------------------------------------------------------------------------
// Chaves do Supabase. Antes vinham do BuildConfig (plugin de secrets), que não
// existe em módulo multiplataforma nem no iPhone. Agora viram um objeto Kotlin
// gerado no build, lido do .env da raiz; no CI, das variáveis de ambiente. Sem
// nenhum dos dois vale o .env.example (placeholder: o app avisa que falta).
// ------------------------------------------------------------------------------
fun lerEnv(conteudo: String?): Map<String, String> =
  conteudo.orEmpty().lines()
    .map { it.trim() }
    .filter { it.isNotEmpty() && !it.startsWith("#") && "=" in it }
    .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim() }

// Lido na configuração (o cache de configuração registra o .env como entrada);
// a tarefa só recebe os textos prontos.
val envLocal = lerEnv(providers.fileContents(rootProject.layout.projectDirectory.file(".env")).asText.orNull)
val envExemplo = lerEnv(providers.fileContents(rootProject.layout.projectDirectory.file(".env.example")).asText.orNull)

fun chave(nome: String): String =
  providers.environmentVariable(nome).orNull?.takeIf { it.isNotBlank() }
    ?: envLocal[nome]?.takeIf { it.isNotBlank() }
    ?: envExemplo[nome].orEmpty()

val configUrl = chave("SUPABASE_URL")
val configChaveAnon = chave("SUPABASE_ANON_KEY")

val gerarConfiguracao = tasks.register("gerarConfiguracao") {
  // Cópias locais: a ação não pode guardar referência ao script (cache de configuração).
  val url = configUrl
  val chaveAnon = configChaveAnon
  val destino = layout.buildDirectory.dir("generated/configuracao/kotlin")
  inputs.property("url", url)
  inputs.property("chaveAnon", chaveAnon)
  outputs.dir(destino)
  doLast {
    // Texto como literal Kotlin: escapa barra, aspas e cifrão.
    fun literal(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\$", "\\\$") + "\""
    val pasta = destino.get().asFile.resolve("com/example/data/supabase").apply { mkdirs() }
    pasta.resolve("Configuracao.kt").writeText(
      listOf(
        "// Gerado pelo build (shared/build.gradle.kts, tarefa gerarConfiguracao). Não editar.",
        "package com.example.data.supabase",
        "",
        "internal object Configuracao {",
        "    const val SUPABASE_URL: String = ${literal(url)}",
        "    const val SUPABASE_ANON_KEY: String = ${literal(chaveAnon)}",
        "}",
        ""
      ).joinToString("\n")
    )
  }
}

kotlin {
  android {
    namespace = "com.example.shared"
    compileSdk { version = release(36) { minorApiLevel = 1 } }
    minSdk = 24
    // O logo e o ícone da notificação são recursos Android deste módulo (R.drawable).
    androidResources { enable = true }
    withHostTest {}
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
  }

  // iPhone: aparelho de verdade e simulador. Só compilam num Mac (fase 5, no CI);
  // no Windows o Kotlin desliga estes alvos e segue com o Android.
  listOf(iosArm64(), iosSimulatorArm64()).forEach { alvo ->
    alvo.binaries.framework {
      baseName = "Shared"
      isStatic = true
    }
  }

  sourceSets {
    commonMain {
      kotlin.srcDir(gerarConfiguracao)
      dependencies {
        // Telas: Compose Multiplatform. No Android ele usa por baixo o mesmo
        // Jetpack Compose de antes; no iPhone, o motor de desenho próprio.
        api(compose.runtime)
        api(compose.foundation)
        api(compose.material3)
        api(compose.ui)
        api(compose.components.resources)
        api(libs.mp.icones.extended)
        api(libs.mp.lifecycle.viewmodel.compose)
        api(libs.mp.lifecycle.runtime.compose)
        api(libs.coil3.compose)
        api(libs.coil3.network.ktor3)
        api(libs.ktor.client.core)
        api(libs.ktor.client.content.negotiation)
        api(libs.ktor.serialization.kotlinx.json)
        api(libs.ktor.client.logging)
        api(libs.kotlinx.serialization.json)
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.coroutines.core)
        // Travas que funcionam também no iPhone (o synchronized é só do Java).
        api(libs.atomicfu)
      }
    }
    androidMain {
      dependencies {
        // Só do Android: a Activity, o motor de rede OkHttp e as corrotinas na thread principal.
        api(libs.androidx.activity.compose)
        api(libs.androidx.core.ktx)
        api(libs.ktor.client.okhttp)
        api(libs.kotlinx.coroutines.android)
      }
    }
    iosMain {
      dependencies {
        // Motor de rede do próprio iPhone.
        implementation(libs.ktor.client.darwin)
      }
    }
    getByName("androidHostTest").dependencies {
      implementation(libs.junit)
      implementation(libs.kotlinx.coroutines.test)
      // Servidor de mentira do Ktor: funciona também no iPhone, ao contrário do MockWebServer.
      implementation(libs.ktor.client.mock)
    }
  }
}
