package com.geone.inspect.threepart_ts.adapter;

import java.util.ArrayList;

import com.geone.inspect.threepart_ts.bean.IBean;
import com.geone.inspect.threepart_ts.fragment.IBeanFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class IPagerAdapter extends FragmentStatePagerAdapter {

	private ArrayList<IBeanFragment> fragmentList;

	public ArrayList<IBean> contentList;

	public IPagerAdapter(Context context, FragmentManager fm,
			ArrayList<IBean> contentList) {
		super(fm);
		this.contentList = contentList;
		initFragments();
	}

	private void initFragments() {

		fragmentList = new ArrayList<IBeanFragment>(contentList.size());
		for (int i = 0; i < contentList.size(); i++) {
			IBeanFragment fragment = new IBeanFragment();
			IBean mIBean = contentList.get(i);
			fragment.showlistMap = mIBean.showlistMap;		
			fragmentList.add(fragment);
		}

	}

	@Override
	public Fragment getItem(int i) {
		return fragmentList.get(i);

	}

	// @Override
	// public int getItemPosition(Object object) {
	// // TODO Auto-generated method stub
	// Log.d("IPagerAdapter", "" + object);
	//
	// return POSITION_NONE;
	// }

	@Override
	public int getCount() {
		return contentList == null ? 0 : contentList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return "1";
	}

}
