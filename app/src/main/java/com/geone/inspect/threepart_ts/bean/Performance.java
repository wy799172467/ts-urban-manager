package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;

public class Performance implements Serializable,Comparable<Performance> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2298577948435485376L;
	/**综合考评标题*/
	public String key;
	/**综合考评内容*/
	public String value;
	/**用于排序的行号*/
	public int xorder;
	@Override
	public int compareTo(Performance another) {
		
		return (xorder - another.xorder);
	}

}
