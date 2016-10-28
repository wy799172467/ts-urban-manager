package com.geone.inspect.threepart_ts.activity;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.Case;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.util.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DelayPostponeActivity extends Activity implements OnClickListener {
	/** 申请延时返回码 */
	public static final int RESULT_CODE_DELAY = 1;
	/** LABELS = { "延时", "缓办"} */
	public static final String[] LABELS = { "延时", "缓办" };
	private String label;
	private EditText etxtDelayDays;
	private EditText etxtDelayReason;
	private Button btnSubmitDelay;
	private Context mContext;
	private Case mCase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delay);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;
		Intent intent = getIntent();
		mCase = (Case) intent.getSerializableExtra("mCase");
		label = intent.getStringExtra("label");
		if (label.equalsIgnoreCase(LABELS[1])) {// 申请缓办
			setTitle(R.string.title_postpone);
		}

		initviews();
	}

	private void initviews() {
		etxtDelayDays = (EditText) findViewById(R.id.etxtDelayDays);
		etxtDelayReason = (EditText) findViewById(R.id.etxtDelayReason);
		btnSubmitDelay = (Button) findViewById(R.id.btnSubmitDelay);
		btnSubmitDelay.setOnClickListener(this);
		if (label.equalsIgnoreCase(LABELS[1])) {// 申请缓办
			etxtDelayDays.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.delay, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.action_cancel:
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// String delayDays = etxtDelayDays.isShown() ? etxtDelayDays.getText()
		// .toString() : "";
		String delayDays = etxtDelayDays.getText().toString();
		String delayReason = etxtDelayReason.getText().toString();
		switch (v.getId()) {
		case R.id.btnSubmitDelay:
			if (label.equalsIgnoreCase(LABELS[1])) {// 申请缓办
				// new ApplyPostponeAsyncTask().execute(
				// InspectorApplication.mUser.userID, mCase.CaseID,
				// delayReason, delayDays);
				if ("".equalsIgnoreCase(delayReason)) {
					Toast.makeText(this, "申请理由不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				new ApplyPostponeAsyncTask().execute(
						AppApplication.mUser.userID, mCase.CaseID,
						delayReason, "");// 不要天数
			} else {// 申请延时
				if ("".equalsIgnoreCase(delayDays)) {
					Toast.makeText(this, "申请天数不能为空", Toast.LENGTH_SHORT).show();
					return;
				}

				new ApplyDelayAsyncTask().execute(
						AppApplication.mUser.userID, mCase.CaseID,
						delayReason, delayDays);
			}
			break;
		}

	}

	/**
	 * 申请缓办处理回执
	 * rs=1:提交成功；0：提交失败，参数#userid#','#caseid#','#applyDesc#'理由,'#timeLimit#'天数
	 */
	class ApplyPostponeAsyncTask extends AsyncTask<String, Void, Object> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			Utils.showWaitingDialog(mContext);
			super.onPreExecute();

		}

		@Override
		protected Object doInBackground(String... args) {
			return HttpQuery.applyPostpone(args[0], args[1], args[2], args[3]);
		}

		@Override
		protected void onPostExecute(Object result) {
			Utils.hideWaitingDialog();
			super.onPostExecute(result);
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (result instanceof String) {
				if ("1".equalsIgnoreCase(result.toString())) {
					Toast.makeText(mContext, R.string.submit_success,
							Toast.LENGTH_SHORT).show();
					setResult(RESULT_OK);
					finish();
				} else {
					// 提交失败，无操作
					Toast.makeText(mContext, R.string.submit_failure,
							Toast.LENGTH_SHORT).show();

				}
			}

		}

	}

	/**
	 * 申请延时处理回执
	 * rs=1:提交成功；0：提交失败，参数#userid#','#caseid#','#applyDesc#'理由,'#timeLimit#'天数
	 */
	class ApplyDelayAsyncTask extends AsyncTask<String, Void, Object> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			Utils.showWaitingDialog(mContext);
			super.onPreExecute();

		}

		@Override
		protected Object doInBackground(String... args) {
			return HttpQuery.applyDelay(args[0], args[1], args[2], args[3]);
		}

		@Override
		protected void onPostExecute(Object result) {
			Utils.hideWaitingDialog();
			super.onPostExecute(result);
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (result instanceof String) {
				if ("1".equalsIgnoreCase(result.toString())) {
					Toast.makeText(mContext, R.string.submit_success,
							Toast.LENGTH_SHORT).show();
					setResult(RESULT_OK);
					finish();
				} else {
					// 提交失败，无操作
					Toast.makeText(mContext, R.string.submit_failure,
							Toast.LENGTH_SHORT).show();

				}
			}

		}

	}
}
