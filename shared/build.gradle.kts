import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Código comum do app: telas, ViewModels, regras e acesso ao Supabase.
// Hoje só com o alvo Android; o iPhone entra como mais um alvo deste módulo.
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
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

  sourceSets {
    androidMain {
      kotlin.srcDir(gerarConfiguracao)
      dependencies {
        // api: o módulo do app (MainActivity e testes de tela) usa as mesmas bibliotecas.
        api(project.dependencies.platform(libs.androidx.compose.bom))
        api(libs.androidx.activity.compose)
        api(libs.androidx.compose.material.icons.core)
        api(libs.androidx.compose.material.icons.extended)
        api(libs.androidx.compose.material3)
        api(libs.androidx.compose.ui)
        api(libs.androidx.compose.ui.graphics)
        api(libs.androidx.compose.ui.tooling.preview)
        api(libs.androidx.core.ktx)
        api(libs.androidx.lifecycle.runtime.compose)
        api(libs.androidx.lifecycle.runtime.ktx)
        api(libs.androidx.lifecycle.viewmodel.compose)
        api(libs.coil.compose)
        api(libs.converter.moshi)
        api(libs.kotlinx.coroutines.android)
        api(libs.kotlinx.coroutines.core)
        api(libs.logging.interceptor)
        api(libs.moshi.kotlin)
        api(libs.okhttp)
        api(libs.retrofit)
      }
    }
    getByName("androidHostTest").dependencies {
      implementation(libs.junit)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.mockwebserver)
    }
  }
}

dependencies {
  add("kspAndroid", libs.moshi.kotlin.codegen)
}
