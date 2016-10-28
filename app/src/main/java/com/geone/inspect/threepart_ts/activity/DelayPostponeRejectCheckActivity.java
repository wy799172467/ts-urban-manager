package com.geone.inspect.threepart_ts.activity;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.Case;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/** 查看延时和拒签 */
public class DelayPostponeRejectCheckActivity extends Activity implements
		OnClickListener {
	/** LABELS = { "延时", "缓办", "拒签" } */
	public static final String[] LABELS = { "延时", "缓办", "拒签" };
	/** 再次拒签标记 */
	public static final int RESULT_REJECT_AGAIN = -2;
	private Case delayCase, rejectCase, postponeCase;
	private TextView txtDelayDate;
	private TextView txtDelayDays;
	private TextView txtDelayReason;
	private TextView txtApprovePerson;
	private TextView txtApproveStatus;
	private TextView txtApproveAdvice;
	private TextView txtApproveDays;
	private TextView txtApproveDate;
	private Button btnRejectAgain;
	/** 是否拒签阶段，默认false */
	private boolean isReject;
	private String label;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delay_check);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent intent = getIntent();
		label = intent.getStringExtra("label");
		if (label.equalsIgnoreCase(LABELS[1])) {// 缓办
			setTitle(R.string.title_postpone_check);// 查看缓办
			postponeCase = (Case) intent.getSerializableExtra("postponeCase");
		} else if (label.equalsIgnoreCase(LABELS[2])) {// 拒签
			setTitle(R.string.title_reject_check);// 查看拒签
			rejectCase = (Case) intent.getSerializableExtra("rejectCase");
		} else {// 默认延时
			setTitle(R.string.title_delay_check);// 查看延迟
			delayCase = (Case) intent.getSerializableExtra("delayCase");
		}
		initViews();
	}

	private void initViews() {
		txtDelayDate = (TextView) findViewById(R.id.txtDelayDate);
		txtDelayDays = (TextView) findViewById(R.id.txtDelayDays);
		txtDelayReason = (TextView) findViewById(R.id.txtDelayReason);
		txtApprovePerson = (TextView) findViewById(R.id.txtApprovePerson);
		txtApproveStatus = (TextView) findViewById(R.id.txtApproveStatus);
		txtApproveAdvice = (TextView) findViewById(R.id.txtApproveAdvice);
		txtApproveDays = (TextView) findViewById(R.id.txtApproveDays);
		txtApproveDate = (TextView) findViewById(R.id.txtApproveDate);
		btnRejectAgain = (Button) findViewById(R.id.btnRejectAgain);
		btnRejectAgain.setOnClickListener(this);
		if (delayCase != null) {
			txtDelayDate.setText("申请日期：" + delayCase.applyDate);
			txtDelayDays.setText("申请天数：" + delayCase.applyAsk);
			txtDelayReason.setText("延时原因：" + delayCase.applyDesc);
			txtApprovePerson.setText("审批人员：" + delayCase.approvePersonName);
			txtApproveStatus.setText("审批状态：" + delayCase.isPassDesc);
			txtApproveAdvice.setText("审批意见：" + delayCase.approveAdvice);
			txtApproveDays.setText("批准天数：" + delayCase.approveDays);
			txtApproveDate.setText("审批日期：" + delayCase.approveDate);

		}
		if (postponeCase != null) {
			txtDelayDays.setVisibility(View.GONE);// 隐藏申请天数
			txtApproveDays.setVisibility(View.GONE);// 隐藏批准天数
			txtDelayDate.setText("申请日期：" + postponeCase.applyDate);
//			txtDelayDays.setText("申请天数：" + postponeCase.applyAsk);
			txtDelayReason.setText("缓办原因：" + postponeCase.applyDesc);
			txtApprovePerson.setText("审批人员：" + postponeCase.approvePersonName);
			txtApproveStatus.setText("审批状态：" + postponeCase.isPassDesc);
			txtApproveAdvice.setText("审批意见：" + postponeCase.approveAdvice);
//			txtApproveDays.setText("批准天数：" + postponeCase.approveDays);
			txtApproveDate.setText("审批日期：" + postponeCase.approveDate);

		}
		if (rejectCase != null) {
			txtDelayDays.setVisibility(View.GONE);// 隐藏申请天数
			txtApproveDays.setVisibility(View.GONE);// 隐藏批准天数
			btnRejectAgain.setVisibility(View.VISIBLE);
			txtDelayDate.setText("申请日期：" + rejectCase.applyDate);
			txtDelayReason.setText("拒签原因：" + rejectCase.applyDesc);
			txtApprovePerson.setText("审批人员：" + rejectCase.approvePersonName);
			txtApproveStatus.setText("审批状态：" + rejectCase.isPassDesc);
			txtApproveAdvice.setText("审批意见：" + rejectCase.approveAdvice);
			txtApproveDate.setText("审批日期：" + rejectCase.approveDate);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.delay_check, menu);
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
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRejectAgain:
			setResult(RESULT_REJECT_AGAIN);
			finish();
			break;
		default:
			break;
		}

	}
}
