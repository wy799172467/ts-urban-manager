package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;

public class Category implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6217459772575239980L;
	/**CATEGORY_TAGS={"large","small","sub"}*/
	public static final String[] TAG_CATEGORYS={"large","small","sub"};
	public String value;// 数字

	public String description;// value对应中文
	/** 主题图层对应服务地址 */
	public String url = "";

	public Category() {

	}

	public Category(String value, String description) {
		this.value = value;
		this.description = description;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if (value == null) {
			return description;
		}
		return description;
	}
}
