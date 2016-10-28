package com.geone.inspect.threepart_ts.activity;

import java.util.ArrayList;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.adapter.PushAdapter;
import com.geone.inspect.threepart_ts.bean.Push;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class PushActivity extends Activity {
	/** 推送消息记录 */
	private ArrayList<Push> mPushList = new ArrayList<Push>();
	private ListView lstPush;
	private PushAdapter mPushAdapter;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leave_history);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;
		mPushList = (ArrayList<Push>) getIntent().getSerializableExtra(
				"pushList");
		lstPush = (ListView) findViewById(R.id.lstLeaveHistory);
		
		if (mPushList != null || mPushList.size() > 0) {
			mPushAdapter = new PushAdapter(mContext, mPushList);
			lstPush.setAdapter(mPushAdapter);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.push, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(0, R.anim.right_out);
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
