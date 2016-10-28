package com.geone.inspect.threepart_ts.fragment;

import java.util.HashMap;
import java.util.Iterator;

import com.geone.inspect.threepart_ts.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * 根据服务器返回的IBean构建屏幕下方的info窗口
 */
public class IBeanFragment extends Fragment {

	public String iLayerType;
//	public HashMap<String, String> iBeanMap;
	private LinearLayout baseLayout;
	public HashMap<String, String> showlistMap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = initViews(inflater, container);

		return rootView;
	}



	private View initViews(LayoutInflater inflater, ViewGroup container) {
		View rootView = inflater.inflate(R.layout.fragment_i, container, false);

		

		baseLayout = (LinearLayout) rootView.findViewById(R.id.baseLayout);

		Iterator<String> iter = showlistMap.keySet().iterator();
		int i = 0;

		while (iter.hasNext()) {
			String key = iter.next();
			String value = showlistMap.get(key);
			// if (key.equals("Photo") || key.equals("objectid")
			// || key.equals("TableName") || key.equals("BJXL")
			// || key.equals("CHRY")) {
			// continue;
			// }
			final View v = inflater.inflate(R.layout.i_item, null);
			final TextView tv_dkh = (TextView) v.findViewById(R.id.tv_key);
			tv_dkh.setText(key + ": ");
			final TextView tv_zxdw = (TextView) v.findViewById(R.id.tv_value);
			tv_zxdw.setText(value);
			// if (i == 0) {
			// final TextView tv_flag = (TextView) v
			// .findViewById(R.id.tv_flag);
			// tv_flag.setText(iLayerType);
			// tv_flag.setVisibility(View.VISIBLE);
			// }
			baseLayout.addView(v);
			i++;
		}

		return rootView;
	}
}
