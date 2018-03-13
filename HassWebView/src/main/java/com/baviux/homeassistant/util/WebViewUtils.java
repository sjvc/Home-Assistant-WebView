package com.baviux.homeassistant.util;

import android.content.Context;
import android.util.Base64;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;

public class WebViewUtils {

    public static void injectJavascriptFile(Context context, WebView view, int rawResId) {
        InputStream input;
        try {
            input = context.getResources().openRawResource(rawResId);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            // String-ify the script byte-array using BASE64 encoding !!!
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.post(new Runnable() {
                @Override
                public void run() {
                    view.loadUrl("javascript:(function() {" +
                            // If it's already injected -> do nothing
                            "if(typeof android_injected_res_" + rawResId + " != 'undefined') return;" +
                            // Else -> inject javascript
                            "var parent = document.getElementsByTagName('head').item(0);" +
                            "var script = document.createElement('script');" +
                            "script.type = 'text/javascript';" +
                            // Tell the browser to BASE64-decode the string into your script !!!
                            "script.innerHTML = window.atob('" + encoded + "');" +
                            // Create var to ensure we won't inject the same code twice
                            "script.innerHTML += 'var android_injected_res_" + rawResId + " = true;';" +
                            "parent.appendChild(script)" +
                            "})()");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void execJavascriptFile(Context context, WebView view, int rawResId){
        execJavascript(context, view,  FileUtils.getRawFileContents(context, rawResId));
    }

    public static void execJavascript(Context context, WebView view, String javascript){
        view.post(new Runnable() {
            @Override
            public void run() {
                view.loadUrl("javascript:(function() { " + javascript + " })();");
            }
        });
    }

}
