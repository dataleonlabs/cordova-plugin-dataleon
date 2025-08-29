package com.dataleon.cordova;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataleonCordovaPlugin extends CordovaPlugin {

    private WebView webViewInstance;
    private FrameLayout container;
    private android.app.Dialog dialog; // Ajouter ce champ

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        if (action.equals("openSession")) {
            try {
                final String url = args.getString(0);
                final Activity activity = cordova.getActivity();
                cordova.getActivity().runOnUiThread(() -> openWebView(activity, url, callbackContext));
                return true;
            } catch (JSONException e) {
                callbackContext.error("Error: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    private void openWebView(Activity activity, String url, CallbackContext callbackContext) {
        container = new FrameLayout(activity);

        webViewInstance = new WebView(activity);
        WebSettings settings = webViewInstance.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDomStorageEnabled(true);

        webViewInstance.setWebChromeClient(new WebChromeClient() {
            // Gérer les permissions caméra/micro ici si besoin
        });

        // Utiliser une classe dédiée pour la JS interface
        webViewInstance.addJavascriptInterface(new DataleonJSInterface(callbackContext), "DataleonNative");

        webViewInstance.loadUrl(url);

        container.addView(webViewInstance);

        // Afficher dans une Dialog pour ne pas casser l'UI principale
        android.app.Dialog dialog = new android.app.Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(container);
        dialog.setCancelable(false);
        dialog.show();

        // Stocker la dialog pour pouvoir la fermer plus tard
        this.dialog = dialog;
    }

    // Classe interne pour la JS interface
    private class DataleonJSInterface {
        private CallbackContext callbackContext;
        DataleonJSInterface(CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
        }
        @JavascriptInterface
        public void postMessage(String message) {
            handleMessage(message, callbackContext);
        }
    }

    private void handleMessage(String message, CallbackContext callbackContext) {
        if ("FINISHED".equals(message) || "CANCELED".equals(message)) {
            callbackContext.success(message);
            closeWebView();
        } else {
            try {
                JSONObject json = new JSONObject(message);
                callbackContext.success(json);
            } catch (Exception e) {
                callbackContext.success(message);
            }
        }
    }

    private void closeWebView() {
        if (webViewInstance != null && container != null) {
            cordova.getActivity().runOnUiThread(() -> {
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    webViewInstance.removeJavascriptInterface("DataleonNative");
                }
                container.removeView(webViewInstance);
                webViewInstance.destroy();
                webViewInstance = null;
                container = null;
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
            });
        }
    }
}
