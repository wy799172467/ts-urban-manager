package com.geone.inspect.threepart_ts.adapter;

import java.util.List;


import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.Performance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PerformanceAdapter extends BaseAdapter {
	private Context mContext;
	private List<Performance> mData;

	public PerformanceAdapter(Context context, List<Performance> data) {
		this.mData = data;
		this.mContext = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int position) {

		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.performance_item, null);
			holder.txtKey = (TextView) convertView
					.findViewById(R.id.tv_key);
			holder.txtValue = (TextView) convertView
					.findViewById(R.id.tv_value);
			convertView.setTag(holder);
		}
		holder = (ViewHolder) convertView.getTag();

		holder.txtKey.setText(mData.get(position).key);
		holder.txtValue.setText(mData.get(position).value);
		return convertView;

	}

	static class ViewHolder {
		public TextView txtKey;
		public TextView txtValue;

	}

//	// adapter中的所有item不可点击
//	@Override
//	public boolean areAllItemsEnabled() {
//		return false;
//	}

	// 下标为position 的item不可选中，不可点击。
	@Override
	public boolean isEnabled(int position) {
		return false;
	}

}
