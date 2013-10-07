package gov.mt.mdt.data;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends Activity{
	WebView mWebView;
	
	/** Called when the activity is first created. */
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		mWebView = (WebView) findViewById(R.id.webView);
		mWebView.loadUrl("file:///android_asset/www/help.html");
		mWebView.getSettings().setJavaScriptEnabled(true);
	}
}