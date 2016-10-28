package com.geone.inspect.threepart_ts.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.User;

public class SettingsActivity extends Activity {

	public static final String INTERVAL = "prefs_interval";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		User user = (User) getIntent().getSerializableExtra("user");
		SettingsFragment SettingsFragment = new SettingsFragment(SettingsActivity.this);
		Bundle bundle = new Bundle();
		bundle.putSerializable("user", user);
		SettingsFragment.setArguments(bundle);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, SettingsFragment).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// NavUtils.navigateUpFromSameTask(this);
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static class SettingsFragment extends PreferenceFragment {

		private User mUser;
		private Preference mIntervalPreference;
		private Context mcontext;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mUser = (User) getArguments().get("user");
			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
			mIntervalPreference = (Preference) findPreference(INTERVAL);
			mIntervalPreference.setSummary(mUser.interval + " 秒");

		}

		//传递上下文对象
		@SuppressLint("ValidFragment")
		public SettingsFragment(Context context) {
			mcontext=context;
		}

		public SettingsFragment() {
		}

		//设置监听器，使用监听器进行页面跳转
		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
			if(preference==findPreference("prefs_common_comments")){
				Intent intent=new Intent(mcontext,CCommentsListActivity.class);
				mcontext.startActivity(intent);//实现页面跳转
			}

			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}

		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
		}

		@Override
		public void onPause() {
			// TODO Auto-generated method stub
			super.onPause();

		}


	}
}