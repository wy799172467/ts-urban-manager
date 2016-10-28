package com.geone.inspect.threepart_ts.adapter;

import java.util.ArrayList;
import java.util.List;

import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.Case;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 问题上报专用 */
public class CaseListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	public int count = 0;
	public List<Case> eventList;

	public String timeZone;
	private int colorLYellow, colorLBlue;

	public CaseListAdapter(LayoutInflater inflater) {
		mInflater = inflater;
		eventList = new ArrayList<Case>();
		colorLYellow = inflater.getContext().getResources()
				.getColor(R.color.fontLYellow);
		colorLBlue = inflater.getContext().getResources()
				.getColor(R.color.fontLBlue);
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
			convertView = mInflater.inflate(R.layout.list_item_report, null);

			holder.layout_main = (LinearLayout) convertView
					.findViewById(R.id.layout_main);
			holder.tv_category_1 = (TextView) convertView
					.findViewById(R.id.tv_category_1);
			holder.tv_category_2 = (TextView) convertView
					.findViewById(R.id.tv_category_2);
			holder.tv_category_3 = (TextView) convertView
					.findViewById(R.id.tv_category_3);
			holder.tv_no = (TextView) convertView.findViewById(R.id.tv_no);
			holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
			holder.tv_detail = (TextView) convertView
					.findViewById(R.id.tv_detail);
			holder.tv_level = (TextView) convertView
					.findViewById(R.id.tv_level);
			holder.tv_status = (TextView) convertView
					.findViewById(R.id.tv_status);
			holder.ib_pic = (CheckBox) convertView.findViewById(R.id.ib_pic);
			holder.ib_location = (CheckBox) convertView
					.findViewById(R.id.ib_location);
			holder.ib_done = (CheckBox) convertView.findViewById(R.id.ib_ok);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Case mCase = eventList.get(position);
		holder.tv_category_1.setText(mCase.CaseClassIDesc);
		holder.tv_category_2.setText(mCase.CaseClassIIDesc);
		if (mCase.CaseClassIIIDesc != null) {
			holder.tv_category_3.setVisibility(View.VISIBLE);
			holder.tv_category_3.setText(mCase.CaseClassIIIDesc);
		}

		holder.tv_no.setText(mCase.ProblemNo);
		// holder.tv_no.setText("案件ID: " + mCase.ProblemNo);
		holder.tv_detail.setText(mCase.ReportCaseDesc);
		holder.tv_date.setVisibility(View.GONE);

		int reportCode = Integer.parseInt(mCase.ProcessResult);
		switch (reportCode) {
		case 0:// 已保存
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_yellow);

			holder.tv_status.setText(Case.REPORT_STATUS[0]);
			holder.tv_status.setTextColor(colorLYellow);
			holder.tv_level.setText(mCase.postTime);
			break;
		case 1:// 已上报
			holder.tv_level.setText(mCase.postTime);
			holder.layout_main
					.setBackgroundResource(R.drawable.listview_bg_blue);
			if ("0".equalsIgnoreCase(mCase.isAccept)) {
				// 不受理
				holder.tv_status.setTextColor(colorLYellow);
				holder.tv_status.setText(Case.REPORT_STATUS[2]);
			} else {
				// 已上报
				holder.tv_status.setTextColor(colorLBlue);
				holder.tv_status.setText(Case.REPORT_STATUS[1]);
			}

			break;
		// case 2:// 不受理
		//
		// break;
		// case 3:// 不立案
		// break;
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
		public TextView tv_date;
		public TextView tv_detail;
		public TextView tv_level;
		public TextView tv_status;
		public CheckBox ib_pic;
		public CheckBox ib_location;
		public CheckBox ib_done;

	}
}
