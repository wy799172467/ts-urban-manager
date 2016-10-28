package com.geone.inspect.threepart_ts.adapter;

import java.util.ArrayList;
import java.util.List;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.Case;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HSHCCLCaseListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	public List<Case> eventList;
	private int colorLYellow, colorLBlue, colorGreen,colorGray;

	public HSHCCLCaseListAdapter(LayoutInflater inflater) {
		mInflater = inflater;
		eventList = new ArrayList<Case>();
		colorLYellow = inflater.getContext().getResources()
				.getColor(R.color.fontLYellow);
		colorLBlue = inflater.getContext().getResources()
				.getColor(R.color.fontLBlue);
		colorGreen = inflater.getContext().getResources()
				.getColor(R.color.fontGreen);
		colorGray=inflater.getContext().getResources()
				.getColor(R.color.fontGray);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return eventList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return eventList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_hc, null);

			holder.layout_main = (LinearLayout) convertView
					.findViewById(R.id.layout_main);
			holder.tv_category_1 = (TextView) convertView
					.findViewById(R.id.tv_category_1);
			holder.tv_category_2 = (TextView) convertView
					.findViewById(R.id.tv_category_2);
			holder.tv_category_3 = (TextView) convertView
					.findViewById(R.id.tv_category_3);
			holder.tv_no = (TextView) convertView.findViewById(R.id.tv_no);
			holder.tv_status = (TextView) convertView
					.findViewById(R.id.tv_status);
			holder.tv_status.setVisibility(View.GONE);// 隐藏
			holder.tv_detail = (TextView) convertView
					.findViewById(R.id.tv_detail);
			holder.tv_level = (TextView) convertView
					.findViewById(R.id.tv_level);
			holder.tv_read = (TextView) convertView.findViewById(R.id.tv_read);
			holder.tv_divider = (TextView) convertView
					.findViewById(R.id.tv_divider);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Case event = eventList.get(position);
		holder.tv_category_1.setText(event.CaseClassIDesc);
		holder.tv_category_2.setText(event.CaseClassIIDesc);
		if (event.CaseClassIIIDesc != null) {
			holder.tv_category_3.setVisibility(View.VISIBLE);
			holder.tv_category_3.setText(event.CaseClassIIIDesc);
		}

		holder.tv_no.setText(event.ProblemNo);
		holder.tv_detail.setText(event.ReportCaseDesc);
		holder.tv_level.setText(event.CurrentStageStart);
		holder.tv_status.setText("待" + event.ProcessStageDesc);
		if (event.status != null && event.status.equals("false")) {
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_red);
			holder.tv_status.setText(event.ProcessStageDesc + "失败");
		}
		holder.tv_read.setVisibility(View.VISIBLE);

		if ("1".equalsIgnoreCase(event.SupervisionStatus)) {
			holder.tv_no.setTextColor(colorGreen);//督办案件改变字体颜色
		} else {
			holder.tv_no.setTextColor(colorGray);
		}
		int isRead = Integer.parseInt(event.IsRead);
		switch (isRead) {
		case 0:// 未读
			holder.tv_read.setText(Case.APPROVE_STATUS[0]);
			holder.tv_read.setTextColor(colorLBlue);
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_blue);
			break;
		case 1:// 已读
			holder.tv_read.setText(Case.APPROVE_STATUS[1]);
			holder.tv_read.setTextColor(colorLYellow);
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_yellow);
			break;
		case 2:// 延时审请中
			holder.tv_read.setText(Case.APPROVE_STATUS[2]);
			holder.tv_read.setTextColor(colorLYellow);
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_yellow);
			break;
		case 3:// 延时已通过
			holder.tv_read.setText(Case.APPROVE_STATUS[3]);
			holder.tv_read.setTextColor(colorLYellow);
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_yellow);
			break;
		case 4:// 延时未通过
			holder.tv_read.setText(Case.APPROVE_STATUS[4]);
			holder.tv_read.setTextColor(colorLYellow);
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_yellow);
			break;
		case 5:// 拒签未通过
			holder.tv_read.setText(Case.APPROVE_STATUS[5]);
			holder.tv_read.setTextColor(colorLYellow);
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_yellow);
			break;
		case 11:// 缓办已通过
			holder.tv_read.setText(Case.APPROVE_STATUS[6]);
			holder.tv_read.setTextColor(colorLYellow);
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_yellow);
			break;
		case 12:// 缓办未通过
			holder.tv_read.setText(Case.APPROVE_STATUS[7]);
			holder.tv_read.setTextColor(colorLYellow);
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_yellow);
			break;
		case 13:// 缓办申请中
			holder.tv_read.setText(Case.APPROVE_STATUS[8]);
			holder.tv_read.setTextColor(colorLYellow);
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_yellow);
			break;

		}

		return convertView;
	}

	// public void initData(ArrayOfSolarEvent eventArray, boolean isRefresh) {
	// if (isRefresh) {
	// for (int i = eventArray.size() - 1; i >= 0; i--) {
	// SolarEvent event = (SolarEvent) eventArray.get(i);
	// eventList.add(0, event);
	// }
	// } else {
	// eventList.addAll(eventArray);
	// }
	// this.count += eventArray.size();
	// }

	class ViewHolder {
		public LinearLayout layout_main;
		public TextView tv_category_1;
		public TextView tv_category_2;
		public TextView tv_category_3;
		public TextView tv_no;
		public TextView tv_status;
		public TextView tv_detail;
		public TextView tv_level;
		public TextView tv_read;
		public TextView tv_divider;

	}
}
