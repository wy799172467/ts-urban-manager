package com.geone.inspect.threepart_ts.activity;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.util.CalendarUtils;
import com.geone.inspect.threepart_ts.util.LogUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/** 绩效考核：利用webview显示各项成绩 */
public class ScoreActivity extends Activity {
	private Context mContext;
	private WebView webScore;
	private ProgressBar barScore;
	private String mUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;
		loadData();
		initView();

	}

	private void loadData() {
		if (AppApplication.isPubllic) {
			mUrl = HttpQuery.serviceMap.get("ViewScore");
		} else {
			mUrl = HttpQuery.serviceMap.get("ViewScore_vpn");
		}
		// 测试：
		// mUrl="http://58.210.9.131/sbjcz/dsfxcservice/view/ViewCase_public.aspx?kpdate=#kpdate#&userid=#userid#";

	}

	private void initView() {
		barScore = (ProgressBar) findViewById(R.id.barLoading);
		webScore = (WebView) findViewById(R.id.webScore);
		// 重写setWebViewClient阻止系统利用自带浏览器打开网页
		webScore.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				barScore.setVisibility(View.VISIBLE);
			}

			public void onPageFinished(WebView view, String url) {
				barScore.setVisibility(View.GONE);
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {

			}
		});
		requestScore(mUrl, CalendarUtils.getCurrentTime("yyyy-MM-dd"),
				AppApplication.mUser.userID);

		// webScore.loadUrl(queryUrl);

	}

	/** 获取综合考评成绩 */
	private void requestScore(String inUrl, String inKPDate, String inUserId) {
		try {
			int end = mUrl.lastIndexOf("?");
			String queryPre = mUrl.substring(0, end + 1);
			String queryUrl = queryPre + "kpdate=" + inKPDate + "&userid="
					+ inUserId;
			// queryUrl = "http://www.baidu.com/";// 测试
			webScore.loadUrl(queryUrl);
			LogUtils.d("ScoreActivity", "queryUrl: " + queryUrl);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.score, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
