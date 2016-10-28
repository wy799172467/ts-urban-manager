package com.geone.inspect.threepart_ts.http;

import android.util.Log;

import com.esri.core.geometry.Point;
import com.geone.inspect.threepart_ts.activity.AppApplication;
import com.geone.inspect.threepart_ts.adapter.TabPagerAdapter;
import com.geone.inspect.threepart_ts.bean.Accept;
import com.geone.inspect.threepart_ts.bean.Case;
import com.geone.inspect.threepart_ts.bean.CaseImage;
import com.geone.inspect.threepart_ts.bean.CaseRecord;
import com.geone.inspect.threepart_ts.bean.Category;
import com.geone.inspect.threepart_ts.bean.Idiom;
import com.geone.inspect.threepart_ts.bean.User;
import com.geone.inspect.threepart_ts.util.GeometryUtils;
import com.geone.inspect.threepart_ts.util.ImageUtils;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.MapEnum;
import com.geone.inspect.threepart_ts.util.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpQuery {

	// 非配置地址----
	/** 公网ip */
	public static final String apkPublicIp = "58.210.9.134";
	/** 专网ip */
	public static final String vpnIp = "60.175.169.64";
	// 1.登录地址
	/** 公网登录地址 */
	//园区:http://61.177.47.229/sbjcz/DSFXCService/Handler/LoginHandler.ashx?
	//苏滁:http://60.175.169.64:4000/CZYX/SCPSBJ/DSFXCService/Handler/LoginHandler.ashx?
	private static final String URL_PREFIX_LOGIN = "http://60.175.169.64:4000/CZYX/SCPSBJ/DSFXCService/Handler/LoginHandler.ashx?";
	/** 专网登录地址 */
	//private static final String URL_PREFIX_LOGIN_VPN = "http://60.175.169.64:4000/CZYX/SCPSBJ/DSFXCService/Handler/LoginHandler.ashx?";
	/** token地址 */
	// public static final String TOKEN_WEB_PRE =
	// "http://58.210.9.131/SIPSD/tokens/?request=getToken";
	// public static final String URL_CHECK_UPDATE =
	// "http://58.210.9.134/mserv/update/app_version_check/android/dsfxc/";
	// ----非配置地址
	/** 自定义连接超时时间30000 */
	public static final int TIMEOUT_VALUE = 30000;
	/** E_CAUSES = { "ConnectTimeoutException", "JSONException" }; */
	private static final String[] E_CAUSES = { "ConnectTimeoutException",
			"JSONException" };

	private static final String DBTAG = "DBTag";
	private static final String QUERY_SQL = "querySQL";
	/** 存放配置信息中的ServiceList */
	public static HashMap<String, String> serviceMap = new HashMap<String, String>();

	public static Object doLogin(String username, String password) {
		String jsonResults = "";
		String url = "";
		String url_get_code_info = "";
		try {
			// http get

			if (AppApplication.isPubllic) {
				url = URL_PREFIX_LOGIN + "username=" + username + "&pwd="
						+ password;
			} else {
				//url = URL_PREFIX_LOGIN_VPN + "username=" + username + "&pwd=" + password;
			}

			HttpGet requestGet = new HttpGet(url);
			LogUtils.d("HttpQuery", "登录地址："
					+ requestGet.getURI().toURL().toString());
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(requestGet);
			// HttpResponse httpResponse = new DefaultHttpClient()
			// .execute(requestGet);

			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Error("无法连接至服务器.");
			}
			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error") || jsonResults.contains("failed")) {
				return new Exception("无法连接至服务器.");
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray.length() <= 0) {
				return new Error("返回结果为空.");
			}
			JSONObject resultObj = resultArray.getJSONObject(0);
			if (resultObj.has("error")) {
				return new Error("返回结果有误.");
			}
			JSONObject userObj = resultObj.getJSONObject("UserInfo");
			User mUser = new User();
			mUser.userID = userObj.getString("ID");
			mUser.name = userObj.getString("UserName");
			mUser.account = userObj.getString("UserAccount");
			mUser.gridID = userObj.getString("Grid");
			mUser.interval = userObj.getLong("interval");
			mUser.rolename = userObj.optString("rolename", "xcDept");// 默认角色为xcDept
			// mUser.rolename = "yhDept";//测试用

			JSONArray serviceArray = resultObj.getJSONArray("ServiceList");
			if (serviceArray.length() <= 0) {
				return new Error("返回的服务列表为空.");
			}
			for (int i = 0; i < serviceArray.length(); i++) {
				JSONObject obj = serviceArray.getJSONObject(i);
				serviceMap.put(obj.getString("name"), obj.getString("url"));
			}

			// 获取配置信息(代码组)
			if (AppApplication.isPubllic) {
				url_get_code_info = serviceMap.get("GetCodeInfo");
			} else {
				url_get_code_info = serviceMap.get("GetCodeInfo_vpn");
			}
			url_get_code_info = url_get_code_info.replaceAll("#username#",
					username);
			url_get_code_info = url_get_code_info.replaceAll("#pwd#", password);
			url_get_code_info = url_get_code_info.replaceAll("#version#", "0");
			requestGet = new HttpGet(url_get_code_info);
			LogUtils.d("url_get_code_info", url_get_code_info);
			httpResponse = new DefaultHttpClient().execute(requestGet);
			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			JSONObject result = new JSONObject(jsonResults);
			int version = result.getInt("version");
//			int version = Integer.parseInt(result.getString("version"));
			// 判断配置是否有更新, 没有则直接返回

			boolean what =  version <= AppApplication.version || !result.has("config");
			if (what) {

				return mUser;
			}
			AppApplication.version = version;
			mUser.config_version = version;

			JSONObject config = result.getJSONObject("config");
			JSONArray typeArray = config.getJSONArray("type");
			// 将caseType列表存入数据库
			AppApplication.myDataBase
					.insertCaseTypeList(getCategoryListFromJSONArray(typeArray));
			typeArray = config.getJSONArray("LargeClass");
			// 将LargeClass列表存入数据库
			AppApplication.myDataBase
					.insertCaseLargeClassList(getCategoryListFromJSONArray(typeArray));
			typeArray = config.getJSONArray("SmallClass");
			// 将SmallClass列表存入数据库
			AppApplication.myDataBase
					.insertCaseSmallClassList(getCategoryListFromJSONArray(typeArray));
			typeArray = config.getJSONArray("SubClass");
			// 将SubClass列表存入数据库
			AppApplication.myDataBase
					.insertCaseSubClassList(getCategoryListFromJSONArray(typeArray));

			return mUser;
		} catch (Exception e) {
			String eDetail = e.toString();
			if (eDetail.contains(E_CAUSES[0])) {
				eDetail = "连接超时，请检查网络!";
			} else if (eDetail.contains(E_CAUSES[1])
					&& eDetail.contains("status")) {
				eDetail = "帐号或密码错误.";
			} else {
				eDetail = "未知错误.";
			}
			// String eCause = e.getClass().getName();
			Log.i("eDetail", "doLogin: " + eDetail);
			e.printStackTrace();
			return e;
		}
	}

	/** 问题上报 */
	public static Object doReport(User mUser, Case mCase) {
		String jsonResults = "";
		String URL_SUBMIT_CASE_INFO = "";

		try {
			if (AppApplication.isPubllic) {
				URL_SUBMIT_CASE_INFO = serviceMap.get("SubmitCaseInfo");
			} else {
				URL_SUBMIT_CASE_INFO = serviceMap.get("SubmitCaseInfo_vpn");
			}
			int end = URL_SUBMIT_CASE_INFO.indexOf("?");
			URL_SUBMIT_CASE_INFO = URL_SUBMIT_CASE_INFO.substring(0, end + 1);

			MultipartEntity multiPartEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			multiPartEntity.addPart("tablename",
					getCharSettedStringBody("T_CaseInfo_Media"));
			multiPartEntity.addPart("wfType", getCharSettedStringBody("sb"));

			multiPartEntity.addPart("ProblemNo",
					getCharSettedStringBody(mCase.ProblemNo));
			multiPartEntity.addPart("InspectorID",
					getCharSettedStringBody(mUser.userID));
			multiPartEntity.addPart("CaseType",
					getCharSettedStringBody(mCase.CaseTypeDesc));
			// multiPartEntity.addPart("CaseLargeClass",
			// getCharSettedStringBody(mCase.CaseClassIDesc));
			// multiPartEntity.addPart("CaseSmallClass",
			// getCharSettedStringBody(mCase.CaseClassIIDesc));
			// multiPartEntity.addPart("CaseSubClass",
			// getCharSettedStringBody(mCase.CaseClassIIIDesc));
			multiPartEntity.addPart("CaseLargeClass",
					getCharSettedStringBody(mCase.CaseClassI));
			multiPartEntity.addPart("CaseSmallClass",
					getCharSettedStringBody(mCase.CaseClassII));
			multiPartEntity.addPart("CaseSubClass",
					getCharSettedStringBody(mCase.CaseClassIII));
			multiPartEntity.addPart("CreateTime",
					getCharSettedStringBody(mCase.createTime));
			multiPartEntity.addPart("PostTime",
					getCharSettedStringBody(mCase.postTime));
			multiPartEntity.addPart("Address",
					getCharSettedStringBody(mCase.ReportAddress));
			multiPartEntity.addPart("CaseDesc",
					getCharSettedStringBody(mCase.ReportCaseDesc));
			multiPartEntity.addPart("x", new StringBody(mCase.X + ""));
			multiPartEntity.addPart("y", new StringBody(mCase.Y + ""));
			multiPartEntity.addPart("layerid",
					getCharSettedStringBody(mCase.layerId + ""));
			multiPartEntity.addPart("thingid",
					getCharSettedStringBody(mCase.thingId + ""));// 防止为null
			multiPartEntity.addPart("GridID",
					getCharSettedStringBody(mUser.gridID));
			multiPartEntity.addPart("emergency",
					getCharSettedStringBody(mCase.emergency));// 注意若含中文的必须进行编码
			multiPartEntity.addPart("caseCondition",
					getCharSettedStringBody(mCase.caseCondition));
			// 图片
			if (!isImageListNull(mCase.imageList)) {

				for (int i = 0; i < mCase.imageList.size(); i++) {
					CaseImage mImage = mCase.imageList.get(i);

					byte[] bytes = ImageUtils.getCompressedPrintBitmapBytes(
							mImage.path, ImageUtils.WITH_HEIGHTS[0][0],
							ImageUtils.WITH_HEIGHTS[0][1], 100);// 图片质量压缩到100K左右（不准确）

					// ByteArrayOutputStream bos = new ByteArrayOutputStream();
					// bm.compress(CompressFormat.JPEG, 100, bos);
					String imageName = mImage.path.substring(mImage.path
							.lastIndexOf("/") + 1);
					// ContentBody mimePart = new
					// ByteArrayBody(bos.toByteArray(),
					// imageName);
					ContentBody mimePart = new ByteArrayBody(bytes, imageName);
					multiPartEntity.addPart("image" + i, mimePart);
					LogUtils.d("HttpQuery", "上传图片：" + bytes.length / 1024);
					// bos.close();
				}
			}
			// 录音
			if (!isRecordListNull(mCase.recordList)) {
				for (int i = 0; i < mCase.recordList.size(); i++) {
					CaseRecord mRecord = mCase.recordList.get(i);
					File file = new File(mRecord.path);
					ContentBody cbFile = new FileBody(file, "audio/3gp");
					multiPartEntity.addPart("audio" + i, cbFile);
				}
			}
			// Http Post
			HttpPost request = new HttpPost(URL_SUBMIT_CASE_INFO);
			request.setEntity(multiPartEntity);

			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Error("无法连接至服务器.");
			}

			jsonResults = EntityUtils.toString(httpResponse.getEntity());

			JSONArray resultArray = new JSONArray(jsonResults);
			JSONObject result = resultArray.getJSONObject(0);

			int status = result.getInt("status");
			if (status == 0) {
				String errorMsg = result.getString("error");
				return new Error(errorMsg); // 出现异常
			}

			return "案件上报成功";
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}
	}

	/**
	 * 核实核查处理案件，区别在wftype
	 */
	public static Object doHSHCCL(Case mCase) {
		String jsonResults = "";
		String url_submit_record = "";

		try {
			if (AppApplication.isPubllic) {
				url_submit_record = serviceMap.get("SubmitDealRecord");
			} else {
				url_submit_record = serviceMap.get("SubmitDealRecord_vpn");
			}
			int end = url_submit_record.indexOf("?");
			url_submit_record = url_submit_record.substring(0, end + 1);
			// Http Post
			HttpPost request = new HttpPost(url_submit_record);
			MultipartEntity multiPartEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			multiPartEntity.addPart("tablename",
					getCharSettedStringBody("T_CaseInfo_Media"));
			multiPartEntity.addPart("wfType",
					getCharSettedStringBody(mCase.wftype));
			multiPartEntity.addPart("cmdID",
					getCharSettedStringBody(mCase.CmdID));
			multiPartEntity.addPart("staffID",
					getCharSettedStringBody(mCase.InspectorID));
			multiPartEntity.addPart("problemNo",
					getCharSettedStringBody(mCase.ProblemNo));
			multiPartEntity.addPart("dealComment",
					getCharSettedStringBody(mCase.DealComment));
			// liyl 2015-3-11
			// 简便期间，以description替代dealTime字段内容，如有无问题、不同过意见等
			// multiPartEntity.addPart("dealTime", new
			// StringBody(mCase.postTime));
			// 意见建议描述信息
			multiPartEntity.addPart("advice",
					getCharSettedStringBody(mCase.Advice));

			// 上传多媒体---
			// 图片
			// if (!isImageListNull(mCase.imageList)) {
			// for (int i = 0; i < mCase.imageList.size(); i++) {
			// CaseImage mImage = mCase.imageList.get(i);
			// File file = new File(mImage.path);
			// ContentBody cbFile = new FileBody(file, "image/jpeg");
			// multiPartEntity.addPart("image" + i, cbFile);
			// }
			// }
			if (!isImageListNull(mCase.imageList)) {

				for (int i = 0; i < mCase.imageList.size(); i++) {
					CaseImage mImage = mCase.imageList.get(i);

					byte[] bytes = ImageUtils.getCompressedPrintBitmapBytes(
							mImage.path, ImageUtils.WITH_HEIGHTS[0][0],
							ImageUtils.WITH_HEIGHTS[0][1], 100);// 图片质量压缩到200K以内

					String imageName = mImage.path.substring(mImage.path
							.lastIndexOf("/") + 1);
					ContentBody mimePart = new ByteArrayBody(bytes, imageName);
					multiPartEntity.addPart("image" + i, mimePart);
					LogUtils.d("HttpQuery", "上传图片：" + bytes.length / 1024);
				}
			}

			// 录音
			if (!isRecordListNull(mCase.recordList)) {
				for (int i = 0; i < mCase.recordList.size(); i++) {
					CaseRecord mRecord = mCase.recordList.get(i);
					File file = new File(mRecord.path);
					ContentBody cbFile = new FileBody(file, "audio/3gp");
					multiPartEntity.addPart("audio" + i, cbFile);
				}
			}

			request.setEntity(multiPartEntity);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return "无法连接至服务器,上传失败";
			}

			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			JSONArray resultArray = new JSONArray(jsonResults);
			JSONObject result = resultArray.getJSONObject(0);

			int status = result.getInt("status");
			if (status == 0) {
				String errorMsg = result.getString("error");
				return new Error(errorMsg); // 出现异常
			}
			return mCase.ProcessStageDesc + "上报成功";
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}
	}

	/** 获取核实核查处理列表，注意处理服务与其他不同 */
	public static Object getHSHCCLCaseList(String gridID, String userID,
			String pageTitle) {
		String wftype = MapEnum.getWftype(pageTitle);
		String jsonResults = "";
		String url = "";
		String querySQL = "";
		int end;

		try {
			// 待处理or我收录
			if (Case.WFTYPES[2].equalsIgnoreCase(wftype)) {// 待处理
				if (TabPagerAdapter.PAGETITLES_MANAGE[1]
						.equalsIgnoreCase(pageTitle)) {
					if (AppApplication.isPubllic) {
						url = serviceMap.get("GetCaseInfo_CL");
					} else {
						url = serviceMap.get("GetCaseInfo_CL_vpn");
					}
				} else {// 我收录

					if (AppApplication.isPubllic) {
						url = serviceMap.get("GetCaseInfo_CL_Mine");
					} else {
						url = serviceMap.get("GetCaseInfo_CL_Mine_vpn");
					}
				}
				end = url.lastIndexOf("=");
				querySQL = url.substring(end + 1);
				querySQL = querySQL.replaceAll("#userid#", userID);
			} else {// 核实核查

				if (AppApplication.isPubllic) {
					url = serviceMap.get("GetCaseByGridStatus");
				} else {
					url = serviceMap.get("GetCaseByGridStatus_vpn");
				}
				// url = serviceMap.get("GetCaseByGridStatus");
				end = url.lastIndexOf("=");
				querySQL = url.substring(end + 1);
				querySQL = querySQL.replaceAll("#gridID#", gridID);
				querySQL = querySQL.replaceAll("#userid#", userID);// v1.8
				querySQL = querySQL.replaceAll("#wftype#", wftype);
			}

			try {
				querySQL = URLEncoder.encode(querySQL, "UTF-8");
			} catch (UnsupportedEncodingException e1) {

				e1.printStackTrace();
			}
			String queryPre = url.substring(0, end + 1);
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "核实核查处理收录列表：" + queryUrl);
			HttpGet request = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Error("无法连接至服务器");
			}

			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			JSONArray resultArray = new JSONArray(jsonResults);
			int length = resultArray.length();
			if (length == 0) {
				return new ArrayList<Case>(0);
			}
			JSONObject result = resultArray.getJSONObject(0);

			if (result.has("error")) {
				String errorMsg = result.getString("error");
				return new Error(errorMsg); // 出现异常
			}

			ArrayList<Case> caseList = new ArrayList<Case>(length);
			for (int i = 0; i < length; i++) {
				JSONObject attrObj = resultArray.getJSONObject(i);
				Case event = new Case();
				event.wftype = attrObj.optString("wftype", "");// 值为null插入数据库时报错
				event.Shape = attrObj.getString("ShapeString");
				event.CaseID = attrObj.getString("CaseID");
				event.InspectorID = attrObj.getString("inspectorid");
				event.CaseTypeDesc = attrObj.getString("CaseTypeDesc");
				event.CaseClassIDesc = attrObj.getString("CaseClassIDesc");
				event.CaseClassIIDesc = attrObj.getString("CaseClassIIDesc");
				event.CaseClassIIIDesc = attrObj.getString("CaseClassIIIDesc");
				event.ProcessStageDesc = attrObj.getString("ProcessStageDesc");
				event.ProcessResult1 = attrObj.getString("ProcessResult1");
				event.CaseClassI = attrObj.optString("CaseClassI", "");
				event.CaseClassII = attrObj.optString("CaseClassII", "");
				event.CaseClassIII = attrObj.optString("CaseClassIII", "");

				event.CurrentStageStart = attrObj
						.getString("CurrentStageStart");
				event.ReportAddress = attrObj.getString("ReportAddress");
				event.ReportCaseDesc = attrObj.getString("ReportCaseDesc");
				event.IsRead = attrObj.optString("IsRead", "0");
				event.SupervisionStatus=attrObj.optString("SupervisionStatus","0");

				// LogUtils.d("HttpQuery", "X: " + attrObj.getString("X") +
				// ", Y: "
				// + attrObj.getString("Y"));
				String xString = attrObj.getString("X");
				String yString = attrObj.getString("Y");
				event.ProblemNo = attrObj.getString("ProblemNo");
				event.CmdID = attrObj.getString("CmdID");

				if (xString != null && !"".equals(xString)) {
					event.X = Double.parseDouble(xString);
				}
				if (yString != null && !"".equals(yString)) {
					event.Y = Double.parseDouble(yString);
				}
				event.thingId = attrObj.optString("thingId", "");
				// 待处理----
				event.IsCanLSJA = attrObj.optString("IsCanLSJA", "");
				event.sendTo = attrObj.optString("sendTo", "");

				caseList.add(event);
			}

			return caseList;

		} catch (Exception e) {
			// TODO: handle exception
			return e;
		}

	}

	/**
	 * type: 上线/下线 上线必须有位置，下线不做要求(这里统一提交空)
	 */
	public static Object goOnLineOffLine(User user, String time, String type) {
		String jsonResults = "";
		String pointStr = "";
		String url = "";
		try {
			if (!GeometryUtils.isPointEmpty(user.location_point)) {
				// 将经纬度转换为点
				Point szPoint = parseLatLonToPoint(user.location_point);
				// user.location_point =
				// parseLatLonToPoint(user.location_point);
				if (szPoint == null) {
					return new Error("坐标转换失败");
				}
				pointStr = Utils.parsePointToString(szPoint);
			}
			if (AppApplication.isPubllic) {
				url = serviceMap.get("SubmitCardLog");
			} else {
				url = serviceMap.get("SubmitCardLog_vpn");
			}

			int end = url.indexOf("?");
			url = url.substring(0, end + 1);
			// Http Post
			HttpPost request = new HttpPost(url);
			List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			//下线服务的参数，要改成滁州的
			postParameters.add(new BasicNameValuePair(DBTAG, "CZSBJ"));
			String sql = "exec InsertCardLog '" + user.userID + "','"
					+ pointStr + "','" + time + "','" + type + "'";
			postParameters.add(new BasicNameValuePair(QUERY_SQL, sql));
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
					postParameters, HTTP.UTF_8);
			// user.account
			request.setEntity(formEntity);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);

			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Error("无法连接至服务器");
			}

			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			JSONArray resultArray = new JSONArray(jsonResults);

			if (resultArray == null || resultArray.length() == 0) {
			} else {
				JSONObject result = resultArray.getJSONObject(0);
				String rs = result.optString("rs");
				return rs;
			}
			return new Exception(type + "失败");
		} catch (Exception e) {
			// TODO: handle exception
			LogUtils.d("Exception", e.toString());
			return e;
		}

	}

	/**
	 * 上传用户位置
	 */
	public static Object uploadLocation(User user, String time) {
		String jsonResults = "";
		String url = "";
		try {

			// 将经纬度转换为点
			Point szPoint = parseLatLonToPoint(user.location_point);
			// user.location_point = parseLatLonToPoint(user.location_point);
			if (szPoint == null) {
				return new Exception("坐标无法转换，上传失败！");
			}
			if (AppApplication.isPubllic) {
				url = serviceMap.get("SubmitLocus");
			} else {
				url = serviceMap.get("SubmitLocus_vpn");
			}


			// Http Post

			url = url.replace("#UserID#",user.account);
			url = url.replace("#Shape#",Utils.parsePointToString(szPoint));
			url = url.replace("#AddDate#",time);


			int end = url.indexOf("exec");
			String url_prefix = url.substring(0,end+1);
			String url_suffix = url.substring(end+1, url.length());

			url_suffix = URLEncoder.encode(url_suffix,HTTP.UTF_8);

			String newUrl = url_prefix+url_suffix;

			HttpPost request = new HttpPost(newUrl);
			//List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
//			postParameters.add(new BasicNameValuePair(DBTAG, "DSFXC"));
//			String sql = "exec InsertLocus '" + user.account + "','"
//					+ Utils.parsePointToString(szPoint) + "','" + time + "'";
//
//			String sql
			//postParameters.add(new BasicNameValuePair(QUERY_SQL, sql));
			//UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters, HTTP.UTF_8);
			//request.setEntity(formEntity);


			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);

			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
			} else {
				JSONObject result = resultArray.getJSONObject(0);
				String rs = result.optString("RS");
				return rs;
			}
			return new Exception("上传失败！");

		} catch (Exception e) {
			// TODO: handle exception
			return e;
		}
	}

	/**
	 * 请假
	 */
	public static Object askForLeave(User user, String start_time,
			String end_time, String leave_type, String reason) {
		String jsonResults = "";
		String url = "";

		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("SubmitLeave");
			} else {
				url = serviceMap.get("SubmitLeave_vpn");
			}

			int end = url.indexOf("?");
			url = url.substring(0, end + 1);
			// Http Post
			HttpPost request = new HttpPost(url);
			List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
