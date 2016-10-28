package com.geone.inspect.threepart_ts.adapter;

import java.util.List;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.Leave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LeaveAdapter extends BaseAdapter {
	private Context mContext;
	private List<Leave> mData;

	public LeaveAdapter(Context context, List<Leave> data) {
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
			holder.txtLeaveDate = (TextView) convertView.findViewById(R.id.txtLeaveDate);
			holder.txtleaveType = (TextView) convertView
					.findViewById(R.id.txtleaveType);
			holder.txtLeaveReason=(TextView) convertView.findViewById(R.id.txtLeaveReason);
			holder.txtIsPass=(TextView) convertView.findViewById(R.id.txtIsPass);			
			convertView.setTag(holder);
		}
		holder = (ViewHolder) convertView.getTag();
		holder.txtLeaveDate.setText("\u3000"+mData.get(position).StartDate+" ～ "+mData.get(position).EndDate);
		holder.txtleaveType.setText(mData.get(position).LeaveType);
		holder.txtLeaveReason.setText("\u3000"+mData.get(position).Reason);//设置首行缩进
		holder.txtIsPass.setText(mData.get(position).IsPass);		
		return convertView;

	}

	static class ViewHolder {
		public TextView txtLeaveDate;
		public TextView txtleaveType;
		public TextView txtLeaveReason;
		public TextView txtIsPass;

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
