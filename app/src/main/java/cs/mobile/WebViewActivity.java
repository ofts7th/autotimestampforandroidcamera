package cs.mobile;

import cs.util.JsBridge;
import cs.util.Session;
import cs.util.WebViewActivityUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

public class WebViewActivity extends Activity {
	protected WebViewActivityUtil util;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.util = new WebViewActivityUtil(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInsanBundle) {
		super.onSaveInstanceState(savedInsanBundle);
		Session.saveStates(savedInsanBundle);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInsanBundle) {
		super.onRestoreInstanceState(savedInsanBundle);
		Session.restoreStates(savedInsanBundle);
	}

	protected void startWebView() {
		WebView view = initWebView();
		view.loadUrl("file:///android_asset/index.htm");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private WebView webview;

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	private WebView initWebView() {
		webview = new WebView(this);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.addJavascriptInterface(new JsBridge(this.util), "local");
		webview.getSettings().setDomStorageEnabled(true);
		this.util.setWebView(webview);
		setContentView(webview);
		return webview;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.util.callJsFunc("goback()");
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}
