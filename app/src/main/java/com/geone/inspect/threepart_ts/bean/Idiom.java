package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;

/** 常用语 */
public class Idiom implements Serializable, Comparable<Idiom> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5389587840455322774L;

	public String cyy;
	public String xorder;

	@Override
	public int compareTo(Idiom another) {
		if (xorder == null || another.xorder == null) {
			return 0;
		}
		return xorder.compareToIgnoreCase(another.xorder);
	}

	@Override
	public String toString() {
		if (cyy == null) {
			return "" + cyy;
		}
		return cyy;
	}

}
