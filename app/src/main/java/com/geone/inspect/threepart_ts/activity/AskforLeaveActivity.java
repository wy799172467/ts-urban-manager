package com.geone.inspect.threepart_ts.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.Leave;
import com.geone.inspect.threepart_ts.bean.User;
import com.geone.inspect.threepart_ts.fragment.DatePickerFragment;
import com.geone.inspect.threepart_ts.fragment.TimePickerFragment;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.Utils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AskforLeaveActivity extends Activity {

	private static EditText et_startDate;
	private static EditText et_startTime;
	private static EditText et_endDate;
	private static EditText et_endTime;
	private static Spinner spin_leave_type;
	private static EditText et_leave_reason;

	private static String leave_type;

	private User mUser;
	private Context mContext;
	private RequestQueue mQueue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_askfor_leave);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;
		mQueue = Volley.newRequestQueue(mContext);
		mUser = (User) getIntent().getSerializableExtra("user");
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(0, R.anim.right_out);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.askfor_leave, menu);
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
			break;
		case R.id.history:// 查看请假历史记录
			String url ="";
			if (AppApplication.isPubllic) {
				url = HttpQuery.serviceMap.get("GetLeaveList");
			} else {
				url = HttpQuery.serviceMap.get("GetLeaveList_vpn");
			}
			String userId = mUser.userID;
			requestLeaveHistoryUsingVolley(url, userId);
			break;
		case R.id.ok:
			if (isEditTextEmpty(et_startTime) || isEditTextEmpty(et_startDate)
					|| isEditTextEmpty(et_endDate)
					|| isEditTextEmpty(et_endTime)
					|| isEditTextEmpty(et_leave_reason)) {

				return false;
			}
			String starttime = et_startDate.getText().toString() + " "
					+ et_startTime.getText().toString();
			String endtime = et_endDate.getText().toString() + " "
					+ et_endTime.getText().toString();
			String leave_reason = et_leave_reason.getText().toString();
			//判断开始时间和结束时间的大小关系
			int flag=starttime.compareTo(endtime);
			if(flag<0){
				new AskForLeaveAsyncTask().execute(starttime, endtime, leave_type,
						leave_reason);
			}
			else {
				Toast.makeText(AskforLeaveActivity.this,"开始日期(时间)应小于结束日期(时间)",Toast.LENGTH_LONG).show();
			}

			break;
		case R.id.cancel:
			onBackPressed();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private boolean isEditTextEmpty(EditText et) {
		if ("".equals(et.getText().toString())) {
			et.setError("必须填写");
			return true;
		}
		return false;
	}

	/** 获取请假记录的异步请求 */
	private void requestLeaveHistoryUsingVolley(String inUrl, String inUserId) {

		int end = inUrl.lastIndexOf("=");
		String querySQL = inUrl.substring(end + 1);
		querySQL = querySQL.replaceAll("#userid#", inUserId);
		try {
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
		} catch (UnsupportedEncodingException e1) {

			e1.printStackTrace();
		}

		String queryPre = inUrl.substring(0, end + 1);
		String queryUrl = queryPre + querySQL;
		LogUtils.d("AskforLeaveActivity", "queryUrl: " + queryUrl);
		Utils.showWaitingDialog(mContext);
		mQueue.add(new JsonArrayRequest(queryUrl, new Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray array) {
				Utils.hideWaitingDialog();
				if (array == null || array.toString().contains("error")
						|| array.length() == 0) {
					Toast.makeText(mContext, "暂无数据", Toast.LENGTH_SHORT).show();
					return;
				}
				try {

					int length = array.length();
					/** 请假记录 */
					ArrayList<Leave> leaveList = new ArrayList<Leave>();

					for (int i = 0; i < length; i++) {
						JSONObject obj = (JSONObject) array.get(i);
						Leave leave = Utils.parseJSONToLeave(obj);
						if (leave != null) {
							leaveList.add(leave);
						}
					}

					if (leaveList.size() > 0) {
						Intent leaveHistoryIntent = new Intent(mContext,
								LeaveHistoryActivity.class);
						leaveHistoryIntent.putExtra("leaveList", leaveList);
						mContext.startActivity(leaveHistoryIntent);
						overridePendingTransition(R.anim.left_in, R.anim.stable);
						
						// LeaveAdapter leaveAdapter = new
						// LeaveAdapter(mContext,
						// leaveList);
						// lstLeaveHistory.setAdapter(leaveAdapter);
					}

				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(mContext, "暂无数据", Toast.LENGTH_SHORT).show();

				}
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				Utils.hideWaitingDialog();
				Toast.makeText(mContext, "暂无数据", Toast.LENGTH_SHORT).show();

			}
		}));

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements
			OnItemSelectedListener {
		private Context mContext;

		public PlaceholderFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			mContext = this.getActivity();

			super.onCreate(savedInstanceState);

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.fragment_askfor_leave,
					container, false);
			et_startDate = (EditText) rootView.findViewById(R.id.et_startdate);
			et_startTime = (EditText) rootView.findViewById(R.id.et_starttime);
			et_endDate = (EditText) rootView.findViewById(R.id.et_enddate);
			et_endTime = (EditText) rootView.findViewById(R.id.et_endtime);
			et_leave_reason = (EditText) rootView
					.findViewById(R.id.et_leave_reason);
			et_startDate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					DatePickerFragment fragment = new DatePickerFragment();

					fragment.target = et_startDate;
					fragment.show(getFragmentManager(), "datepicker");

				}
			});
			et_startTime.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					TimePickerFragment fragment = new TimePickerFragment();

					fragment.target = et_startTime;
					fragment.show(getFragmentManager(), "timepicker");

				}
			});
			et_endDate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					DatePickerFragment fragment = new DatePickerFragment();

					fragment.target = et_endDate;
					fragment.show(getFragmentManager(), "datepicker");

				}
			});
			et_endTime.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					TimePickerFragment fragment = new TimePickerFragment();

					fragment.target = et_endTime;
					fragment.show(getFragmentManager(), "timepicker");

				}
			});
			spin_leave_type = (Spinner) rootView
					.findViewById(R.id.spin_leavetype);
			ArrayAdapter<String> adapter_type = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_spinner_item,
					getResources().getStringArray(R.array.leave_array));

			adapter_type
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spin_leave_type.setAdapter(adapter_type);
			spin_leave_type.setOnItemSelectedListener(this);
			leave_type = getResources().getStringArray(R.array.leave_array)[0];

			return rootView;
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {

			leave_type = getResources().getStringArray(R.array.leave_array)[position];

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			//

		}

	}

	class AskForLeaveAsyncTask extends AsyncTask<String, Void, Object> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Utils.showWaitingDialog(AskforLeaveActivity.this);
		}

		@Override
		protected Object doInBackground(String... args) {
			String starttime = args[0];
			String endtime = args[1];
			String leavetype = args[2];
			String reason = args[3];
			return HttpQuery.askForLeave(mUser, starttime, endtime, leavetype,
					reason);
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			Utils.hideWaitingDialog();

			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(AskforLeaveActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (result instanceof Error) {
				Error e = (Error) result;
				Toast.makeText(AskforLeaveActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
				return;
				//
			} else if (result instanceof String) {
				String returnValue = (String) result;
				// setResult(RESULT_OK);
				Toast.makeText(AskforLeaveActivity.this, returnValue,
						Toast.LENGTH_SHORT).show();
				onBackPressed();
			}

		}
	}

}
