package com.geone.inspect.threepart_ts.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.util.LogUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class CCommentsListActivity extends ListActivity implements
		OnItemClickListener {

	public static final String COMMENT_LIST = "prefs_common_comments";

	private SharedPreferences mDefaultPrefs;

	private static ArrayAdapter<String> listAdapter;

	private Set<String> commentSet;

	private boolean isEditing;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_comments);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mDefaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		commentSet = mDefaultPrefs.getStringSet(COMMENT_LIST,
				new HashSet<String>());
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new ArrayList<String>(
						commentSet));
		setListAdapter(listAdapter);
		// getListView().setOnItemClickListener(this);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		setTitle(R.string.common_comments);
		if (isEditing) {
			menu.clear();
			getMenuInflater().inflate(R.menu.comments_edit, menu);
		} else {
			menu.clear();
			getMenuInflater().inflate(R.menu.comments, menu);
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.comments, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.add:
			buildDialogAddComment(this).show();
			return true;
		case R.id.remove:
			isEditing = true;
			invalidateOptionsMenu();
			editListView();
			return true;
		case R.id.accept:
			isEditing = false;
			invalidateOptionsMenu();
			SparseBooleanArray sp = getListView().getCheckedItemPositions();
			int size = sp.size();
			LogUtils.d("CCommentsListActivity", "sp.size: " + size);
			if (size > 0) {
				commentSet = mDefaultPrefs.getStringSet(COMMENT_LIST,
						new HashSet<String>());
				HashSet<String> mHashSet = new HashSet<String>(commentSet);
				for (int i = 0; i < sp.size(); i++) {
					if (sp.valueAt(i) == true) {
						String checkedItem = (String) getListView()
								.getItemAtPosition(sp.keyAt(i));
						mHashSet.remove(checkedItem);
					}

				}
				listAdapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1,
						new ArrayList<String>(mHashSet));
				listAdapter.notifyDataSetChanged();

				mDefaultPrefs.edit().putStringSet(COMMENT_LIST, mHashSet)
						.commit();
			}

			restoreListView();
			return true;
		case R.id.cancel:
			isEditing = false;
			invalidateOptionsMenu();
			restoreListView();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void editListView() {
		Set<String> commentSet = mDefaultPrefs.getStringSet(COMMENT_LIST,
				new HashSet<String>());
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice,
				new ArrayList<String>(commentSet));
		setListAdapter(listAdapter);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setOnItemClickListener(this);

	}

	private void restoreListView() {
		Set<String> commentSet = mDefaultPrefs.getStringSet(COMMENT_LIST,
				new HashSet<String>());
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new ArrayList<String>(
						commentSet));
		setListAdapter(listAdapter);
		getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
		getListView().setOnItemClickListener(null);

	}

	private Dialog buildDialogAddComment(final Context context) {
		AlertDialog.Builder builder = new Builder(context);
		// Get the layout inflater
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_add_comment, null);
		final EditText mEditText = (EditText) v.findViewById(R.id.et_text);
		builder.setView(v)
				// Add action buttons
				.setPositiveButton(R.string.alert_dialog_add,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								String comment = mEditText.getText().toString();
								if (comment == null || "".equals(comment)) {
									mEditText.setError("输入内容才能添加");
									return;
								} else {
									mEditText.setError(null);
								}
								commentSet = mDefaultPrefs.getStringSet(
										COMMENT_LIST, new HashSet<String>());
								HashSet<String> ss = new HashSet<String>(
										commentSet);
								ss.add(comment);
								mDefaultPrefs.edit()
										.putStringSet(COMMENT_LIST, ss)
										.commit();

								listAdapter = new ArrayAdapter<String>(context,
										android.R.layout.simple_list_item_1,
										new ArrayList<String>(ss));
								setListAdapter(listAdapter);

							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						});
		return builder.create();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		SparseBooleanArray sp = getListView().getCheckedItemPositions();
		int size = sp.size();
		int select_count = 0;
		LogUtils.d("CCommentsListActivity", "--- onItemClick ---");
		if (size > 0) {
			for (int i = 0; i < sp.size(); i++) {
				if (sp.valueAt(i) == true) {
					select_count++;
				}
			}
		}

		if (select_count == 0) {
			setTitle(R.string.common_comments);
		} else {
			setTitle("已选择了 " + select_count + " 条");
		}

	}

}