package com.geone.inspect.threepart_ts.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.io.UserCredentials;
import com.geone.inspect.threepart_ts.activity.AppApplication;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.IBean;
import com.geone.inspect.threepart_ts.bean.Idiom;
import com.geone.inspect.threepart_ts.bean.Leave;
import com.geone.inspect.threepart_ts.bean.Performance;
import com.geone.inspect.threepart_ts.bean.Push;
import com.geone.inspect.threepart_ts.http.HttpQuery;

public class Utils {

	private static Dialog wDialog;

	private static UserCredentials uc = null;

	public static void showWaitingDialog(Context context) {
		if (wDialog == null) {
			wDialog = new Dialog(context, R.style.CustomDialogTheme);
		}
		wDialog.setContentView(R.layout.loading);
		wDialog.setCancelable(false);
		wDialog.show();
	}

	public static void hideWaitingDialog() {
		if (wDialog != null && wDialog.isShowing()) {
			wDialog.cancel();
			wDialog = null;
		}
	}

	/** 根据固定tokenService获取UserCredentials */
	public static UserCredentials getUserCredentials() {
		if (uc == null) {
			uc = new UserCredentials();
			String url = "";
			if (AppApplication.isPubllic) {
				url = HttpQuery.serviceMap.get("token");
			} else {
				url = HttpQuery.serviceMap.get("token_vpn");
			}
			uc.setTokenServiceUrl(url);
			uc.setUserAccount("sipsd", "sipsd123");
		}
		return uc;
	}

	/** 根据tokenService获取UserCredentials */
	public static UserCredentials getUserCredentials(String tokenService) {
		// http://58.210.9.131/SIPSD/tokens/?request=getToken&username=sipsd&password=sipsd123
		UserCredentials uc = new UserCredentials();
		int end = tokenService.indexOf("&");
		tokenService = tokenService.substring(0, end);
		uc.setTokenServiceUrl(tokenService);
		uc.setUserAccount("sipsd", "sipsd123");

		return uc;
	}

	public static String formatDate(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static Long persistDate(Date date) {
		if (date != null) {
			return date.getTime();
		}
		return null;
	}

	public static Date loadDate(long milliseconds) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(milliseconds);
		return c.getTime();
	}

