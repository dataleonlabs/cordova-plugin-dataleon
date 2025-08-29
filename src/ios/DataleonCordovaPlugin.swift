import Foundation
import WebKit

@objc(DataleonCordovaPlugin) class DataleonCordovaPlugin: CDVPlugin, WKScriptMessageHandler, WKNavigationDelegate {
    var webViewController: UIViewController?
    var callbackId: String?

    @objc(openSession:)
    func openSession(command: CDVInvokedUrlCommand) {
        guard let urlString = command.arguments.first as? String,
              let url = URL(string: urlString) else {
            let pluginResult = CDVPluginResult(status: .error, messageAs: "Invalid URL")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        self.callbackId = command.callbackId

        DispatchQueue.main.async {
            // Configurer la WebView avec support caméra/micro
            let contentController = WKUserContentController()
            contentController.add(self, name: "dataleonCallback")

            let config = WKWebViewConfiguration()
            config.userContentController = contentController
            config.allowsInlineMediaPlayback = true
            config.mediaTypesRequiringUserActionForPlayback = []

            let wkWebView = WKWebView(frame: UIScreen.main.bounds, configuration: config)
            wkWebView.navigationDelegate = self
            wkWebView.load(URLRequest(url: url))

            // Présenter la WebView dans un viewController modal
            let vc = UIViewController()
            vc.view = wkWebView
            vc.modalPresentationStyle = .fullScreen

            if let presenter = self.viewController {
                presenter.present(vc, animated: true, completion: nil)
                self.webViewController = vc
            }
        }
    }

    // Recevoir les messages JS via window.webkit.messageHandlers.dataleonCallback.postMessage(...)
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard let callbackId = self.callbackId else { return }

        if let messageBody = message.body as? String {
            // Messages simples : FINISHED / CANCELED
            if ["FINISHED", "CANCELED"].contains(messageBody) {
                let pluginResult = CDVPluginResult(status: .ok, messageAs: messageBody)
                self.commandDelegate.send(pluginResult, callbackId: callbackId)
                dismissWebView()
            }
        } else if let dict = message.body as? [String: Any], let type = dict["type"] as? String {
            // Messages avec payload personnalisé
            let pluginResult = CDVPluginResult(status: .ok, messageAs: dict)
            self.commandDelegate.send(pluginResult, callbackId: callbackId)
        }
    }

    private func dismissWebView() {
        DispatchQueue.main.async {
            if let wkWebView = self.webViewController?.view as? WKWebView {
                wkWebView.configuration.userContentController.removeScriptMessageHandler(forName: "dataleonCallback")
            }
            self.webViewController?.dismiss(animated: true, completion: nil)
            self.webViewController = nil
            self.callbackId = nil
        }
    }
}
