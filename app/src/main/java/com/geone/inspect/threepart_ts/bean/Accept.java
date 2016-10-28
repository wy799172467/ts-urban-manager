package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;

/** 案件受理信息 */
public class Accept implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4496179222632664314L;
	/** 案件编码 */
	public String ProblemNo;
	/** 是否受理,0未受理，1受理 ，默认1*/
	public String IsAccept;

}
