import UIKit
import XCTest

/// Percorre as telas do app no simulador e fotografa cada uma, para conferir o
/// visual do iPhone sem ter um Mac. Entra com a conta de teste (segredos
/// TESTE_EMAIL e TESTE_SENHA do GitHub) e sai de tudo sem salvar nada: não
/// responde o PAR-Q, não entra no ranking e não grava treino.
///
/// As etiquetas ("campo_email", "nav_tab_home"...) são os testTag do Compose.
final class PercorrerTelas: XCTestCase {

    private let app = XCUIApplication()
    private var numero = 0
    private lazy var pasta: URL = {
        let caminho = ProcessInfo.processInfo.environment["CAPTURAS_DIR"] ?? NSTemporaryDirectory()
        let url = URL(fileURLWithPath: caminho, isDirectory: true)
        try? FileManager.default.createDirectory(at: url, withIntermediateDirectories: true)
        return url
    }()

    override func setUp() {
        continueAfterFailure = true
        // Avisos do sistema (salvar senha, notificações): recusar e seguir.
        addUIInterruptionMonitor(withDescription: "avisos do sistema") { alerta in
            for botao in ["Not Now", "Agora Não", "Don’t Allow", "Não Permitir", "Cancel", "Cancelar"] {
                if alerta.buttons[botao].exists { alerta.buttons[botao].tap(); return true }
            }
            return false
        }
    }

