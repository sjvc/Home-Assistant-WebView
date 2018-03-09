package com.baviux.homeassistant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.baviux.homeassistant.util.WebViewUtils;

public class HassWebView extends WebView{
    private final static String TAG = "HassWebView";

    public interface IEventHandler{
        public void onFinish();
    }

    class JsEventHandler {
        @JavascriptInterface
        public void onFinish(){
            if (mEventHandler != null){
                mEventHandler.onFinish();
            }
        }
    }

    private IEventHandler mEventHandler;
    private boolean mPageLoadFinished = false;
    private boolean mHideAdminMenuItems = true;
    private boolean mAdjustBackKeyBehavior = true;

    public HassWebView(Context context) {
        super(context);

        initializeWebView();
    }

    public HassWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initializeWebView();
    }

    public HassWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initializeWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        setWebViewClient(mWebViewClient);
        setWebChromeClient(mWebChromeClient);
        addJavascriptInterface(new JsEventHandler(), "HassWebView_EventHandler");
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            WebViewUtils.injectJavascriptFile(getContext(), HassWebView.this, R.raw.hass_web_view);
            if (mHideAdminMenuItems) {
                WebViewUtils.execJavascript(getContext(), HassWebView.this, "HassWebView.setAdmin(false);");
            }

            mPageLoadFinished = true;
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d(TAG, consoleMessage.message() + " -- From line " + consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
            return super.onConsoleMessage(consoleMessage);
        }
    };

    public void setEventHandler(IEventHandler handler){
        mEventHandler = handler;
    }

    public void setHideAdminMenuItems(boolean hideAdminMenuItems){
        mHideAdminMenuItems = hideAdminMenuItems;
        if (mPageLoadFinished) {
            WebViewUtils.execJavascript(getContext(), this, "HassWebView.setAdmin(" + !mHideAdminMenuItems + ");");
        }
    }

    public void setAdjustBackKeyBehavior(boolean adjustBackKeyBehavior){
        mAdjustBackKeyBehavior = adjustBackKeyBehavior;
    }

    @Override
    public void goBack() {
        if (!canGoBack()){
            if (mEventHandler != null) {
                mEventHandler.onFinish();
            }
            return;
        }

        if (mAdjustBackKeyBehavior){
            WebViewUtils.execJavascript(getContext(), this, "if (!HassWebView.onBackPressed()){ HassWebView_EventHandler.onFinish(); }");
        }
        else{
            super.goBack();
        }
    }
}
