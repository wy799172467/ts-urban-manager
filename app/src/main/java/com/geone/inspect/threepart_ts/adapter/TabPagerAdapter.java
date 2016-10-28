package com.geone.inspect.threepart_ts.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.geone.inspect.threepart_ts.activity.AppApplication;
import com.geone.inspect.threepart_ts.bean.User;
import com.geone.inspect.threepart_ts.fragment.CaseReportFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {

	// private static final int[] TITLE = { R.string.case_report,
	// R.string.case_verify, R.string.case_inspect};
	private String[] pageTitles;
	/** 巡查单位tab{ "问题上报", "待核实", "待核查" } */
	public static final String[] PAGETITLES_XC = { "问题上报", "待核实", "待核查" };
//	public static final String[] PAGETITLES_XC = { "问题上报" };
	/** 处置单位tab{ "问题上报", "待处理", "我签收" } */
	public static final String[] PAGETITLES_MANAGE = { "问题上报", "待处理", "我收录" };

	private Context mContext;

	public TabPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
		if (User.ROLENAMES[0]
				.equalsIgnoreCase(AppApplication.mUser.rolename)) {// 巡查单位：问题上报、核实、核查
			pageTitles = PAGETITLES_XC;
		} else {
			pageTitles = PAGETITLES_MANAGE;
			// if (User.ROLENAMES[2]
			// .equalsIgnoreCase(InspectorApplication.mUser.rolename)) {//
			// 养护单位:处理
			// pageTitles = new int[] { R.string.case_process };
			//
			// } else {// 养护单位以外的处置单位：问题上报、处理
			// pageTitles = new int[] { R.string.case_report,
			// R.string.case_process };
			// }

		}

	}

	@Override
	public Fragment getItem(int i) {

		Fragment fragment = new CaseReportFragment();
		Bundle args = new Bundle();
		args.putInt("tab_index", i);
		args.putString("pageTitle", (String) getPageTitle(i));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public int getCount() {
		return pageTitles.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return pageTitles[position];
	}
	// //移除所有view重新加载
	// @Override
	// public int getItemPosition(Object object) {
	// return POSITION_NONE;
	// }

}
