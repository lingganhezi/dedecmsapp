package com.lingganhezi.myapp.ui.activity;

import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.ui.Topbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class ArticleActivity extends BaseActivity {

	private final String BLANK_PAGE_URL = "file:///android_asset/none.html";

	public final static String KEY_URL = "KEY_URL";
	public final static String KEY_TITLE = "KEY_TITLE";
	public final static String KEY_CONTENT = "KEY_CONTENT";

	private WebView mWebView;
	private ProgressBar mProgressBar;
	private Topbar mTopbar;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_acticle);
		mTopbar = (Topbar) findViewById(R.id.topbar);
		mProgressBar = (ProgressBar) findViewById(R.id.web_progressbar);

		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setWebChromeClient(mWebChromeClient);
		configureWebSettings(mWebView.getSettings());

		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	public void handleIntent(Intent intent) {
		String title = intent.getStringExtra(KEY_TITLE);
		mTopbar.setTitle(title);

		String url = intent.getStringExtra(KEY_URL);
		if (TextUtils.isEmpty(url)) {
			url = BLANK_PAGE_URL;
		}
		mWebView.loadUrl(url);
	}

	@Override
	public void onResume() {
		super.onResume();
		mWebView.onResume();
	}

	@Override
	public void onPause() {
		mWebView.onPause();
		super.onPause();
	}

	private static void configureWebSettings(WebSettings settings) {
		settings.setJavaScriptEnabled(true);
	}

	/**
	 * This {@link WebChromeClient} has implementation for handling
	 * {@link PermissionRequest}.
	 */
	private WebChromeClient mWebChromeClient = new WebChromeClient() {

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			mProgressBar.setProgress(newProgress);
			if (newProgress >= 100) {
				mProgressBar.setVisibility(View.GONE);
			} else {
				mProgressBar.setVisibility(View.VISIBLE);
			}
			super.onProgressChanged(view, newProgress);
		}

	};
}
