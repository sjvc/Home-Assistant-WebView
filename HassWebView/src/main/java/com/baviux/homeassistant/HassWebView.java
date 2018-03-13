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
    private final static String HASS_WEB_TITLE = "Home Assistant";

    public interface IOnFinishEventHandler{
        void onFinish();
    }

    public interface IMoreInfoDialogHandler{
        void onShowMoreInfoDialog();
        void onHideMoreInfoDialog();
    }

    class JsEventHandler {
        @JavascriptInterface
        public void onFinish(){
            if (mOnFinishEventHandler != null){
                mOnFinishEventHandler.onFinish();
            }
        }
        @JavascriptInterface
        public void onShowMoreInfoDialog(){
            if(mMoreInfoDialogHandler != null){
                mMoreInfoDialogHandler.onShowMoreInfoDialog();
            }
        }
        @JavascriptInterface
        public void onHideMoreInfoDialog(){
            if(mMoreInfoDialogHandler != null){
                mMoreInfoDialogHandler.onHideMoreInfoDialog();
            }
        }
    }

    private IOnFinishEventHandler mOnFinishEventHandler;
    private IMoreInfoDialogHandler mMoreInfoDialogHandler;
    private boolean mWebPageIsHass = false;
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

            mWebPageIsHass = HASS_WEB_TITLE.equals(view.getTitle());

            if (mWebPageIsHass) {
                WebViewUtils.injectJavascriptFile(getContext(), HassWebView.this, R.raw.hass_web_view);
                WebViewUtils.execJavascript(getContext(), HassWebView.this, "HassWebView.onLoad(" + (mHideAdminMenuItems ? "false" : "true") + ");");
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

    public void setOnFinishEventHandler(IOnFinishEventHandler handler){
        mOnFinishEventHandler = handler;
    }

    public void setMoreInfoDialogHandler(IMoreInfoDialogHandler handler){
        mMoreInfoDialogHandler = handler;
    }

    public void setHideAdminMenuItems(boolean hideAdminMenuItems){
        mHideAdminMenuItems = hideAdminMenuItems;
        if (mPageLoadFinished && mWebPageIsHass) {
            WebViewUtils.execJavascript(getContext(), this, "HassWebView.setAdmin(" + !mHideAdminMenuItems + ");");
        }
    }

    public void setAdjustBackKeyBehavior(boolean adjustBackKeyBehavior){
        mAdjustBackKeyBehavior = adjustBackKeyBehavior;
    }

    public boolean onBackPressed() {
        // If loaded web page is not Home Assistant -> event not handled
        if (!mWebPageIsHass){
            return false;
        }

        // If cannot go back -> "finish" WebView
        if (!canGoBack()){
            if (mOnFinishEventHandler != null) {
                mOnFinishEventHandler.onFinish();
            }
            return true;
        }

        // If adjust back behavior is set -> let HassWebView handle it
        if (mAdjustBackKeyBehavior){
            WebViewUtils.execJavascript(getContext(), this, "if (!HassWebView.onBackPressed()){ HassWebView_EventHandler.onFinish(); }");
        }
        // Else -> let standard WebView handle it
        else{
            super.goBack();
        }

        return true;
    }
}
