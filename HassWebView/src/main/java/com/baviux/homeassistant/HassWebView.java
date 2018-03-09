package com.baviux.homeassistant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.baviux.homeassistant.util.WebViewUtils;

public class HassWebView extends WebView{
    private final static String TAG = "HassWebView";

    public interface IEventHandler{
        void onFinish();
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
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            WebViewUtils.injectJavascriptFile(getContext(), HassWebView.this, R.raw.android_hass);
            if (mHideAdminMenuItems) {
                WebViewUtils.execJavascript(getContext(), HassWebView.this, "AndroidHass.setAdmin(false);");
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
            WebViewUtils.execJavascript(getContext(), this, "AndroidHass.setAdmin(" + !mHideAdminMenuItems + ");");
        }
    }

    public void setAdjustBackKeyBehavior(boolean adjustBackKeyBehavior){
        mAdjustBackKeyBehavior = adjustBackKeyBehavior;
    }

    @Override
    public void goBack() {
        if (mAdjustBackKeyBehavior){
            evaluateJavascript("AndroidHass.onBackPressed();", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    if (!"true".equals(value) && mEventHandler != null) {
                        mEventHandler.onFinish();
                    }
                }
            });
        }
        else{
            super.goBack();
        }
    }
}
