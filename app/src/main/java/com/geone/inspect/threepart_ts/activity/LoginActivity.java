package com.geone.inspect.threepart_ts.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.esri.core.geometry.Point;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.AppVersion;
import com.geone.inspect.threepart_ts.bean.User;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.http.OnlineOfflineAsyncTask;
import com.geone.inspect.threepart_ts.http.OnlineOfflineAsyncTask.IOnlineOfflineListener;
import com.geone.inspect.threepart_ts.util.GeometryUtils;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import geone.base.network.StringCallback;
import geone.base.network.StringResponse;
import geone.base.network.URLRequest;

public class LoginActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener {


	private String mLoginUrl = "http://60.175.169.64:4000/CZYX/SCPSBJ/DSFXCService/Handler/LoginHandler.ashx?";

	public static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	public static final String AUTO_LOGIN = "auto";

	public static final String FROM_EXIT = "exit";
	/** btnLogin公网,btnVpnLogin专网登录 */
	private Button btnLogin;// btnVpnLogin;

	private EditText etUser;
	private EditText etPassword;
	private ProgressBar pb_loading, pb_loading_vpn;
	private CheckBox ckbox_remember;
	private CheckBox ckbox_auto;
	/** Activity私有参数 */
	private SharedPreferences loginInfoPrefs;
	/** 共享参数 */
	SharedPreferences mDefaultPrefs;
	private String mUsername;
	private String mPassword;
	private Context mContext;

