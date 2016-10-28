package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;

public class Push implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7992160760297339550L;
	// 推送消息------
	public String ID;
	/** 开始日期 */
	public String StartDate;
	/** 结束日期 */
	public String EndDate;
	/** 消息标题 */
	public String Title;
	/** 消息内容 */
	public String Content;
	/** 紧急程度 */
	public String Emergency;
}
