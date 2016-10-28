package com.geone.inspect.threepart_ts.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import java.io.IOException;

import com.geone.inspect.threepart_ts.R;

public class AudioPlayActivity extends Activity implements OnClickListener {

	private static final String LOG_TAG = "AudioPlayActivity";

	private MediaPlayer mPlayer = null;
	private Button mFinishButton;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setTitle(R.string.record_sound);
		setContentView(R.layout.audio_player);
		mFinishButton = (Button) findViewById(R.id.btn_finish);
		mFinishButton.setOnClickListener(this);

		String audio_path = getIntent().getStringExtra("audio_path");
		String audio_url = getIntent().getStringExtra("audio_url");
		if (audio_path == null || audio_path.isEmpty()) {
			Uri uri = Uri.parse(audio_url);
			startPlaying(uri);// 播放网络音频
		} else {
			startPlaying(audio_path);// 播放本地音频
		}
		//
		// Uri
		// uri=Uri.parse("http://www.mobvcasting.com/android/audio/goodmorningandroid.mp3");

	}

	@Override
	public void onPause() {
		super.onPause();

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	/** 播放本地音频 */
	private void startPlaying(String audio_path) {
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				finish();
			}
		});
		try {			
			mPlayer.setDataSource(audio_path);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	/**
	 * 播放web音频
	 * 
	 * @param audio_uri
	 * 
	 */
	private void startPlaying(Uri audio_uri) {
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				finish();
			}
		});
		try {
			//mPlayer.reset();
			// 根据uri来加载指定的声音文件
			mPlayer.setDataSource(this, audio_uri);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	private void stopPlaying() {
		mPlayer.release();
		mPlayer = null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_finish:
			stopPlaying();
			finish();
			break;
		default:
			break;
		}

	}
}