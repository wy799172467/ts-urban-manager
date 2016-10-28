package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;

public class CaseRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1796437253814144067L;

	public int _id;
	public String caseID;
	public String path;
	// liyl 2015-4-14
	public String wftype;
	// 未启用字段---
	public String processStage;
	public String processResult;
	// ---未启用字段

}
