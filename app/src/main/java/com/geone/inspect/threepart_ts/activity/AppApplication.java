package com.geone.inspect.threepart_ts.activity;

import com.geone.inspect.threepart_ts.bean.User;
import com.geone.inspect.threepart_ts.sql.MyDatabase;

import android.support.multidex.MultiDexApplication;

public class AppApplication extends MultiDexApplication {
	// 配置信息{-----
	/** 配置信息版本 */
	public static int version = -1;
	public static final String APP_ID = "SBJCZ";
	/** 离线图层存放目录 */
	public static final String LOCAL_LAYERS = "layers";
	/** 基础地图tpk名称 */
	public static final String BASE_MAP_TPK_NAME = "PUBLICMAP1000.tpk";
	// 配置信息}-----
	/** 主要供工具类使用 */
	private static AppApplication mAppApplication;

	public static String session_key = null;
	public static MyDatabase myDataBase;
	public static User mUser;
	/** 是否公网登录，默认true,false时为vpn登录 */
	public static boolean isPubllic = true;

	// public static final String BASE_MAPLAYER_URL =
	// "http://58.210.9.131/sipsd/rest/services/SIPSD/SPS25D/MapServer";
	// public static final String TEST_MAPLAYER_URL
	// ="http://192.168.34.3:6080/arcgis/rest/services/WKID_TEST/MapServer";

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mAppApplication = this;
		// AVOSCloud.initialize(this,
		// "56148b2k9bou4scjq51lje8a1yaokan6dg42cakt667x8xt9",
		// "yu1djeo5ybdai31fe77s8c7y52xic22lx1wct2nc98irixfd");
		// AVAnalytics.enableCrashReport(this, true);

	}

	public static AppApplication getApplication() {
		return mAppApplication;
	}

}
