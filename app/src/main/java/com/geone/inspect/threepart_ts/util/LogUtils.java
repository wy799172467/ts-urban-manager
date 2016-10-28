package com.geone.inspect.threepart_ts.util;

import android.util.Log;

public class LogUtils {

	public LogUtils() {
		// TODO Auto-generated constructor stub
	}

	/** 控制log输出,true打印log，false不打印 */
	private static final boolean isDebug = false;
	public static void d(String tag, String msg) {
		if (isDebug) {
			Log.d(tag, msg);
		}
	}
}
