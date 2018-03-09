# Home-Assistant-WebView
A modified Android WebView with some enhancements for display Home Assistant Web


## Hide administrator menu items
You can hide administrator menu items from the drawer menu, so you can let your family members use home assistant without the risk they change any configuration parameter.

<img src="https://github.com/sjvc/Home-Assistant-Launcher/blob/master/screenshots/no-admin-drawer.png?raw=true" width="300" />

## Back key behavior
If you are using Home Assistant through your web browser, Android back key behaves like browser back key. So, if you navigated through several tabs, pressing back key takes you to previous visited tab. Android apps don't work that way (it should close the app). So this is the back key behavior:

- If a "more info" popup is showing, popup will close.
- Else, if section "Overview" is not showing, you will be back to "Overview".
- Else, onFinish event is fired, so you can exit the app, or close a dialog, or whatever you want.

# Usage
## Add to your layout as a regular WebView:
```
    <com.baviux.homeassistant.HassWebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
## Set options
```
    mWebView.setAdjustBackKeyBehavior(boolean); // If true, back key behavior will be like described above. Default is true.
    mWebView.setHideAdminMenuItems(boolean); // If true, administrator menu items will be hidden. Default is true.
```
## Set on finish event handler
It will be fired when WebView should be "closed" (here you should finish Activity, or close a dialog, or hide the WebView, etc..)
```
    mWebView.setEventHandler(new HassWebView.IEventHandler() {
        @Override
        public void onFinish() {
            MainActivity.this.finish();
        }
    });
 ```
 ## Redirect onBackPressed event from your Activity to WebView
 ```
    @Override
    public void onBackPressed() {
        if (mWebView == null){
            super.onBackPressed();
            return;
        }

        mWebView.goBack();
    }
 ```
 ## Load HASS url
 ```
    mWebView.loadUrl("https://example.com:8123");
 ```