    func testPercorrerTelas() throws {
        let ambiente = ProcessInfo.processInfo.environment
        let email = ambiente["TESTE_EMAIL"] ?? ""
        let senha = ambiente["TESTE_SENHA"] ?? ""

        app.launch()
        fotografar("entrar")

        if item("campo_email").waitForExistence(timeout: 15) {
            guard !email.isEmpty, !senha.isEmpty else {
                throw XCTSkip("Sem TESTE_EMAIL/TESTE_SENHA: só a tela de entrar.")
            }
            digitar(email, em: "campo_email")
            digitar(senha, em: "campo_senha")
            esconderTeclado()
            tocar("botao_enviar")
        }

        guard item("nav_tab_home").waitForExistence(timeout: 30) else {
            fotografar("login-falhou")
            XCTFail("Não chegou na tela inicial depois de entrar.")
            return
        }
        esperar(4)  // o treino do dia e o progresso chegam do Supabase
        fotografar("home")
        rolarEFotografar("home")

        for (aba, nome) in [("nav_tab_plan", "plano"),
                            ("nav_tab_workouts", "treinos"),
                            ("nav_tab_my_workouts", "meus-treinos")] {
            tocar(aba)
            esperar(3)
            fotografar(nome)
            rolarEFotografar(nome)
        }

        // Perfil: o cartão da conta mostra nome e e-mail. As capturas são
        // públicas, então ele sai coberto e a árvore não é gravada.
        tocar("nav_tab_profile")
        esperar(3)
        fotografar("perfil", cobrir: ["conta_card"], arvore: false)
        app.swipeUp()
        esperar(1)
        fotografar("perfil-rolado", cobrir: ["conta_card"], arvore: false)

        // Ranking: só conferir que abre. Sem foto nem árvore, porque ele mostra
        // nomes de alunos e as capturas vão para um repositório público.
        if procurarRolando("abrir_ranking") {
            tocar("abrir_ranking")
            let abriu = item("participar_do_ranking").waitForExistence(timeout: 10)
                || item("sair_do_ranking").exists
            // Só as etiquetas, nunca os textos (que trazem nomes de alunos).
            let etiquetas = app.descendants(matching: .any).allElementsBoundByIndex
                .map(\.identifier).filter { !$0.isEmpty }
            anotar("ranking", (abriu ? "O ranking abriu." : "O ranking NÃO abriu.")
                + "
Etiquetas na tela: " + etiquetas.joined(separator: ", "))
            XCTAssertTrue(abriu, "O ranking não abriu")
            app.terminate()
            app.launch()
            _ = item("nav_tab_home").waitForExistence(timeout: 20)
        }

        // Treino: começar e sair sem salvar.
        if !item("nav_tab_home").exists { app.terminate(); app.launch() }
        tocar("nav_tab_home")
        esperar(2)
        if procurarRolando("start_workout_cta") {
            tocar("start_workout_cta")
            esperar(2)
            if item("parq_fechar").waitForExistence(timeout: 3) {
                fotografar("parq")
                rolarEFotografar("parq")
                tocar("parq_fechar")
            } else if item("tempo_de_treino").waitForExistence(timeout: 5) {
                fotografar("executando")
                tocar("serie_atual")
                esperar(1)
                fotografar("executando-toque")
                tocar("close_execution_button")
                esperar(1)
                fotografar("sair-do-treino")
                tocar("sair_sem_salvar")
            }
        }
        esperar(1)
        fotografar("fim")
    }

    // MARK: - Ajudantes

    private func item(_ etiqueta: String) -> XCUIElement {
        app.descendants(matching: .any)[etiqueta].firstMatch
    }

    private func tocar(_ etiqueta: String) {
        let alvo = item(etiqueta)
        guard alvo.waitForExistence(timeout: 8) else {
            XCTFail("Não achei \(etiqueta)")
            anotarArvore("faltou-\(etiqueta)")
            return
        }
        alvo.tap()
    }

    private func digitar(_ texto: String, em etiqueta: String) {
        let campo = item(etiqueta)
        campo.tap()
        esperar(1)
        campo.typeText(texto)
    }

    private func esconderTeclado() {
        if app.keyboards.buttons["Return"].exists { app.keyboards.buttons["Return"].tap() }
        else if app.keyboards.buttons["return"].exists { app.keyboards.buttons["return"].tap() }
        esperar(1)
    }

    private func procurarRolando(_ etiqueta: String) -> Bool {
        for _ in 0..<8 {
            if visivel(item(etiqueta)) { return true }
            app.swipeUp()
            esperar(0.5)
        }
        anotarArvore("faltou-\(etiqueta)")
        return false
    }

    /// No Compose tudo é uma superfície só: "isHittable" diz sim mesmo quando o
    /// elemento está atrás da barra de abas. Visível é estar acima dela.
    private func visivel(_ alvo: XCUIElement) -> Bool {
        guard alvo.exists, alvo.isHittable else { return false }
        let barra = item("nav_tab_home")
        let limite = barra.exists ? barra.frame.minY - 8 : app.frame.maxY
        return alvo.frame.minY > app.frame.minY + 80 && alvo.frame.maxY < limite
    }

    private func rolarEFotografar(_ nome: String) {
        app.swipeUp()
        esperar(1)
        fotografar("\(nome)-rolado")
        app.swipeDown(); app.swipeDown()
        esperar(0.5)
    }

    private func esperar(_ segundos: TimeInterval) {
        RunLoop.current.run(until: Date().addingTimeInterval(segundos))
    }

    /// Guarda a tela como PNG na pasta das capturas, e também no relatório do teste.
    private func fotografar(_ nome: String, cobrir: [String] = [], arvore: Bool = true) {
        numero += 1
        var imagem = XCUIScreen.main.screenshot().image
        let areas = cobrir.map(item).filter(\.exists).map(\.frame)
        if !areas.isEmpty { imagem = cobrirAreas(imagem, areas) }
        let arquivo = pasta.appendingPathComponent(String(format: "%02d-%@.png", 10 + numero, nome))
        try? imagem.pngData()?.write(to: arquivo)
        if arvore { anotarArvore(nome) }
    }

    /// Pinta uma tarja cinza por cima das áreas (em pontos da tela).
    private func cobrirAreas(_ imagem: UIImage, _ areas: [CGRect]) -> UIImage {
        let formato = UIGraphicsImageRendererFormat()
        formato.scale = imagem.scale
        return UIGraphicsImageRenderer(size: imagem.size, format: formato).image { _ in
            imagem.draw(at: .zero)
            UIColor.systemGray3.setFill()
            areas.forEach { UIRectFill($0) }
        }
    }

    private func anotar(_ nome: String, _ texto: String) {
        numero += 1
        let arquivo = pasta.appendingPathComponent(String(format: "%02d-%@.txt", 10 + numero, nome))
        try? texto.write(to: arquivo, atomically: true, encoding: .utf8)
    }

    /// A árvore de acessibilidade da tela: mostra as etiquetas que o teste enxerga.
    private func anotarArvore(_ nome: String) {
        let arquivo = pasta.appendingPathComponent(String(format: "%02d-%@.txt", 10 + numero, nome))
        try? app.debugDescription.write(to: arquivo, atomically: true, encoding: .utf8)
    }
}
