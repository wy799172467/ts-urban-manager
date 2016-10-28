package com.geone.inspect.threepart_ts.fragment;

import java.io.IOException;

import com.geone.inspect.threepart_ts.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class AudioPlayFragment extends DialogFragment {

	private static final String LOG_TAG = "AudioPlayFragment";

	private MediaPlayer mPlayer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View mView = inflater.inflate(R.layout.audio_player, null);

		String audio_path = getArguments().getString("audio_path");
		startPlaying(audio_path);

		builder.setView(mView)

		.setPositiveButton(R.string.finish,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						stopPlaying();
						dismiss();
					}
				});
		return builder.create();
	}

	private void startPlaying(String audio_path) {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(audio_path);
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

}
