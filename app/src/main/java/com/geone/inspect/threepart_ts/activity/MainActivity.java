package com.geone.inspect.threepart_ts.activity;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.esri.core.geometry.Point;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.adapter.TabPagerAdapter;
import com.geone.inspect.threepart_ts.bean.AppVersion;
import com.geone.inspect.threepart_ts.bean.User;
import com.geone.inspect.threepart_ts.fragment.MenuFragment;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.http.OnlineOfflineAsyncTask;
import com.geone.inspect.threepart_ts.http.OnlineOfflineAsyncTask.IOnlineOfflineListener;
import com.geone.inspect.threepart_ts.util.GPSTracker;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.Utils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends SlidingFragmentActivity implements
		ActionBar.TabListener {
	/** 上传位置消息User.location_point */
	public static final int MSG_UPLOAD_LOCATION = 0;
	/** 位置上传间隔的单位以秒计 */
	public static final long UPLOAD_INTERVAL_UNIT = 1000;// 测试用，正式使用时改为1000
	public static Handler mHandler;
	private Context mContext;

	private TabPagerAdapter mTabPagerAdapter;

	private ViewPager mViewPager;

	private User mUser;
	private AppVersion mAppVersion;

	private static GPSTracker mGpsTracker;
	/** 上传位置的时间间隔，从服务器端获取 */
	private long upload_interval;
	// Volley
	private RequestQueue mQueue;
	public static String versionName;
	public OnPageChange onPageChange;

	public interface OnPageChange {
		public void onPageChange();

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHandler = new Handler() {
			int i = 1, j = 1;// 测试用

			@Override
			public void handleMessage(Message msg) {

				super.handleMessage(msg);

				switch (msg.what) {
				// 上传用户位置User.location_point
				case MSG_UPLOAD_LOCATION:
					LogUtils.d("MainActivity", "位置上传" + i);
					i++;
					uploadUpdateLocation(mContext, MSG_UPLOAD_LOCATION,
							upload_interval);
					break;
				default:
					break;
				}
			}
		};
		mContext = this;
		mUser = (User) getIntent().getSerializableExtra("user");
		// PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		mGpsTracker = new GPSTracker(this);
		mGpsTracker.startGPSLocation();// 开始搜索GPS信号
		// 检测GPS是否打开，没有打开时提示打开
		if (!mGpsTracker.isGPSEnabled()) {
			mGpsTracker.showSettingsAlert();
		}
		upload_interval = mUser.interval * UPLOAD_INTERVAL_UNIT;
		// upload_interval = 6000;// 调试用，正式上线时更换为后台时间间隔

		// mUser.location_point = getCurrentLocation();// 此处有可能为null
		// mHandler.sendEmptyMessageDelayed(MSG_UPDATE_LOCATION,2000);//
		// 2s后开始更新位置User.location_point
		mHandler.sendEmptyMessageDelayed(MSG_UPLOAD_LOCATION, 5000);// v2.5
																	// 5s后开始上传位置User.location_point
		mQueue = Volley.newRequestQueue(this);
		mAppVersion = (AppVersion) getIntent().getSerializableExtra(
				"appversion");

		// Create the adapter that will return a fragment for each of the three
		// primary sections
		// of the app.
		FragmentManager mFragmentManager = getSupportFragmentManager();
		mTabPagerAdapter = new TabPagerAdapter(this, mFragmentManager);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		// Specify that the Home/Up button should not be enabled, since there is
		// no hierarchical
		// parent.
		// actionBar.setHomeButtonEnabled(false);
		// Specify that we will be displaying tabs in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Set up the ViewPager
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mTabPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);// 默认为1,0不起作用
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// select the corresponding tab.
						actionBar.setSelectedNavigationItem(position);
						// ((CaseReportFragment) mTabPagerAdapter
						// .getItem(position)).updateHSHCCLCaseListView();
					}
				});

		// add tabs
		for (int i = 0; i < mTabPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mTabPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		// actionBar.getTabAt(0).setIcon(R.drawable.action_search);
		// TextView txt=new TextView(mContext);
		// txt.setBackgroundResource(R.drawable.action_search);
		// actionBar.getTabAt(0).setCustomView(txt);
		// txt.setText("问题上报");

		actionBar.setDisplayHomeAsUpEnabled(true);
		setBehindContentView(R.layout.menu_frame);

		// set up sliding menu
		SlidingMenu slidingMenu = getSlidingMenu();
		MenuFragment menuFragment = new MenuFragment();
		Bundle mBundle = new Bundle();
		mBundle.putSerializable("user", mUser);
		mBundle.putSerializable("appversion", mAppVersion);
		menuFragment.setArguments(mBundle);
		mFragmentManager.beginTransaction().add(R.id.menu_frame, menuFragment)
				.commit();
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		// 养护单位默认待处理界面
		if (User.ROLENAMES[2].equalsIgnoreCase(AppApplication.mUser.rolename)) {
			mViewPager.setCurrentItem(1);
		}

	}

	/**
	 * 上传或更新位置
	 * 
	 * @param inContext
	 * @param inMessage
	 *            ,上传为MSG_UPLOAD_LOCATION，更新为MSG_UPDATE_LOCATION
	 * @param inInterval
	 */
	protected void uploadUpdateLocation(Context inContext, int inMessage,
			long inInterval) {
		// 检测网络
		if (!Utils.isNetworkAvailable(mContext)) {
			switch (inMessage) {
			case MSG_UPLOAD_LOCATION:
				Toast.makeText(mContext, R.string.upload_no_network,
						Toast.LENGTH_SHORT).show();
				break;
			}
			mHandler.sendEmptyMessageDelayed(inMessage, inInterval);
			return;
		}
		// 检测GPS是否打开
		mGpsTracker.startGPSLocation();// 开始搜索GPS信号，注意：必须放在isGPSEnabled()前，否则isGPSEnabled返回值错误
		if (!mGpsTracker.isGPSEnabled()) {
			switch (inMessage) {
			case MSG_UPLOAD_LOCATION:
				Toast.makeText(mContext, R.string.upload_no_gps, Toast.LENGTH_SHORT).show();
				break;

			}
			mHandler.sendEmptyMessageDelayed(inMessage, inInterval);
			return;
		}
		// 过滤无效位置
		Point current_point = getCurrentLocation();

		if (current_point != null) {
			// Toast.makeText(
			// mContext,
			// "Longitude:" + current_point.getX() + "latitude:"
			// + current_point.getY(), Toast.LENGTH_SHORT).show();
			// 上传或更新用户位置
			mUser.location_point = current_point;// 注意此Point未转换为苏州独立坐标
			switch (inMessage) {
			case MSG_UPLOAD_LOCATION:
				new UploadUserLocationAsyncTask().execute();
				break;
			}

		}
		// 只要不下线继续上传或更新
		mHandler.sendEmptyMessageDelayed(inMessage, inInterval);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeMessages(MSG_UPLOAD_LOCATION);
		mGpsTracker.stopUsingGPS();

	}

	@Override
	public void onBackPressed() {
		buildDialogExit(this).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main, menu);
		// // Associate searchable configuration with the SearchView
		// SearchManager searchManager = (SearchManager)
		// getSystemService(Context.SEARCH_SERVICE);
		// SearchView searchView = (SearchView) menu.findItem(R.id.search)
		// .getActionView();
		// searchView.setSearchableInfo(searchManager
		// .getSearchableInfo(getComponentName()));
		// searchView.setQueryHint(getResources().getText(R.string.search_hint));
		//
		// // Applies white color on searchview text
		// int id = searchView.getContext().getResources()
		// .getIdentifier("android:id/search_src_text", null, null);
		// TextView textView = (TextView) searchView.findViewById(id);
		// textView.setHintTextColor(Color.WHITE);

		return true;
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		switch (tab.getPosition()) {
		case 0:
			getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			break;
		case 1:
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
			break;
		case 2:
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
			break;

		default:
			break;
		}
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// NavUtils.navigateUpFromSameTask(this);
			showMenu();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/** 获取用户位置Point(latitude, longitude)，无效坐标返回null，注意此Point未转换为苏州独立坐标 */
	public static Point getCurrentLocation() {
		double latitude = mGpsTracker.getLatitude();
		double longitude = mGpsTracker.getLongitude();
		if (latitude == 0 || longitude == 0) {
			return null;
		}

		LogUtils.d("MainActivity", "longitude:" + longitude + "latitude:"
				+ latitude);
		return new Point(longitude, latitude);
	}

	private Dialog buildDialogWarning(String msg) {
		AlertDialog.Builder builder = new Builder(MainActivity.this);
		builder.setTitle(R.string.alert_dialog_warning);
		builder.setMessage(msg);
		final Dialog mDialog = builder.create();
		mDialog.setCanceledOnTouchOutside(true);
		final Timer t = new Timer();
		t.schedule(new TimerTask() {
			public void run() {
				mDialog.dismiss(); // when the task active then close the dialog
				t.cancel(); // also just top the timer thread, otherwise, you
							// may receive a crash report
			}
		}, 2000);
		return mDialog;
	}

	private Dialog buildDialogExit(final Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(R.string.hint);
		builder.setMessage(R.string.exit_detail);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (MenuFragment.isOnline
								&& Utils.isNetworkAvailable(mContext)) {
							// 发送下线请求，忽略成败
							attemptOnlineOffLine(
									OnlineOfflineAsyncTask.OFFLINE,
									AppApplication.mUser);
						}
						finish();
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

	/** 上线下线,忽略成败 */
	private void attemptOnlineOffLine(String type, User user) {
		OnlineOfflineAsyncTask task = new OnlineOfflineAsyncTask();
		task.execute(type, user);
		task.setOnlineOfflineListener(new IOnlineOfflineListener() {
			@Override
			public void onSuccess(Message msg) {
				// donothing
			}

			@Override
			public void onFailure(Message msg) {
				// donothing

			}
		});
	}

	class UploadUserLocationAsyncTask extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Object doInBackground(Object... args) {
			Calendar c = Calendar.getInstance();
			String time = Utils.formatDate(c.getTime(), "yyyy-MM-dd HH:mm:ss");
			return HttpQuery.uploadLocation(mUser, time);
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);

			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(MainActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (result instanceof String) {
				if ("1".equalsIgnoreCase(result.toString())) {
					LogUtils.d("MainActivity", "上传成功");
				} else {
					// 后台提示，如“GPS坐标不在有效网格内”
					Toast.makeText(MainActivity.this, result.toString(),
							Toast.LENGTH_SHORT).show();
				}

			}

		}
	}

}