	public static Calendar stringToCalendar(String strDate, String format,
			TimeZone timezone) {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(timezone);
		Date date;
		try {
			date = sdf.parse(strDate);
			cal.setTime(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cal;
	}

	private static String formatPoint(Point p) {
		return p.getX() + " " + p.getY();
	}

	public static String parsePointToString(Point p) {
		return "POINT (" + formatPoint(p) + ")";
	}

	public static Polygon parseStringToPolygon(String shapeString) {
		Polygon mPolygon = new Polygon();
		shapeString = shapeString.replace("POLYGON ((", "");
		shapeString = shapeString.replace("))", "");

		String[] points = shapeString.split(", ");
		for (int i = 0; i < points.length; i++) {
			String pointStr = points[i];
			String[] xy = pointStr.split(" ");

			double x = Double.parseDouble(xy[0]);
			double y = Double.parseDouble(xy[1]);

			Point p = new Point(x, y);
			if (i == 0) {
				mPolygon.startPath(p);
			} else {
				mPolygon.lineTo(p);
			}
		}

		return mPolygon;

	}

	public static Polyline parseStringToPolyline(String shapeString) {
		Polyline mPolyline = new Polyline();
		shapeString = shapeString.replace("LINESTRING (", "");
		shapeString = shapeString.replace(")", "");
		String[] points = shapeString.split(",");

		for (int i = 0; i < points.length; i++) {
			String pointStr = points[i].trim();
			String[] xy = pointStr.split(" ");

			double x = Double.parseDouble(xy[0]);
			double y = Double.parseDouble(xy[1]);

			Point p = new Point(x, y);
			if (i == 0) {
				mPolyline.startPath(p);
			} else {
				mPolyline.lineTo(p);
			}
		}

		return mPolyline;
	}

	public static Point parseStringToPoint(String shapeString) {
		Point p = new Point();
		shapeString = shapeString.replace("POINT (", "");
		shapeString = shapeString.replace(")", "");
		String[] xy = shapeString.split(" ");
		p.setX(Double.parseDouble(xy[0]));
		p.setY(Double.parseDouble(xy[1]));

		return p;
	}

	public static Polygon getCircle(Point center, double radius) {
		Polygon polygon = new Polygon();
		getCircle(center, radius, polygon);
		return polygon;
	}

	private static void getCircle(Point center, double radius, Polygon circle) {
		circle.setEmpty();
		Point[] points = getPoints(center, radius);
		circle.startPath(points[0]);
		for (int i = 1; i < points.length; i++)
			circle.lineTo(points[i]);
	}

	private static Point[] getPoints(Point center, double radius) {
		Point[] points = new Point[50];
		double sin;
		double cos;
		double x;
		double y;
		for (double i = 0; i < 50; i++) {
			sin = Math.sin(Math.PI * 2 * i / 50);
			cos = Math.cos(Math.PI * 2 * i / 50);
			x = center.getX() + radius * sin;
			y = center.getY() + radius * cos;
			points[(int) i] = new Point(x, y);
		}
		return points;
	}

	public static IBean parseJSONToBean(JSONObject obj) {
		IBean mIBean = new IBean();

		try {
			if (obj.has("error")) {
				return null;
			}
			mIBean.id = obj.getString("id");
			mIBean.iLayerType = obj.getString("tablename");
			String shape = obj.getString("shape");
			if (shape.contains("POLYGON")) {
				Polygon mPolygon = Utils.parseStringToPolygon(shape);
				mIBean.shape = mPolygon;
			} else if (shape.contains("POINT")) {
				Point mPoint = Utils.parseStringToPoint(shape);
				mIBean.shape = mPoint;
			} else if (shape.contains("LINESTRING")) {
				mIBean.shape = Utils.parseStringToPolyline(shape);
			}
			JSONObject attrObj = obj.optJSONObject("showlist");// 只取showlist不取attribute
			mIBean.showlistMap = new LinkedHashMap<String, String>(
					attrObj.length());
			Iterator<String> iterator = attrObj.keys();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = attrObj.getString(key);
				if (value != null) {
					value = value.trim();
				}
				mIBean.showlistMap.put(key, value);
			}

			return mIBean;

		} catch (JSONException e) {
			e.printStackTrace();

			return null;
		}
	}

	/** 将JSONObject解析为Push */
	public static Push parseJSONToPush(JSONObject obj) {
		Push push = new Push();
		if (obj == null || obj.has("error")) {
			return null;
		}
		push.ID = obj.optString("ID");
		push.EndDate = obj.optString("EndDate");
		push.StartDate = obj.optString("StartDate");
		push.Title = obj.optString("Title");
		push.Content = obj.optString("Content");
		push.Emergency = obj.optString("Emergency");
		return push;

	}

	/** 将JSONObject解析为Leave */
	public static Leave parseJSONToLeave(JSONObject obj) {
		Leave leave = new Leave();
		if (obj == null || obj.has("error")) {
			return null;
		}
		leave.ID = obj.optString("ID");
		leave.EndDate = obj.optString("EndDate");
		leave.StartDate = obj.optString("StartDate");
		leave.IsPass = obj.optString("IsPass");
		leave.Reason = obj.optString("Reason");
		leave.LeaveType = obj.optString("LeaveType");
		return leave;

	}

	/** 将JSONObject解析为Performance */
	public static Performance parseJSONToPerformance(JSONObject obj) {
		Performance mPerformance = new Performance();
		if (obj == null || obj.has("error")) {
			return null;
		}

		try {
			mPerformance.key = obj.optString("rateType");
			mPerformance.value = obj.optString("score");
			mPerformance.xorder = obj.getInt("xorder");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mPerformance;

	}

	/**
	 * 初始化 AutoCompleteTextView的下拉列表
	 * 
	 * @return
	 */
	public static ArrayAdapter<Idiom> initAutoCompleteTextView(
			Context mContext, AutoCompleteTextView atuoText,
			List<Idiom> idiomList) {
		ArrayAdapter<Idiom> listAdapter = new ArrayAdapter<Idiom>(mContext,
				android.R.layout.simple_list_item_1, idiomList);
		atuoText.setAdapter(listAdapter);
		atuoText.setThreshold(1);
		atuoText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				AutoCompleteTextView autoV = (AutoCompleteTextView) v;
				autoV.showDropDown();
			}
		});
		;
		atuoText.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				AutoCompleteTextView autoV = (AutoCompleteTextView) v;
				autoV.showDropDown();
				return false;
			}
		});
		return listAdapter;
	}

	/** 初始化 AutoCompleteTextView的下拉列表 */
	public static void initAutoCompleteTextView(Context mContext,
			AutoCompleteTextView atuoText, Set<String> stringSet) {
		ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_list_item_1, new ArrayList<String>(
						stringSet));
		atuoText.setAdapter(listAdapter);
		atuoText.setThreshold(1);
		atuoText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				AutoCompleteTextView autoV = (AutoCompleteTextView) v;
				autoV.showDropDown();
			}
		});
		;
		atuoText.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				AutoCompleteTextView autoV = (AutoCompleteTextView) v;
				autoV.showDropDown();
				return false;
			}
		});
	}

	/** 将raw下filName文件拷贝到sd卡filePath目录下，成功返回true，失败返回false */
	public static boolean copyRawFile2SD(Context context, int resId,
			String filName, String filePath) {
		String fileFullName = filePath + filName;
		try {
			File dir = new File(filePath);
			// 如果目录不存在，创建这个目录
			if (!dir.exists())
				dir.mkdir();
			// 获得raw文件的InputStream对象
			InputStream fis = context.getResources().openRawResource(resId);
			FileOutputStream fos = new FileOutputStream(fileFullName);
			byte[] buffer = new byte[8192];
			int count = 0;
			// 开始复制文件
			while ((count = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, count);
			}
			fos.close();
			fis.close();

		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}
		return true;
	}

	/** 判断网络是否链接 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 方法1：此方法只能检测是否连接网络不能确定网络是否可用
		// if (cm == null) {
		// return false;
		// } else {
		// NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		//
		// if (networkInfo == null) {
		// return false;
		// } else {
		// return networkInfo.isAvailable();
		// }
		//
		// }
		// 方法2：此方法只能检测是否连接网络不能确定网络是否可用
		if (cm != null) {
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;

	}

	/**
	 * 根据filePath判断SD卡上是否寸在该文件(夹)
	 * 
	 * @return
	 */
	public static boolean isFileExist(String filePath) {
		if (isSDCardExist()) {
			try {
				File f = new File(filePath);
				if (!f.exists()) {
					return false;
				} else {
					return true;
				}

			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}

	}

	/**
	 * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
	 * 
	 * @return
	 */
	public static boolean isSDCardExist() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取SD卡根目录路径/storage/emulated/0，注意最后无分隔符
	 * 
	 * @return
	 */
	public static String getSDCardPath() {
		boolean exist = isSDCardExist();
		String sdpath = "";
		if (exist) {
			sdpath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		} else {
			// sdpath = "不适用";
		}
		return sdpath;

	}

	/** 获取本地tpk地址含扩展名 */
	public static String getLocalTpkPath(String tpkName) {

		String localTpkPath = getSDCardPath() + File.separator
				+ AppApplication.APP_ID + File.separator
				+ AppApplication.LOCAL_LAYERS + File.separator + tpkName;
		File f = new File(localTpkPath);
		if (f.exists()) {
			return localTpkPath;
		} else {
			return null;
		}
	}

	/** 获取本地离线地图，成功ArcGISLocalTiledLayer，失败null */
	public static ArcGISLocalTiledLayer getLocalLayer(String tpkName) {
		ArcGISLocalTiledLayer localTLayer = null;
		if (!TextUtils.isEmpty(tpkName)) {
			String localTpkPath = getLocalTpkPath(tpkName);
			if (localTpkPath != null) {
				localTLayer = new ArcGISLocalTiledLayer(localTpkPath);
			}
		}
		return localTLayer;
	}

	/** 获取ArcGISTiledMapServiceLayer，失败返回null */
	public static ArcGISTiledMapServiceLayer getTiledLayer(String url) {
		ArcGISTiledMapServiceLayer tiledLayer = null;
		if (!TextUtils.isEmpty(url)) {
			tiledLayer = new ArcGISTiledMapServiceLayer(url,
					getUserCredentials());
		}
		return tiledLayer;
	}

	/** 根据key获取公网地址或专网地址 */
	public static String getPublicVpnUrl(String key) {
		String url = "";
		if (AppApplication.isPubllic) {
			url = HttpQuery.serviceMap.get(key);
		} else {
			url = HttpQuery.serviceMap.get(key + "_vpn");
		}
		return url;
	}

	/** 将类似"0,2"字符串转换为数组 {0,2} */
	public static int[] parseString2IntArray(String inStr) {
		if (inStr == null || inStr.isEmpty()) {
			return null;
		}
		String[] subStrs = inStr.split(",");
		int[] subIds = new int[subStrs.length];
		int i = 0;
		for (String subStr : subStrs) {
			Integer mInteger = Integer.parseInt(subStr);
			subIds[i] = mInteger.intValue();
			i++;
		}
		return subIds;
	}

	// 获取intent---------------------------
	/** android获取一个用于打开HTML文件的intent */
	public static Intent getHtmlFileIntent(String param) {
		Uri uri = Uri.parse(param).buildUpon()
				.encodedAuthority("com.android.htmlfileprovider")
				.scheme("content").encodedPath(param).build();
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setDataAndType(uri, "text/html");
		return intent;
	}

	/** android获取一个用于打开图片文件的intent */
	public static Intent getImageFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "image/*");
		return intent;
	}

	/** android获取一个用于打开PDF文件的intent */
	public static Intent getPdfFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/pdf");
		return intent;
	}

	/** android获取一个用于打开raw文件夹下PDF文件的intent */
	public static Intent getRawPdfFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// Uri uri = Uri.fromFile(new File(param));
		Uri uri = Uri.parse(param);
		intent.setDataAndType(uri, "application/pdf");
		return intent;
	}

	/** android获取一个用于打开文本文件的intent */
	public static Intent getTextFileIntent(String param, boolean paramBoolean) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (paramBoolean) {
			Uri uri1 = Uri.parse(param);
			intent.setDataAndType(uri1, "text/plain");
		} else {
			Uri uri2 = Uri.fromFile(new File(param));
			intent.setDataAndType(uri2, "text/plain");
		}
		return intent;
	}

	/** android获取一个用于打开音频文件的intent */
	public static Intent getAudioFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "audio/*");
		return intent;
	}

	/** android获取一个用于打开视频文件的intent */
	public static Intent getVideoFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "video/*");
		return intent;
	}

	/** android获取一个用于打开CHM文件的intent */
	public static Intent getChmFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/x-chm");
		return intent;
	}

	/** android获取一个用于打开Word文件的intent */
	public static Intent getWordFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/msword");
		return intent;
	}

	/** android获取一个用于打开Excel文件的intent */
	public static Intent getExcelFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.ms-excel");
		return intent;
	}

	/** android获取一个用于打开PPT文件的intent */
	public static Intent getPptFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		return intent;
	}

	/** android获取一个用于打开PPT文件的intent */
	public static boolean testPDFReader(Context mContext,Intent intent){
		List<ResolveInfo> list =  mContext.getPackageManager().queryIntentActivities(intent, 0);
		if(list.size() > 0){
			for(int i=0;i<list.size();i++){
				ResolveInfo ri=list.get(i);
				if(!ri.activityInfo.taskAffinity.contains("tencent")){
					return true;
				}
			}
		}else {
			return false;
		}
		return false;
	}

}
