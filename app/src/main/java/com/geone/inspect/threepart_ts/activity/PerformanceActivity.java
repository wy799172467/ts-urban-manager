package com.geone.inspect.threepart_ts.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.geone.inspect.threepart_ts.adapter.PerformanceAdapter;
import com.geone.inspect.threepart_ts.bean.Performance;
import com.geone.inspect.threepart_ts.fragment.PerformanceDatePickerFragment;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**暂不启用。绩效考核：利用Listview显示各项成绩*/
public class PerformanceActivity extends Activity implements OnClickListener,
		PerformanceDatePickerFragment.DateSelectListener {
	private Button btnQueryPerformance;
	private Context mContext;
	private ListView lstPerformance;
	private PerformanceAdapter mPerformanceAdapter;
	/** 选择日期进行查询 */
	private EditText etxtPerformanceDate;
	// volley-----
	private RequestQueue mQueue;
	private List<Performance> mPerformanceList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_performance);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;
		mQueue = Volley.newRequestQueue(this);
		mPerformanceList = new ArrayList<Performance>();

		// 界面------
		initView();
	}

	private void initView() {
		lstPerformance = (ListView) findViewById(R.id.lstPerformance);
		etxtPerformanceDate = (EditText) findViewById(R.id.etxtPerformanceDate);
		etxtPerformanceDate.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.performance, menu);
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

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(0, R.anim.right_out);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.etxtPerformanceDate:// 选择日期进行查询
			PerformanceDatePickerFragment fragment = new PerformanceDatePickerFragment();
			fragment.target = etxtPerformanceDate;
			fragment.show(getFragmentManager(), "datepicker");
			break;
		default:
			break;
		}

	}

	/** inUrl:请求地址，inKPDate：考评日期，inUserId：用户id */
	private void requestPerformanceUsingVolley(String inUrl, String inKPDate,
			String inUserId) {
		// inUrl:"http://58.210.9.131/DataCenter/querySQL.ashx?DBTag=DSFXC&querySQL=exec PF_M_GetZHKPTable '#KPDate#','#user#'"
		// inUrl:"http://58.210.9.131/DataCenter/querySQL.ashx?DBTag=DSFXC&querySQL=exec PF_M_GetZHKPTable '2015-03','1244A9B8-7387-4DC1-855C-EEEA4633BE69'"

		int end = inUrl.lastIndexOf("=");
		String querySQL = inUrl.substring(end + 1);
		querySQL = querySQL.replaceAll("#KPDate#", inKPDate);
		querySQL = querySQL.replaceAll("#user#", inUserId);

		try {
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
		} catch (UnsupportedEncodingException e1) {

			e1.printStackTrace();
		}

		String queryPre = inUrl.substring(0, end + 1);
		String queryUrl = queryPre + querySQL;
		LogUtils.d("PerformanceActivity", "queryUrl: " + queryUrl);
		Utils.showWaitingDialog(this);

		mQueue.add(new JsonArrayRequest(queryUrl, new Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray array) {
				Utils.hideWaitingDialog();
				if (array == null || array.toString().contains("error")
						|| array.length() == 0) {
					Toast.makeText(mContext, "暂无数据", Toast.LENGTH_SHORT).show();
					lstPerformance.setVisibility(View.GONE);
					return;
				}
				try {
					lstPerformance.setVisibility(View.VISIBLE);
					int length = array.length();
					if (length > 0) {
						mPerformanceList.clear();
						for (int i = 0; i < length; i++) {
							JSONObject obj = (JSONObject) array.get(i);
							Performance performance = Utils
									.parseJSONToPerformance(obj);
							if (performance != null) {
								mPerformanceList.add(performance);
							}
						}
						if (mPerformanceList == null
								|| mPerformanceList.size() == 0) {
							return;
						}
						Collections.sort(mPerformanceList);// 按照xOrder排序
						mPerformanceAdapter = new PerformanceAdapter(mContext,
								mPerformanceList);
						lstPerformance.setAdapter(mPerformanceAdapter);

					} else {
						Toast.makeText(mContext, "暂无数据", Toast.LENGTH_SHORT)
								.show();
						lstPerformance.setVisibility(View.GONE);
					}

				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(mContext, "暂无数据", Toast.LENGTH_SHORT).show();
					lstPerformance.setVisibility(View.GONE);
				}
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				Utils.hideWaitingDialog();
				Toast.makeText(mContext, "暂无数据", Toast.LENGTH_SHORT).show();
				lstPerformance.setVisibility(View.GONE);
			}
		}));

	}

	@Override
	public void dateSelectCompleted(String date) {
		String url = "";
		if (AppApplication.isPubllic) {
			url = HttpQuery.serviceMap.get("GetZHKPTable");
		} else {
			url = HttpQuery.serviceMap.get("GetZHKPTable_vpn");
		}
		String userId = AppApplication.mUser.userID;
		requestPerformanceUsingVolley(url, date, userId);
	}

}
