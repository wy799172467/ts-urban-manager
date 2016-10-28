package com.geone.inspect.threepart_ts.activity;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.sql.MyDatabase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

	private static long SPLASH_TIME = 0;

	private Runnable r = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent i = new Intent();
			i.setClass(SplashActivity.this, LoginActivity.class);
			startActivity(i);
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
			finish();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		// 初始化数据库
		AppApplication.myDataBase = new MyDatabase(this);

		new Handler().postDelayed(r, SPLASH_TIME);
	}
}