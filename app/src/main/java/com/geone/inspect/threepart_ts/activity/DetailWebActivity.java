package com.geone.inspect.threepart_ts.activity;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.util.Utils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class DetailWebActivity extends Activity {

	private WebView mWebView;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_webview);

		String caseid = getIntent().getStringExtra("caseID");
		setTitle(caseid);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		actionBar.setDisplayHomeAsUpEnabled(true);

		mWebView = (WebView) findViewById(R.id.webView1);
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

		mWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				Utils.showWaitingDialog(DetailWebActivity.this);
			}

			public void onPageFinished(WebView view, String url) {
				Utils.hideWaitingDialog();
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {

			}
		});

		String url = getIntent().getStringExtra("url");
		mWebView.loadUrl(url);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return true;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// NavUtils.navigateUpFromSameTask(this);
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class WebAppInterface {
		Context mContext;

		/** Instantiate the interface and set the context */
		WebAppInterface(Context c) {
			mContext = c;
		}

		/** Show a toast from the web page */
		@JavascriptInterface
		public void showToast(String toast) {
			Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
		}
	}
}
