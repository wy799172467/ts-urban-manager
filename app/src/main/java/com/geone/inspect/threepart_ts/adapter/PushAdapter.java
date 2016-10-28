package com.geone.inspect.threepart_ts.adapter;

import java.util.List;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.Push;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PushAdapter extends BaseAdapter {
	private Context mContext;
	private List<Push> mData;

	public PushAdapter(Context context, List<Push> data) {
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
					R.layout.leave_item, null);
			holder.txt01 = (TextView) convertView
					.findViewById(R.id.txtLeaveTitle);
			holder.txt02 = (TextView) convertView
					.findViewById(R.id.txtLeaveReason);
			holder.txt03 = (TextView) convertView
					.findViewById(R.id.txtLeaveDate);
			holder.txt04 = (TextView) convertView
					.findViewById(R.id.txtleaveType);

			convertView.setTag(holder);
		}
		holder = (ViewHolder) convertView.getTag();
		holder.txt01.setText(mData.get(position).Title);
		holder.txt02.setText("\u3000" + mData.get(position).Content);// 设置首行缩进
		holder.txt03.setText("\u3000" + mData.get(position).StartDate + " ～ "
				+ mData.get(position).EndDate);
		holder.txt04.setText(mData.get(position).Emergency);

		return convertView;

	}

	static class ViewHolder {
		public TextView txt01;// 标题
		public TextView txt02;// 内容
		public TextView txt03;// 日期
		public TextView txt04;// 紧急程度

	}

	// // adapter中的所有item不可点击
	// @Override
	// public boolean areAllItemsEnabled() {
	// return false;
	// }

	// 下标为position 的item不可选中，不可点击。
	@Override
	public boolean isEnabled(int position) {
		return false;
	}

}
