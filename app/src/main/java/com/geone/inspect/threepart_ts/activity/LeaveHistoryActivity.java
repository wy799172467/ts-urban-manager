package com.geone.inspect.threepart_ts.activity;

import java.util.ArrayList;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.adapter.LeaveAdapter;
import com.geone.inspect.threepart_ts.bean.Leave;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class LeaveHistoryActivity extends Activity {
	/** 请假记录 */
	private ArrayList<Leave> mLeaveList = new ArrayList<Leave>();
	private ListView lstLeaveHistory;
	private LeaveAdapter mLeaveAdapter;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leave_history);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;
		mLeaveList = (ArrayList<Leave>) getIntent().getSerializableExtra(
				"leaveList");
		lstLeaveHistory = (ListView) findViewById(R.id.lstLeaveHistory);
		if (mLeaveList != null || mLeaveList.size() > 0) {
			mLeaveAdapter = new LeaveAdapter(mContext, mLeaveList);
			lstLeaveHistory.setAdapter(mLeaveAdapter);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.leave_history, menu);
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
