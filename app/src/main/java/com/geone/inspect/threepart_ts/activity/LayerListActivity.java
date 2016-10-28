package com.geone.inspect.threepart_ts.activity;

import java.util.ArrayList;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.adapter.LayerListAdapter;
import com.geone.inspect.threepart_ts.bean.Layer;
import com.geone.inspect.threepart_ts.sql.MyDatabase;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ListView;

/**
 *  离线包管理界面
 */
public class LayerListActivity extends ListActivity {
	/** 初始化列表的List */
	private ArrayList<Layer> mLayerList = new ArrayList<Layer>();
	private ArrayList<String> checkedItemIDs = new ArrayList<String>();
	private LayerListAdapter mLayerListAdapter;
	private Context mContext;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layer_manage);
		mContext = this;
		// Set up the action bar.
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mLayerList=(ArrayList<Layer>) getIntent().getSerializableExtra("layerList");
		updateLayerList(mLayerList);// 此处更新是为了使Item上显示更新
		mLayerListAdapter = new LayerListAdapter(this, mLayerList,
				checkedItemIDs);
		mLayerListAdapter.mListView = getListView();
		setListAdapter(mLayerListAdapter);

	}

	private void updateLayerList(ArrayList<Layer> layerList) {
		if (layerList == null || layerList.size() == 0) {
			return;
		}
		// 遍历整个layer，判断是否已下载或有更新
		if (AppApplication.myDataBase == null) {
			AppApplication.myDataBase = new MyDatabase(mContext);
		}
		for (int i = 0; i < layerList.size(); i++) {
			Layer layer = layerList.get(i);
			Layer localLayer = AppApplication.myDataBase
					.getLocalLayerById(layer.id);
			if (localLayer == null) {
				layer.download_status = Layer.NOT_DOWNLOAD;
				continue;
			}
			layer.download_status = Layer.DOWNLOADED;
			if (localLayer.offline_time < layer.offline_time) {
				layer.hasUpdate = true;
			}
			layerList.set(i, layer);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.layer_analysis_menu, menu);
		return true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// AVAnalytics.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// AVAnalytics.onResume(this);
	}

	@Override
	public void onBackPressed() {

		if (mLayerListAdapter.btn_task_map != null
				&& !mLayerListAdapter.btn_task_map.isEmpty()) {
			Toast.makeText(this, "当前有离线包正在下载，请勿离开此页面", Toast.LENGTH_SHORT)
					.show();

			return;// 任务完成前不允许退出
		}

		super.onBackPressed();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
