package com.geone.inspect.threepart_ts.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.geone.inspect.threepart_ts.activity.AskforLeaveActivity;
import com.geone.inspect.threepart_ts.activity.AppApplication;
import com.geone.inspect.threepart_ts.activity.LayerListActivity;
import com.geone.inspect.threepart_ts.activity.PushActivity;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.activity.LoginActivity;
import com.geone.inspect.threepart_ts.activity.ScoreActivity;
import com.geone.inspect.threepart_ts.activity.SettingsActivity;
import com.geone.inspect.threepart_ts.bean.AppVersion;
import com.geone.inspect.threepart_ts.bean.Layer;
import com.geone.inspect.threepart_ts.bean.Push;
import com.geone.inspect.threepart_ts.bean.User;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.http.OnlineOfflineAsyncTask;
import com.geone.inspect.threepart_ts.http.OnlineOfflineAsyncTask.IOnlineOfflineListener;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.ModelUtils;
import com.geone.inspect.threepart_ts.util.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MenuFragment extends Fragment implements OnItemClickListener {

	public static final String ONLINE = "上线";
	public static final String OFFLINE = "下线";
	/** 是否上线，默认true */
	public static boolean isOnline = true;

	private static final int[] DRAWABLE_ID = { R.drawable.img_online,
			R.drawable.img_msg_push, R.drawable.img_leave,
			R.drawable.img_check, R.drawable.img_help, R.drawable.img_settings,
			R.drawable.img_download, R.drawable.img_update, R.drawable.img_exit };

	private ListView list;
	private String[] listStr;
	private Context mContext;
	private MenuListAdapter adapter;

	private User mUser;
	private AppVersion mAppVersion;

	// Volley
	private RequestQueue mQueue;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContext = getActivity();
		mQueue = Volley.newRequestQueue(mContext);
		View v = inflater.inflate(R.layout.slidingmenucontent, null);
		list = (ListView) v.findViewById(R.id.menu_list);
		mUser = (User) getArguments().getSerializable("user");
		mAppVersion = (AppVersion) getArguments().getSerializable("appversion");

		final TextView tv_username = (TextView) v.findViewById(R.id.tv_name);
		tv_username.setText(mUser.name);
		init();
		return v;
	}

	private void init() {
		listStr = mContext.getResources().getStringArray(R.array.menu_list);

		adapter = new MenuListAdapter();
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	public class MenuListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public MenuListAdapter() {
			mInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listStr.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub

			return listStr[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return DRAWABLE_ID[position];
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.menu_list_item, null);
				holder = new ViewHolder();
				holder.tv = (TextView) convertView
						.findViewById(R.id.menu_list_item_tv);
				holder.img = (ImageView) convertView
						.findViewById(R.id.img_title);
				holder.tv_notification = (TextView) convertView
						.findViewById(R.id.tv_notification);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv.setText(listStr[position]);
			holder.img.setImageDrawable(getResources().getDrawable(
					DRAWABLE_ID[position]));
			if (getItemId(position) == R.drawable.img_online) {
				if (isOnline) {
					holder.tv_notification.setVisibility(View.VISIBLE);
					holder.tv_notification.setText("已上线");
				} else {
					holder.tv_notification.setVisibility(View.INVISIBLE);
				}
			}
			if (getItemId(position) == R.drawable.img_update) {
				holder.tv_notification.setVisibility(View.VISIBLE);
				if (mAppVersion != null) {
					holder.tv_notification.setTextColor(Color.RED);
					holder.tv_notification.setText("新版本可用: V"
							+ mAppVersion.version);
				} else {
					holder.tv_notification.setText("V"
							+ LoginActivity.versionName);
				}

			}
			if (getItemId(position) == R.drawable.img_exit) {
				if (isOnline) {
					holder.tv.setText("下线");
				} else {
					holder.tv.setText("注销");
				}
			}

			return convertView;
		}

		@Override
		public boolean areAllItemsEnabled() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			// if (position == 0 || position == 7) {
			// return false;
			// }
			return super.isEnabled(position);
		}

	}

	public class ViewHolder {
		TextView tv;
		ImageView img;
		TextView tvTitle;
		TextView tv_notification;
	}

	@Override
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		Intent intent = null;
		switch ((int) id) {
		case R.drawable.img_online:// 上线:打卡

			if (isOnline) {
				Toast.makeText(mContext, R.string.repeat_online,
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (!Utils.isNetworkAvailable(mContext)) {
				// 若网络不可用则提示联网
				Toast.makeText(mContext, R.string.no_network,
						Toast.LENGTH_SHORT).show();
				return;
			}
			attemptOnlineOffLine(OnlineOfflineAsyncTask.ONLINE,
					AppApplication.mUser);
			break;
		case R.drawable.img_msg_push:// 今日推送
			// 示例dateStr=2015-03-12
			String dateStr = Utils.formatDate(Calendar.getInstance().getTime(),
					"yyyy-MM-dd");
			requestPushMessageUsingVolley(dateStr);

			break;
		case R.drawable.img_leave:// 请假
			// 跳转到请假界面
			intent = new Intent(mContext, AskforLeaveActivity.class);
			intent.putExtra("user", mUser);
			mContext.startActivity(intent);
			getActivity().overridePendingTransition(R.anim.left_in,
					R.anim.stable);

			break;
		case R.drawable.img_check:// 绩效考核
			// Intent performanceIntent = new Intent(mContext,
			// PerformanceActivity.class);
			intent = new Intent(mContext, ScoreActivity.class);
			// performanceIntent.putExtra("user", mUser);
			mContext.startActivity(intent);
			getActivity().overridePendingTransition(R.anim.left_in,
					R.anim.stable);
			break;
		case R.drawable.img_help:// 系统帮助
			// 将文件拷贝只sd卡后 打开
			String pdfName = "help_info.pdf";
			String pdfPath = Utils.getSDCardPath() + "/"
					+ AppApplication.APP_ID + "/";
			LogUtils.d("MenuFragment", "pdfPath+name:" + pdfPath + pdfName);
			if (Utils.isFileExist(pdfPath + pdfName)) {
			} else {
				boolean isSuccess = Utils.copyRawFile2SD(mContext,
						R.raw.help_info, pdfName, pdfPath);
				if (!isSuccess) {
					return;
				}
			}
			intent = Utils.getPdfFileIntent(pdfPath + pdfName);
			//判断是否安装PDF阅读器
			boolean flag=Utils.testPDFReader(mContext,intent);
			if(flag){
				startActivity(intent);
			}else {
			}
			Toast.makeText(mContext,"请确保已安装PDF阅读器",Toast.LENGTH_LONG).show();
			break;
		case R.drawable.img_exit:// 注销、下线

			// 若不在线则注销，否则切换到下线
			if (!isOnline) {
				attemptLogout();
				return;

			}
			if (!Utils.isNetworkAvailable(mContext)) {
				// 若网络不可用则提示联网
				Toast.makeText(mContext, R.string.no_network,
						Toast.LENGTH_SHORT).show();
				return;
			}
			buildDialogOffline(mContext).show();
			break;
		case R.drawable.img_settings:// 参数设置
			intent = new Intent(mContext, SettingsActivity.class);
			intent.putExtra("user", mUser);
			mContext.startActivity(intent);
			break;
		case R.drawable.img_download:// 图层管理
			// intent = new Intent(mContext, LayerListActivity.class);
			// startActivity(intent);
			requestLayersUsingVolley();
			break;
		case R.drawable.img_update:// 检测更新
			if (mAppVersion != null) {
				buildDialogUpdate(mContext, "新版本V" + mAppVersion.version,
						mAppVersion.update_content, mAppVersion.url).show();
			} else {
				requestCKUpdateUsingVolley(LoginActivity.versionName);
			}
			break;
		default:
			break;
		}

	}

	private Dialog buildDialogOffline(final Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(R.string.hint);
		builder.setMessage(R.string.logout_detail);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 下线
						attemptOnlineOffLine(OnlineOfflineAsyncTask.OFFLINE,
								AppApplication.mUser);

					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// setTitle("点击了对话框上的取消按钮");
					}
				});

		return builder.create();
	}

	/** 上线下线 */
	private void attemptOnlineOffLine(String type, User user) {
		OnlineOfflineAsyncTask task = new OnlineOfflineAsyncTask();
		task.execute(type, user);
		task.setOnlineOfflineListener(new IOnlineOfflineListener() {
			@Override
			public void onSuccess(Message msg) {
				switch (msg.what) {
				case OnlineOfflineAsyncTask.ONLINE_SUCCESS:

					break;
				case OnlineOfflineAsyncTask.OFFLINE_SUCCESS:
					attemptLogout();
					break;
				}
			}

			@Override
			public void onFailure(Message msg) {
				switch (msg.what) {
				case OnlineOfflineAsyncTask.ONLINE_FAILUER:// 上线失败
					break;
				case OnlineOfflineAsyncTask.OFFLINE_FAILURE:
					Toast.makeText(mContext, msg.obj.toString(),
							Toast.LENGTH_SHORT).show();
					break;
				}

			}
		});
	}

	/** 注销 */
	private void attemptLogout() {
		// 取消自动登录
		SharedPreferences loginInfoPrefs = getActivity().getPreferences(
				mContext.MODE_PRIVATE);
		loginInfoPrefs.edit().putBoolean(LoginActivity.AUTO_LOGIN, false)
				.commit();

		Intent login = new Intent(mContext, LoginActivity.class);
		login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		login.addCategory(Intent.CATEGORY_HOME);
		login.putExtra(LoginActivity.FROM_EXIT, true);
		startActivity(login);
		getActivity().finish();
	}

	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
		}

	}

	private void requestCKUpdateUsingVolley(String versionName) {
		// Volley
		String url = "";
		if (AppApplication.isPubllic) {
			url = HttpQuery.serviceMap.get("CheckUpdate") + versionName;
		} else {
			url = HttpQuery.serviceMap.get("CheckUpdate_vpn") + versionName;
		}
		LogUtils.d("LoginActivity", "检测appversion：" + url);

//		//换端口
//		int index=url.lastIndexOf("4");
//		String pre=url.substring(0,index+1);
//		String pro=url.substring(index+7,url.length());
//		url=pre+":8007"+pro;

		mQueue.add(new JsonObjectRequest(Method.GET, url, null,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject jsonObject) {
						Utils.hideWaitingDialog();
						if (jsonObject == null) {
							Toast.makeText(getActivity(), "无法连接服务器,请稍后再试.",
									Toast.LENGTH_SHORT).show();
							return;
						}
						try {
							// 存在更新
							if (jsonObject.getInt("status") > 0) {
								mAppVersion = new AppVersion();
								mAppVersion.url = jsonObject.getString("url");
								if (!AppApplication.isPubllic) {// 专网地址替换
									mAppVersion.url = mAppVersion.url
											.replaceAll(HttpQuery.apkPublicIp,
													HttpQuery.vpnIp);
								}
								mAppVersion.version = jsonObject
										.getString("version");
								mAppVersion.update_importance = jsonObject
										.getString("update_importance");
								mAppVersion.update_content = jsonObject
										.getString("update_content");
								mAppVersion.update_time = jsonObject
										.getLong("update_time");
								mAppVersion.update_content = mAppVersion.update_content
										.replaceAll("@", "\n");
								buildDialogUpdate(mContext,
										"发现新版本V" + mAppVersion.version,
										mAppVersion.update_content,
										mAppVersion.url).show();
							} else {

								Toast.makeText(mContext, "已是最新版本.",
										Toast.LENGTH_SHORT).show();

							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {

						String errMsg = arg0.getLocalizedMessage();
						Toast.makeText(getActivity(), errMsg,
								Toast.LENGTH_SHORT).show();
						// Log.d("MainActivity", arg0.getLocalizedMessage());

					}

				}));
		//目前方法进行了更新,不需要此语句
//		mQueue.start();
	}

	private Dialog buildDialogUpdate(Context context, String title, String msg,
			final String url) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton(R.string.alert_dialog_download,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri
								.parse(url));
						startActivity(intent);
					}
				});
		builder.setNegativeButton(R.string.alert_dialog_no,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		Dialog mDialog = builder.create();
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
	}

	/** 获取今日推送内容 */
	private void requestPushMessageUsingVolley(String dateStr) {
		if (dateStr == null || "".equalsIgnoreCase(dateStr)) {
			return;
		}
		String url = "";
		if (AppApplication.isPubllic) {
			url = HttpQuery.serviceMap.get("GetPushInfoList");
		} else {
			url = HttpQuery.serviceMap.get("GetPushInfoList_vpn");
		}
		int end = url.lastIndexOf("=");
		String querySQL = url.substring(end + 1);
		querySQL = querySQL.replaceAll("#PushDate#", dateStr);
		try {
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
		} catch (UnsupportedEncodingException e1) {

			e1.printStackTrace();
		}

		String queryPre = url.substring(0, end + 1);
		String queryUrl = queryPre + querySQL;
		LogUtils.d("MenuFragment", "queryUrl: " + queryUrl);
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
					ArrayList<Push> pushList = new ArrayList<Push>();

					for (int i = 0; i < length; i++) {
						JSONObject obj = (JSONObject) array.get(i);
						Push push = Utils.parseJSONToPush(obj);
						if (push != null) {
							pushList.add(push);
						}
					}

					if (pushList.size() > 0) {
						Intent pushIntent = new Intent(mContext,
								PushActivity.class);
						pushIntent.putExtra("pushList", pushList);
						mContext.startActivity(pushIntent);
						getActivity().overridePendingTransition(R.anim.left_in,
								R.anim.stable);

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

	/** 获取离线地图图层 */
	private void requestLayersUsingVolley() {
		String url = "";
		if (AppApplication.isPubllic) {
			url = HttpQuery.serviceMap.get("GetMapTPK");
		} else {
			url = HttpQuery.serviceMap.get("GetMapTPK_vpn");
		}
		int end = url.lastIndexOf("=");
		String queryPre = url.substring(0, end + 1);
		String querySQL = url.substring(end + 1);
		try {
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			Toast.makeText(mContext, "连接服务器失败！", Toast.LENGTH_SHORT).show();
			e1.printStackTrace();
		}
		String queryUrl = queryPre + querySQL;
		LogUtils.d("MenuFragment", "queryUrl: " + queryUrl);
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

					ArrayList<Layer> layerList = new ArrayList<Layer>();
					for (int i = 0; i < length; i++) {
						JSONObject obj = (JSONObject) array.get(i);
						Layer layer = ModelUtils.parseJSONToLayer(obj);
						if (layer != null) {
							layerList.add(layer);
						}
					}
					if (layerList.size() > 0) {
						Intent intent = new Intent(mContext,
								LayerListActivity.class);
						intent.putExtra("layerList", layerList);
						mContext.startActivity(intent);
						getActivity().overridePendingTransition(R.anim.left_in,
								R.anim.stable);
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
	// class OnlineOfflineAsyncTask extends AsyncTask<Object, Void, Object> {
	//
	// String type;
	//
	// @Override
	// protected void onPreExecute() {
	// super.onPreExecute();
	// }
	//
	// @Override
	// protected Object doInBackground(Object... args) {
	// type = (String) args[0];// 上下线
	// Calendar c = Calendar.getInstance();
	// String time = Utils.formatDate(c.getTime(), "yyyy-MM-dd HH:mm:ss");
	// Object ob = HttpQuery.goOnLineOffLine(mUser, time, type);
	// return ob;
	// }
	//
	// @Override
	// protected void onPostExecute(Object result) {
	// // super.onPostExecute(result);
	// if (result instanceof Exception) {
	// Exception e = (Exception) result;
	// Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
	// .show();
	// return;
	// }
	// if (result instanceof String) {
	// if ("1".equalsIgnoreCase(result.toString())) {
	//
	// if (type.equals(ONLINE)) {
	// isOnline = true;
	// if (!MainActivity.mHandler
	// .hasMessages(MainActivity.MSG_UPLOAD_LOCATION)) {// v2.5
	// MainActivity.mHandler
	// .sendEmptyMessage(MainActivity.MSG_UPLOAD_LOCATION);
	// }
	// } else {
	// isOnline = false;
	// MainActivity.mHandler
	// .removeMessages(MainActivity.MSG_UPLOAD_LOCATION);
	//
	// }
	// Toast.makeText(mContext, type + "成功", Toast.LENGTH_SHORT)
	// .show();
	// // 更新列表
	// adapter.notifyDataSetChanged();
	// } else {
	// // 后台提示，如“GPS坐标不在有效网格内”
	// Toast.makeText(mContext, result.toString(),
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// }
	//
	// }
	// }
}
