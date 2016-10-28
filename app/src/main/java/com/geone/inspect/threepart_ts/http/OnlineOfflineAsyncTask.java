package com.geone.inspect.threepart_ts.http;

import android.os.AsyncTask;
import android.os.Message;

import com.geone.inspect.threepart_ts.bean.User;
import com.geone.inspect.threepart_ts.util.Utils;

import java.util.Calendar;

public class OnlineOfflineAsyncTask extends AsyncTask<Object, Void, Object> {
	public static final String ONLINE = "上线";
	public static final String OFFLINE = "下线";
	/** 上线成功 */
	public static final int ONLINE_SUCCESS = 1;
	/** 上线失败 */
	public static final int ONLINE_FAILUER = -1;
	/** 下线成功 */
	public static final int OFFLINE_SUCCESS = 2;
	/** 下线失败 */
	public static final int OFFLINE_FAILURE = -2;
	private IOnlineOfflineListener mIOnlineOfflineListener;

	public interface IOnlineOfflineListener {
		public void onSuccess(Message msg);

		public void onFailure(Message msg);
	};

	private String mType;
	public void setOnlineOfflineListener(IOnlineOfflineListener listener) {
		mIOnlineOfflineListener=listener;
	}

	/**
	 * @param args
	 *            [0]:String type 上下线
	 * @param args
	 *            [1]:User user
	 */
	@Override
	protected Object doInBackground(Object... args) {
		mType = (String) args[0];// 上下线
		Calendar c = Calendar.getInstance();
		String time = Utils.formatDate(c.getTime(), "yyyy-MM-dd HH:mm:ss");
		Object ob = HttpQuery.goOnLineOffLine((User) args[1], time, mType);
		return ob;
	}

	@Override
	protected void onPostExecute(Object result) {
		Message msg = Message.obtain();
		// super.onPostExecute(result);
		if (result instanceof Exception) {
			if (mType.equals(ONLINE)) {
				msg.what = ONLINE_FAILUER;
			} else {
				msg.what = OFFLINE_FAILURE;
			}
			msg.obj = (Exception) result;
			mIOnlineOfflineListener.onFailure(msg);
			return;
		}
		if (result instanceof String) {
			if ("1".equalsIgnoreCase(result.toString())) {
				if (mType.equals(ONLINE)) {
					msg.what = ONLINE_SUCCESS;
				} else {
					msg.what = OFFLINE_SUCCESS;
				}
				msg.obj = (String) result;
				mIOnlineOfflineListener.onSuccess(msg);

			} else {
				if (mType.equals(ONLINE)) {
					msg.what = ONLINE_FAILUER;
				} else {
					msg.what = OFFLINE_FAILURE;
				}
				msg.obj = (Exception) result;
				mIOnlineOfflineListener.onFailure(msg);
			}

		}

	}

}
