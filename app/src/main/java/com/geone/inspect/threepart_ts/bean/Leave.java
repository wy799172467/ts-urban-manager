package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;

public class Leave implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4472198641044321388L;
	// 请假记录------
	public String ID;
	/** 开始日期时间 */
	public String StartDate;
	/** 结束日期时间 */
	public String EndDate;
	/** 类型：年假、事假…… */
	public String LeaveType;
	/** 请假原因 */
	public String Reason;
	/** 审批情况：未审批、未通过、已通过 */
	public String IsPass;
}
