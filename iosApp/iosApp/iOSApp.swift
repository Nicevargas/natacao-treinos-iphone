import SwiftUI
import Shared

/// O app do iPhone. Toda a interface vem do código comum (Kotlin + Compose), o
/// mesmo do Android; aqui só se abre a janela e se repassam os links.
@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            TelaDoApp()
                // O Compose cuida das bordas (barra de status, teclado e a barrinha de baixo),
                // como no Android com edge-to-edge.
                .ignoresSafeArea()
                // Link de treino compartilhado: natacaocriativa://treino/<código>.
                .onOpenURL { url in
                    MainViewControllerKt.receberLink(url: url.absoluteString)
                }
        }
    }
}