	// Volley
	private RequestQueue mQueue;
	public static String versionName;
	private AppVersion mAppVersion;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.login);
		mContext = this;
		mQueue = Volley.newRequestQueue(this);

		etUser = (EditText) findViewById(R.id.etUser);
		etPassword = (EditText) findViewById(R.id.etPassword);
		pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
		pb_loading_vpn = (ProgressBar) findViewById(R.id.pb_loading_vpn);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(this);
		//btnVpnLogin = (Button) findViewById(R.id.btnVpnLogin);
		//btnVpnLogin.setOnClickListener(this);

		ckbox_remember = (CheckBox) findViewById(R.id.ckBox_remember);
		ckbox_auto = (CheckBox) findViewById(R.id.ckBox_auto);
		ckbox_auto.setOnCheckedChangeListener(this);

		loginInfoPrefs = getPreferences(MODE_PRIVATE);
		mDefaultPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		if (!(mUsername = loginInfoPrefs.getString(USERNAME, "")).equals("")) {
			mPassword = loginInfoPrefs.getString(PASSWORD, "");

			etUser.setText(mUsername);
			etPassword.setText(mPassword);
			ckbox_remember.setChecked(true);

			if (loginInfoPrefs.getBoolean(AUTO_LOGIN, false) == true) {
				ckbox_auto.setChecked(true);
			}
		}

		AppApplication.version = loginInfoPrefs.getInt("config_version", -1);

		Intent intent = getIntent();
		boolean isFromExit = intent.getBooleanExtra(FROM_EXIT, false);// 注销后传回true
		if (!isFromExit) {
			// versionName = getPackageVersionName();
			// requestCKUpdateUsingVolley(versionName);
		}
		if (ckbox_auto.isChecked() && !isFromExit) {
			btnLogin.performClick();
		}

	}

	private void verifyUser(final String user, final String password) {
		// 存入sharedPreferences供其他Activity使用
		mDefaultPrefs.edit().putString(USERNAME, user)
				.putString(PASSWORD, password).commit();
		ckbox_remember.setChecked(true);// 强制记录帐号密码
		if (ckbox_remember.isChecked()) {
			loginInfoPrefs.edit().putString(USERNAME, user)
					.putString(PASSWORD, password).commit();
		} else {
			loginInfoPrefs.edit().putString(USERNAME, "")
					.putString(PASSWORD, "").commit();
		}
		if (ckbox_auto.isChecked()) {
			loginInfoPrefs.edit().putBoolean(AUTO_LOGIN, true).commit();
		} else {
			loginInfoPrefs.edit().putBoolean(AUTO_LOGIN, false).commit();
		}
		//new LoginAsyncTask().execute(user, password);

//		String url =  "http://60.175.169.64:4000/CZYX/SCPSBJ/DSFXCService/Handler/LoginHandler.ashx?username="
//				+ user + "&pwd=" + password;
		//将服务登录入口的IP换成需要域名www.yxscloud.com的方式
//		String url =  "http://www.yxscloud.com/CZYX/SCPSBJ/DSFXCService/Handler/LoginHandler.ashx?username="
//				+ user + "&pwd=" + password;
		String url =  "http://58.210.9.131/sbj/DSFXCService/Handler/LoginHandler.ashx?username="
				+ user + "&pwd=" + password;


		URLRequest request = new URLRequest.Builder()
				.withUrl(url)
				.withMethod("GET")
				.withCallback(new StringCallback() {
					@Override
					public void onFailure(IOException e) {

					}

					@Override
					public void onResponse(StringResponse stringResponse) throws IOException {
						handleLoginResponse(stringResponse.getResponseBody());
						//        geone.base.utils.LogUtils.d(stringResponse.getResponseBody());
					}
				}).build();
		request.perform();
	}




	private void handleLoginResponse(String response) {

		JSONArray resultArray = null;
		JSONObject resultObj = null;
		User user = new User();
		try {
			resultArray = new JSONArray(response);
			resultObj = resultArray.getJSONObject(0);

			if (resultArray.length() <= 0) {
				return;
			}


			if (resultObj.has("error")) {
				return;
			}


			JSONObject userObj = resultObj.getJSONObject("UserInfo");

			user.userID = userObj.getString("ID");
			user.name = userObj.getString("UserName");
			user.account = userObj.getString("UserAccount");
			user.gridID = userObj.getString("Grid");
			user.interval = userObj.getLong("interval");
			user.rolename = userObj.optString("rolename", "xcDept");// 默认角色为xcDept
			// mUser.rolename = "yhDept";//测试用

			JSONArray serviceArray = resultObj.getJSONArray("ServiceList");
			if (serviceArray.length() <= 0) {
				return;
			}
			for (int i = 0; i < serviceArray.length(); i++) {
				JSONObject obj = serviceArray.getJSONObject(i);
				HttpQuery.serviceMap.put(obj.getString("name"), obj.getString("url"));
			}

			AppApplication.mUser = user;

			String url_get_code_info = null;

			// 获取配置信息(代码组)
			if (AppApplication.isPubllic) {
				url_get_code_info = HttpQuery.serviceMap.get("GetCodeInfo");
			} else {
				url_get_code_info = HttpQuery.serviceMap.get("GetCodeInfo_vpn");
			}


			requestType(url_get_code_info);
		} catch (JSONException e1) {
//			e1.printStackTrace();
			Looper.prepare();
			Toast.makeText(LoginActivity.this,"账号密码错误",Toast.LENGTH_LONG).show();
			Looper.loop();
		}



	}

	private void requestType(String url){

		//String url = "http://60.175.169.64:4000/CZYX/SCPSBJ/DSFXCService/handler/GetCodeInfoHandler.ashx?username=#username#&pwd=#pwd#&version=#version#";

		url = url.replaceAll("#username#", mUsername);
		url = url.replaceAll("#pwd#", mPassword);
		url = url.replaceAll("#version#", "0");


		URLRequest request = new URLRequest.Builder()
				.withUrl(url)
				.withMethod("GET")
				.withCallback(new StringCallback() {
					@Override
					public void onFailure(IOException e) {
						geone.base.utils.LogUtils.d(e.getLocalizedMessage());
					}

					@Override
					public void onResponse(StringResponse stringResponse) throws IOException {
						JSONObject result = null;
						try {
							result = new JSONObject(stringResponse.getResponseBody());
							int version = result.getInt("version");

							//避免重复向数据库中插入数据
							if (version > AppApplication.version) {
								AppApplication.version = version;
								AppApplication.mUser.config_version = version;

								JSONObject config = result.getJSONObject("config");
								JSONArray typeArray = config.getJSONArray("type");


								// 将caseType列表存入数据库
								AppApplication.myDataBase.insertCaseTypeList(HttpQuery.getCategoryListFromJSONArray(typeArray));

								typeArray = config.getJSONArray("LargeClass");
								// 将LargeClass列表存入数据库
								AppApplication.myDataBase.insertCaseLargeClassList(HttpQuery.getCategoryListFromJSONArray(typeArray));
								typeArray = config.getJSONArray("SmallClass");
								// 将SmallClass列表存入数据库
								AppApplication.myDataBase.insertCaseSmallClassList(HttpQuery.getCategoryListFromJSONArray(typeArray));
								typeArray = config.getJSONArray("SubClass");
								// 将SubClass列表存入数据库
								AppApplication.myDataBase.insertCaseSubClassList(HttpQuery.getCategoryListFromJSONArray(typeArray));
							}

							//hideWaiting();



							if (AppApplication.mUser.config_version != -1) {
								loginInfoPrefs.edit().putInt("config_version", AppApplication.version).commit();
							}
							// 检测更新
							versionName = getPackageVersionName();
							requestCKUpdateUsingVolley(versionName);

							// Intent intent = new Intent(LoginActivity.this,
							// MainActivity.class);
							// intent.putExtra("user", user);
							// intent.putExtra("appversion", mAppVersion);
							// startActivity(intent);
							// finish();


						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}).build();
		request.perform();


		//return mUser;
//	} catch (Exception e) {
//		String eDetail = e.toString();
//		if (eDetail.contains(E_CAUSES[0])) {
//			eDetail = "连接超时，请检查网络!";
//		} else if (eDetail.contains(E_CAUSES[1])
//				&& eDetail.contains("status")) {
//			eDetail = "帐号或密码错误.";
//		} else {
//			eDetail = "未知错误.";
//		}
//
//		// String eCause = e.getClass().getName();
//		Log.i("eDetail", "doLogin: " + eDetail);
//		e.printStackTrace();
//		return e;
//	}
	}

	private void showWaiting() {
		if (AppApplication.isPubllic) {
			pb_loading.setVisibility(View.VISIBLE);
			btnLogin.setText(R.string.logining);
		} else {
			pb_loading_vpn.setVisibility(View.VISIBLE);
			//btnVpnLogin.setText(R.string.logining);
		}

	}

	private void hideWaiting() {
		if (AppApplication.isPubllic) {
			pb_loading.setVisibility(View.GONE);
			btnLogin.setText(R.string.login);
		} else {
			pb_loading_vpn.setVisibility(View.GONE);
			//btnVpnLogin.setText(R.string.login_vpn);
		}

	}

	class LoginAsyncTask extends AsyncTask<String, Void, Object> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected Object doInBackground(String... args) {
			return HttpQuery.doLogin(args[0], args[1]);
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			hideWaiting();
			if (result instanceof Exception) { // ??????
				Exception e = (Exception) result;
				Toast.makeText(LoginActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (result instanceof Error) {
				Error e = (Error) result;
				Toast.makeText(LoginActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();

			} else if (result instanceof User) {

				User user = (User) result;
				AppApplication.mUser = user;
				if (user.config_version != -1) {
					loginInfoPrefs.edit()
							.putInt("config_version", AppApplication.version)
							.commit();
				}
				// 检测更新
				versionName = getPackageVersionName();
				requestCKUpdateUsingVolley(versionName);

				// Intent intent = new Intent(LoginActivity.this,
				// MainActivity.class);
				// intent.putExtra("user", user);
				// intent.putExtra("appversion", mAppVersion);
				// startActivity(intent);
				// finish();
			}

		}

	}

	@Override
	public void onClick(View v) {
		boolean isPubllic = true;
		switch (v.getId()) {
		case R.id.btnLogin:
			isPubllic = true;
			break;
//		case R.id.btnVpnLogin:
//			isPubllic = false;
//			break;
		}
		mUsername = etUser.getText().toString();
		mPassword = etPassword.getText().toString();
		if ("".equalsIgnoreCase(mUsername) || "".equalsIgnoreCase(mPassword)) {
			Toast.makeText(mContext, R.string.account_null, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (!Utils.isNetworkAvailable(mContext)) {
			Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		AppApplication.isPubllic = isPubllic;
		verifyUser(mUsername, mPassword);

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			ckbox_remember.setChecked(true);
		}
	}

	/** 根据versionName检测更新如versionName=1.0 */
	private void requestCKUpdateUsingVolley(String versionName) {
		// Volley

		String url = "";
		if (AppApplication.isPubllic) {
			url = HttpQuery.serviceMap.get("CheckUpdate") + versionName;
		} else {
			url = HttpQuery.serviceMap.get("CheckUpdate_vpn") + versionName;
		}
		LogUtils.d("LoginActivity", "检测appversion：" + url);
		mQueue.add(new JsonObjectRequest(Method.GET, url, null,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject jsonObject) {
						Utils.hideWaitingDialog();
						if (jsonObject == null) {
							// Toast.makeText(LoginActivity.this,
							// "无法连接服务器,请稍后再试.", Toast.LENGTH_SHORT)
							// .show();
							// 没有更新直接跳转
							// goMainActivity();
							attemptOnlineOffLine(OnlineOfflineAsyncTask.ONLINE,
									AppApplication.mUser);

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
										.getString("version");// 如1.0
								mAppVersion.update_importance = jsonObject
										.getString("update_importance");
								mAppVersion.update_content = jsonObject
										.getString("update_content");
								mAppVersion.update_time = jsonObject
										.getLong("update_time");
								mAppVersion.update_content = mAppVersion.update_content
										.replaceAll("@", "\n");
								buildDialogUpdate(LoginActivity.this,
										"发现新版本V" + mAppVersion.version,
										mAppVersion.update_content,
										mAppVersion.url).show();
							}
//							else if(jsonObject.getInt("status")==-1){
//								return;
//							}
							else {

								requestOnlineStatus();
								//attemptOnlineOffLine(OnlineOfflineAsyncTask.ONLINE, AppApplication.mUser);
							}

						} catch (JSONException e) {
							Utils.hideWaitingDialog();
							Toast.makeText(mContext, "尝试检测更新失败!",
									Toast.LENGTH_SHORT).show();
							//attemptOnlineOffLine(OnlineOfflineAsyncTask.ONLINE,AppApplication.mUser);
							requestOnlineStatus();
						}
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Utils.hideWaitingDialog();
						Toast.makeText(mContext, "尝试检测更新失败!",
								Toast.LENGTH_SHORT).show();
						attemptOnlineOffLine(OnlineOfflineAsyncTask.ONLINE,
								AppApplication.mUser);
						// Log.d("MainActivity", arg0.getLocalizedMessage());

					}

				}));
		//目前方法进行了更新,不需要此语句
//		mQueue.start();
	}


	private void requestOnlineStatus(){

		String jsonResults = null;
		String pointStr = null;
		String url = "";

		if (!GeometryUtils.isPointEmpty(AppApplication.mUser.location_point)) {
			// 将经纬度转换为点
			Point szPoint = HttpQuery.parseLatLonToPoint(AppApplication.mUser.location_point);
			// user.location_point =
			// parseLatLonToPoint(user.location_point);
			if (szPoint == null) {
				return;
			}
			pointStr = Utils.parsePointToString(szPoint);
		}

		if (AppApplication.isPubllic) {
			url = HttpQuery.serviceMap.get("SubmitCardLog");
		} else {
			url = HttpQuery.serviceMap.get("SubmitCardLog_vpn");
		}

		int end = url.indexOf("?");
		url = url.substring(0, end + 1);
		// Http Post

		//String sql = "exec InsertCardLog '" + AppApplication.mUser.userID + "','"+ pointStr + "','" + time + "','" + type + "'";

		Calendar c = Calendar.getInstance();
		String time = Utils.formatDate(c.getTime(), "yyyy-MM-dd HH:mm:ss");
		String sql = "exec InsertCardLog '" + AppApplication.mUser.userID + "','"
				+ pointStr + "','" + time + "','" + "上线" + "'";
		JSONObject data = new JSONObject();
		try {
			data.put("DBTag","DSFXC-CZ");
			data.put("querySQL",sql);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		url = url+"DBTag=CZSBJ&querySQL="+sql;

		// 上线下线
		URLRequest request = new URLRequest.Builder()
				.withUrl(url)
				.withMethod("GET")
				.withCallback(new StringCallback() {
					@Override
					public void onFailure(IOException e) {

					}

					@Override
					public void onResponse(StringResponse stringResponse) throws IOException {
						JSONArray resultArray = null;
						int rs = 0;
						try {
							resultArray = new JSONArray(stringResponse.getResponseBody());

							if (resultArray != null) {
								JSONObject result = resultArray.getJSONObject(0);
								rs = result.optInt("rs");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

						if (rs!= 1) {
							return;
						}else{
							goMainActivity();
						}


					}
				}).build();
		request.perform();


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
					goMainActivity();
					break;
				case OnlineOfflineAsyncTask.OFFLINE_SUCCESS:
					break;
				}
			}

			@Override
			public void onFailure(Message msg) {
				switch (msg.what) {
				case OnlineOfflineAsyncTask.ONLINE_FAILUER:// 上线失败
					Toast.makeText(mContext, msg.obj.toString(),
							Toast.LENGTH_SHORT).show();
					break;
				case OnlineOfflineAsyncTask.OFFLINE_FAILURE:

					break;
				}

			}
		});
	}

	/**
	 * 获得软件版本，对应manifest中version
	 */
	private String getPackageVersionName() {
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void goMainActivity() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		intent.putExtra("user", AppApplication.mUser);
		intent.putExtra("appversion", mAppVersion);
		startActivity(intent);
		finish();
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
						// goMainActivity();
						attemptOnlineOffLine(OnlineOfflineAsyncTask.ONLINE,
								AppApplication.mUser);
					}
				});
		Dialog mDialog = builder.create();
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
	}

}
