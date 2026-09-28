import SwiftUI
import UIKit
import Shared

/// Mostra no SwiftUI a tela que o Kotlin monta (MainViewController).
struct TelaDoApp: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
