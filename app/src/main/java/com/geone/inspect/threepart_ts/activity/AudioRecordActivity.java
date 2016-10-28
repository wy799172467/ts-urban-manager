package com.geone.inspect.threepart_ts.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.android.photobyintent.AlbumStorageDirFactory;
import com.example.android.photobyintent.BaseAlbumDirFactory;
import com.example.android.photobyintent.FroyoAlbumDirFactory;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.util.LogUtils;

public class AudioRecordActivity extends Activity implements OnClickListener {
	private static final String AUDIO_FILE_PREFIX = "AUDIO_";
	private static final String AUDIO_FILE_SUFFIX = ".mp3";
	private static final String LOG_TAG = "AudioRecordTest";

	private Button mRecordButton = null;
	private MediaRecorder mRecorder = null;

	private Button mPlayButton = null;
	private MediaPlayer mPlayer = null;
	private LinearLayout linearLayout_OK;
	private ProgressBar pbRecording;
	private Button mFinishButton;

	private boolean mStartRecording = true;
	private boolean mStartPlaying = true;

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private static String mCurrentAudioPath;

	private void onRecord(boolean start) {
		if (start) {
			startRecording();
			pbRecording.setVisibility(View.VISIBLE);
		} else {
			stopRecording();
			pbRecording.setVisibility(View.GONE);
		}
	}

	private void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}

	private void startPlaying() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mCurrentAudioPath);
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

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mCurrentAudioPath);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	public AudioRecordActivity() {

	}

	private File createAudioFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = AUDIO_FILE_PREFIX + timeStamp + "_";
		File audio = File.createTempFile(imageFileName, AUDIO_FILE_SUFFIX,
				getAlbumDir());
		mCurrentAudioPath = audio.getAbsolutePath();
		return audio;
	}

	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.app_name);
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory
					.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						LogUtils.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setTitle(R.string.record_sound);
		setContentView(R.layout.recording);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}

		try {
			createAudioFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			mCurrentAudioPath = null;
			e.printStackTrace();
		}

		mRecordButton = (Button) findViewById(R.id.btn_record);
		mPlayButton = (Button) findViewById(R.id.btn_playing);
		linearLayout_OK = (LinearLayout) findViewById(R.id.linearLayout_ok);
		pbRecording = (ProgressBar) findViewById(R.id.progressBar_recording);
		mFinishButton = (Button) findViewById(R.id.btn_finish);

		mRecordButton.setOnClickListener(this);
		mPlayButton.setOnClickListener(this);
		mFinishButton.setOnClickListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_record:
			onRecord(mStartRecording);
			if (mStartRecording) {
				mRecordButton.setText(R.string.stopRecording);
			} else {
				mRecordButton.setText(R.string.startRecording);
				try {
					Thread.sleep(100);
					mRecordButton.setVisibility(View.GONE);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				linearLayout_OK.setVisibility(View.VISIBLE);
			}
			mStartRecording = !mStartRecording;
			break;
		case R.id.btn_playing:
			onPlay(mStartPlaying);
			if (mStartPlaying) {
				mPlayButton.setText(R.string.stopPlaying);
			} else {
				mPlayButton.setText(R.string.startPlaying);
			}
			mStartPlaying = !mStartPlaying;
			break;
		case R.id.btn_finish:
			// finish();
			Intent i = new Intent();
			i.putExtra("audio_path", mCurrentAudioPath);
			setResult(RESULT_OK, i);
			finish();
			break;
		default:
			break;
		}

	}
}