//			postParameters.add(new BasicNameValuePair(DBTAG, "DSFXC"));
			//服务访问写错了
			postParameters.add(new BasicNameValuePair(DBTAG, "CZSBJ"));
			String sql = "exec InsertLeave '" + user.account + "','"
					+ start_time + "','" + end_time + "','" + leave_type
					+ "','" + reason + "'";
			postParameters.add(new BasicNameValuePair(QUERY_SQL, sql));
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
					postParameters, HTTP.UTF_8);

			request.setEntity(formEntity);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);

			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Error("无法连接至服务器");
			}

			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.equals("")) {
				return "请假已提交";
			}
			return new Error(jsonResults);

		} catch (Exception e) {
			// TODO: handle exception
			return e;
		}

	}

	/**
	 * 提交已读回执
	 */
	public static Object submitHaveRead(Case mCase) {
		String jsonResults = "";
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("SubmitHaveRead");
			} else {
				url = serviceMap.get("SubmitHaveRead_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#ProblemNo#", mCase.ProblemNo);// 注意大小写必须严格一致，此处与后台确认一下
			querySQL = querySQL.replaceAll("#staffID#", mCase.InspectorID);
			querySQL = querySQL.replaceAll("#cmdID#", mCase.CmdID);
			querySQL = querySQL.replaceAll("#cmdType#", mCase.wftype);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "提交已读:" + queryUrl);
			HttpGet request = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			return jsonResults;

		} catch (Exception e) {
			// TODO: handle exception
			return e;
		}

	}

	/**
	 * 退回案件userId,caseId,cmdContent
	 */
	public static Object rejectCase(String userId, String caseId,
			String cmdContent) {
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("CancelCmd");
			} else {
				url = serviceMap.get("CancelCmd_vpn");
			}
			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#CmdStaff#", userId);
			querySQL = querySQL.replaceAll("#CaseID#", caseId);
			querySQL = querySQL.replaceAll("#CmdContent#", cmdContent);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "拒签案件:" + queryUrl);
			HttpGet request = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);

			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error")) {
				return new Exception("有错误"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("无数据");
			}
			JSONObject result = resultArray.getJSONObject(0);
			String rs = result.optString("rs", "");// 区分大小写
			return rs;

		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 申请缓办#userid#','#caseid#','#applyDesc#'理由,'#timeLimit#'天数
	 */
	public static Object applyPostpone(String userId, String caseId,
			String applyDesc, String timeLimit) {
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("ApplyInfoHB");
			} else {
				url = serviceMap.get("ApplyInfoHB_vpn");
			}
			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#userid#", userId);
			querySQL = querySQL.replaceAll("#caseid#", caseId);
			querySQL = querySQL.replaceAll("#applyDesc#", applyDesc);
			querySQL = querySQL.replaceAll("#timeLimit#", timeLimit);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "申请缓办:" + queryUrl);
			HttpGet requestGet = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(requestGet);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error")) {
				return new Exception("有错误"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("无数据");
			}
			JSONObject result = resultArray.getJSONObject(0);
			String rs = result.optString("rs", "");
			return rs;

		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 申请延时#userid#','#caseid#','#applyDesc#'理由,'#timeLimit#'天数
	 */
	public static Object applyDelay(String userId, String caseId,
			String applyDesc, String timeLimit) {
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("ApplyDelay");
			} else {
				url = serviceMap.get("ApplyDelay_vpn");
			}
			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#userid#", userId);
			querySQL = querySQL.replaceAll("#caseid#", caseId);
			querySQL = querySQL.replaceAll("#applyDesc#", applyDesc);
			querySQL = querySQL.replaceAll("#timeLimit#", timeLimit);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "申请延时:" + queryUrl);
			HttpGet requestGet = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(requestGet);
			// HttpResponse httpResponse = new DefaultHttpClient()
			// .execute(requestGet);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error")) {
				return new Exception("有错误"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("无数据");
			}
			JSONObject result = resultArray.getJSONObject(0);
			String rs = result.optString("rs", "");
			return rs;

		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 根据点位获取对应地址，成功返回position对应值，失败返回Exception
	 */
	public static Object getAddressByPoint(Point p) {
		String url = "";
		try {
			if (p == null || p.isEmpty()) {
				return new Exception("点位无效");
			}
			if (AppApplication.isPubllic) {
				url = serviceMap.get("GetPositionByPoint");
			} else {
				url = serviceMap.get("GetPositionByPoint_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);

			querySQL = querySQL.replaceAll("#POINT#",
					Utils.parsePointToString(p));
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "获取当前位置地址:" + queryUrl);
			HttpGet requestGet = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(requestGet);

			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			// 返回结果示例[{"position":"停车场"}]
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error") || jsonResults.contains("failed")) {
				return new Exception("暂无数据"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("暂无数据");
			}
			JSONObject result = resultArray.getJSONObject(0);
			// 位置、区域 [{"position":"测绘地理 信息大楼","region":"东沙湖社区"}]
			String rs = result.optString("region", "") + ",";
			rs += result.optString("position");
			if (",".equalsIgnoreCase(rs)) {
				return new Exception("暂无数据");
			}
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return new Exception("地址解析失败");
		}

	}

	/**
	 * 获取意见描述的常用语
	 */
	public static Object getSuggestIdiom(String wftype) {
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("GetCYYList");
			} else {
				url = serviceMap.get("GetCYYList_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#type#", wftype);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "意见描述常用语:" + queryUrl);
			HttpGet request = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error")) {
				return new Exception("暂无数据"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("暂无数据");
			}
			ArrayList<Idiom> idiomList = new ArrayList<Idiom>();
			for (int i = 0; i < resultArray.length(); i++) {
				JSONObject result = resultArray.getJSONObject(i);
				Idiom suggestIdiom = new Idiom();
				suggestIdiom.cyy = result.optString("cyy", "");
				suggestIdiom.xorder = result.optString("xorder", "");
				idiomList.add(suggestIdiom);
			}
			return idiomList;
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 取消收录请求
	 */
	public static Object unsign(String caseId, String userId) {
		String rs = "";
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("RefuseCaseCmd");
			} else {
				url = serviceMap.get("RefuseCaseCmd_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#caseId#", caseId);
			querySQL = querySQL.replaceAll("#userId#", userId);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "取消收录请求:" + queryUrl);
			HttpGet request = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error") || jsonResults.contains("failed")) {
				return new Exception("取消收录失败"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("取消收录失败");
			}
			// JSONObject result = resultArray.getJSONObject(0);
			// rs = result.optString("RS", "");
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 提交收录请求
	 */
	public static Object sign(String caseId, String userId) {
		String rs = "";
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("ReceiveCaseCmd");
			} else {
				url = serviceMap.get("ReceiveCaseCmd_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#caseId#", caseId);
			querySQL = querySQL.replaceAll("#userId#", userId);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "提交收录请求:" + queryUrl);
			HttpGet request = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error") || jsonResults.contains("failed")) {
				return new Exception("收录失败"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("收录失败");
			}
			// JSONObject result = resultArray.getJSONObject(0);
			// rs = result.optString("RS", "");
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 获取问题编号，成功返回problemno对应值，失败返回Exception
	 */
	public static Object getProblemNo(String userId) {
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("GetProblemNo");
			} else {
				url = serviceMap.get("GetProblemNo_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#UserID#", userId);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "获取ProblemNo:" + queryUrl);
			HttpGet request = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			// 返回结果示例[{"problemno":"201504220033"}]
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error") || jsonResults.contains("failed")) {
				return new Exception("暂无数据"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("暂无数据");
			}
			JSONObject result = resultArray.getJSONObject(0);
			String rs = result.optString("problemno", "");
			if ("".equalsIgnoreCase(rs)) {
				return new Exception("暂无数据");
			}
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 查看拒签
	 */
	public static Object getReject(String userId, String caseId) {
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("GetApplyRefuseInfo");
			} else {
				url = serviceMap.get("GetApplyRefuseInfo_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#userid#", userId);
			querySQL = querySQL.replaceAll("#caseid#", caseId);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "查看拒签:" + queryUrl);
			HttpGet request = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error")) {
				return new Exception("暂无数据"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("暂无数据");
			}
			JSONObject result = resultArray.getJSONObject(0);
			Case delayCase = new Case();
			delayCase.applyDate = result.optString("applyDate", "");
			delayCase.applyDesc = result.optString("applyDesc", "");

			delayCase.approveDate = result.optString("approveDate", "");
			delayCase.approvePersonName = result.optString("approvePersonName",
					"");
			delayCase.approveAdvice = result.optString("approveAdvice", "");
			delayCase.isPassDesc = result.optString("isPassDesc", "");
			return delayCase;
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 查看延迟
	 */
	public static Object getDelay(String userId, String caseId) {
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("GetApplyDelayInfo");
			} else {
				url = serviceMap.get("GetApplyDelayInfo_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#userid#", userId);
			querySQL = querySQL.replaceAll("#caseid#", caseId);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "查看延时:" + queryUrl);
			HttpGet request = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error")) {
				return new Exception("暂无数据"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("暂无数据");
			}
			JSONObject result = resultArray.getJSONObject(0);
			Case delayCase = new Case();
			delayCase.applyDate = result.optString("applyDate", "");
			delayCase.applyAsk = result.optString("applyAsk", "");
			delayCase.applyDesc = result.optString("applyDesc", "");

			delayCase.approveDate = result.optString("approveDate", "");
			delayCase.approvePersonName = result.optString("approvePersonName",
					"");
			delayCase.approveAdvice = result.optString("approveAdvice", "");
			delayCase.approveDays = result.optString("Responsetime", "");
			delayCase.isPassDesc = result.optString("isPassDesc", "");
			return delayCase;
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 查看缓办
	 */
	public static Object getPostpone(String userId, String caseId) {
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("GetApplyHBInfo");
			} else {
				url = serviceMap.get("GetApplyHBInfo_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#userid#", userId);
			querySQL = querySQL.replaceAll("#caseid#", caseId);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "查看缓办:" + queryUrl);
			HttpGet request = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			String jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error")) {
				return new Exception("暂无数据"); // 出现异常
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("暂无数据");
			}
			JSONObject result = resultArray.getJSONObject(0);
			Case postponeCase = new Case();
			postponeCase.applyDate = result.optString("applyDate", "");
			// postponeCase.applyAsk = result.optString("applyAsk", "");//申请天数
			postponeCase.applyDesc = result.optString("applyDesc", "");

			postponeCase.approveDate = result.optString("approveDate", "");
			postponeCase.approvePersonName = result.optString(
					"approvePersonName", "");
			postponeCase.approveAdvice = result.optString("approveAdvice", "");
			// postponeCase.approveDays = result.optString("Responsetime",
			// "");//批准天数
			postponeCase.isPassDesc = result.optString("isPassDesc", "");
			return postponeCase;
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 检测案件是否受理
	 */
	public static Object checkAccept(String problemNos) {

		String jsonResults = "";
		String url = "";
		ArrayList<Accept> acceptList = new ArrayList<Accept>();
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("GetReceiveByProblemNo");
			} else {
				url = serviceMap.get("GetReceiveByProblemNo_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#ProblemNoList#", problemNos);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "检测案件是否受理:" + queryUrl);
			HttpGet requestGet = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(requestGet);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}
			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			if (jsonResults.contains("error")
					|| (jsonResults.contains("failed"))) {
				return new Exception("暂无数据");
			}
			JSONArray resultArray = new JSONArray(jsonResults);
			if (resultArray == null || resultArray.length() == 0) {
				return new Exception("暂无数据");
			} else {
				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject result = resultArray.getJSONObject(i);
					Accept accept = new Accept();
					accept.ProblemNo = result.optString("ProblemNo");
					accept.IsAccept = result.optString("IsAccept");
					acceptList.add(accept);
				}
			}
			return acceptList;

		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/**
	 * 提交任务已处理回执 RS=1:未处理，可以提交；0：已处理，不能再提交
	 */
	public static Object checkHaveDone(Case mCase) {
		String jsonResults = "";
		String url = "";
		try {
			if (AppApplication.isPubllic) {
				url = serviceMap.get("WF_IsCanSubmit");
			} else {
				url = serviceMap.get("WF_IsCanSubmit_vpn");
			}

			int end = url.lastIndexOf("=");
			String queryPre = url.substring(0, end + 1);
			String querySQL = url.substring(end + 1);
			querySQL = querySQL.replaceAll("#ProblemNo#", mCase.ProblemNo);
			querySQL = querySQL.replaceAll("#wftype#", mCase.wftype);
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
			String queryUrl = queryPre + querySQL;
			LogUtils.d("HttpQuery", "checkHaveDoneURL:" + queryUrl);
			HttpGet requestGet = new HttpGet(queryUrl);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(requestGet);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return new Exception("无法连接至服务器");
			}

			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			JSONArray resultArray = new JSONArray(jsonResults);
			JSONObject result = resultArray.getJSONObject(0);
			String rs = result.optString("RS", "");
			return rs;

		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}

	}

	/** 将经纬度转换为point */
	public static Point parseLatLonToPoint(Point p) {
		String jsonResults = "";
		String url = "";
		try {
			//
			if (AppApplication.isPubllic) {
				url = serviceMap.get("gps2df");
			} else {
				url = serviceMap.get("gps2df_vpn");
			}

			url = url.replaceAll("#longitude#", p.getX() + "");
			url = url.replaceAll("#latitude#", p.getY() + "");
			LogUtils.d("HttpQuery", "坐标转换：" + url);

			HttpGet request = new HttpGet(url);
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_VALUE);// 设置连接超时
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_VALUE);// 设置读取超时
			HttpResponse httpResponse = httpClient.execute(request);

			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				return null;
			}

			jsonResults = EntityUtils.toString(httpResponse.getEntity());
			JSONObject result = new JSONObject(jsonResults);
			double x = result.getDouble("east");
			double y = result.getDouble("north");
			return new Point(x, y);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ArrayList<Category> getCategoryListFromJSONArray(
			JSONArray typeArray) {
		int length = typeArray.length();

		ArrayList<Category> typeList = new ArrayList<Category>(length);
		for (int i = 0; i < length; i++) {
			JSONObject obj;
			try {
				obj = typeArray.getJSONObject(i);
				Category mCategory = new Category();
				mCategory.value = obj.getString("value");
				mCategory.description = obj.getString("desc");
				typeList.add(mCategory);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		return typeList;
	}

	private static StringBody getCharSettedStringBody(String source) {
		try {
			return new StringBody(source, Charset.defaultCharset());
		} catch (Exception e) {
			try {
				return new StringBody(source);
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				return null;
			}
		}

	}

	private static boolean isImageListNull(ArrayList<CaseImage> objList) {
		if (objList != null && objList.size() > 0) {
			return false;
		}

		return true;
	}

	private static boolean isRecordListNull(ArrayList<CaseRecord> objList) {
		if (objList != null && objList.size() > 0) {
			return false;
		}

		return true;
	}
}
