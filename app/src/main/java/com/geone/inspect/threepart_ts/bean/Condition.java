package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;

/** 立案条件 */
public class Condition implements Serializable, Comparable<Condition> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8681973189202992171L;
	/**
	 * 
	 */
	/** CONDITIONS_DEF = {"请选择立案条件"} */
	public static final String[] CONDITIONS_DEF = {"请选择立案条件" };
	public String code;// 数字
	public String cname;// code name
	public int xorder;// 用于排序

	public Condition(String code, String cname, int xorder) {
		this.code = code;
		this.cname = cname;
		this.xorder = xorder;
	}

	public Condition() {

	}

	// 用于Spinner显示内容
	@Override
	public String toString() {
		if (cname == null) {
			return "" + code;
		}
		return cname;
	}

	@Override
	public int compareTo(Condition another) {
		// TODO Auto-generated method stub
		return xorder - another.xorder;
	}

	// @Override
	// public int compareTo(CaseCondition another) {
	//
	// return xorder - another.xorder;
	// }

}
