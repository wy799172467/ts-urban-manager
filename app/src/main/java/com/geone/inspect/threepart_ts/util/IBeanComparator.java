package com.geone.inspect.threepart_ts.util;

import java.util.Comparator;

import com.geone.inspect.threepart_ts.bean.IBean;

public class IBeanComparator implements Comparator<IBean> {

	@Override
	public int compare(IBean lhs, IBean rhs) {
		int a = Integer.parseInt(lhs.id);
		int b = Integer.parseInt(rhs.id);
		return a - b;
	}
}
