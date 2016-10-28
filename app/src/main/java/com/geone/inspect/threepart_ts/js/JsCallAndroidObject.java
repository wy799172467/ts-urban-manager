package com.geone.inspect.threepart_ts.js;

import com.geone.inspect.threepart_ts.activity.AudioPlayActivity;
import com.geone.inspect.threepart_ts.activity.PicViewActivity;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.util.LogUtils;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JsCallAndroidObject {
	Context mContext;

	public JsCallAndroidObject(Context c) {
		mContext = c;
	}

	@JavascriptInterface
	public void showWebImg(String url) {
		if (url == null || "".equalsIgnoreCase(url)) {
			Toast.makeText(mContext, R.string.no_data, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		LogUtils.d("JsCallAndroidObject", "图片url：" + url);
		Intent intent = new Intent(mContext, PicViewActivity.class);
		intent.putExtra("image_url", url);
		mContext.startActivity(intent);
	}

	@JavascriptInterface
	public void playWebAudio(String url) {
		if (url == null || "".equalsIgnoreCase(url)) {
			Toast.makeText(mContext, R.string.no_data, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		LogUtils.d("JsCallAndroidObject", "声音url：" + url);
		Intent intent = new Intent(mContext, AudioPlayActivity.class);
		intent.putExtra("audio_url", url);
		mContext.startActivity(intent);
	}

}
