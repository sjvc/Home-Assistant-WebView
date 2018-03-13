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
## Add this in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

## Add dependency to your build.gradle:
```
    compile 'com.github.sjvc:Home-Assistant-WebView:master-SNAPSHOT'
```

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
## Set onFinish event handler
It will be fired when WebView should be "closed" (here you should finish Activity, or close a dialog, or hide the WebView, etc..)
```
    mWebView.setOnFinishEventHandler(new HassWebView.IOnFinishEventHandler() {
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

        mWebView.onBackPressed();
    }
 ```
 ## Load HASS url
 ```
    mWebView.loadUrl("https://example.com:8123");
 ```
