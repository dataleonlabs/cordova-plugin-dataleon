package com.dataleoncordovaplugin.cordova;

import android.app.Activity;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.util.Log;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataleonCordovaPlugin extends CordovaPlugin {

    private static final String TAG = "DataleonPlugin";

    private WebView webViewInstance;
    private FrameLayout container;
    private android.app.Dialog dialog;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        Log.d(TAG, "execute called with action: " + action);

        if ("openSession".equals(action)) {
            try {
                final String url = args.getString(0);
                final Activity activity = cordova.getActivity();

                Log.d(TAG, "Opening session with URL: " + url);

                cordova.getActivity().runOnUiThread(() -> openWebView(activity, url, callbackContext));
                return true;
            } catch (JSONException e) {
                callbackContext.error("Error: " + e.getMessage());
                Log.e(TAG, "JSONException: ", e);
                return false;
            }
        }
        else if ("closeSession".equals(action)) {
            Log.d(TAG, "Closing session from external call");
            closeWebView();
            callbackContext.success("Session closed");
            return true;
        }
        return false;
    }

    private void openWebView(Activity activity, String url, CallbackContext callbackContext) {
        Log.d(TAG, "openWebView called");

        container = new FrameLayout(activity);
        container.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        webViewInstance = new WebView(activity);
        webViewInstance.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        WebSettings settings = webViewInstance.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDomStorageEnabled(true);

        webViewInstance.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
                Log.d(TAG, "Console: " + consoleMessage.message());
                return true;
            }
        });

        webViewInstance.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "✅ Page loaded: " + url);

                // Injection du JS pour intercepter les postMessage
                String bridgeScript =
                        "window.addEventListener('message', function(e) { " +
                        "   DataleonNative.postMessage(e.data);" +
                        "});";
                webViewInstance.evaluateJavascript(bridgeScript, null);
            }
        });

        // Add JS interface
        webViewInstance.addJavascriptInterface(new DataleonJSInterface(callbackContext), "DataleonNative");

        Log.d(TAG, "Loading URL in WebView: " + url);
        webViewInstance.loadUrl(url);

        container.addView(webViewInstance);

        // Create dialog (fullscreen)
        this.dialog = new android.app.Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.dialog.setContentView(container);
        this.dialog.setCancelable(false);
        this.dialog.show();

        Log.d(TAG, "✅ Dialog with WebView should now be visible");
    }


    // JS bridge
    private class DataleonJSInterface {
        private CallbackContext callbackContext;
        DataleonJSInterface(CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
        }
        @JavascriptInterface
        public void postMessage(String message) {
            Log.d(TAG, "Message from JS: " + message);
            handleMessage(message, callbackContext);
        }
    }

    private void handleMessage(String message, CallbackContext callbackContext) {
        Log.d(TAG, "Session ended with message: " + message);
        if ("CANCELED".equals(message)) {
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
        Log.d(TAG, "Closing WebView/Dialog");

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
                    Log.d(TAG, "✅ Dialog closed");
                }
            });
        }
    }
}
