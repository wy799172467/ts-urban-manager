package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;

import com.esri.core.geometry.Point;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -731491014666596326L;
	/** ROLENAMES = { "xcDept", "manageDept", "yhDept" };yhDept属于manageDept */
	public static final String[] ROLENAMES = { "xcDept", "manageDept", "yhDept" };
	public String userID;

	public String name;

	public String account;

	public String password;

	public String gridID;

	// public boolean isOnline;

	public Point location_point;
	/** 配置信息版本 */
	public int config_version = -1;
	/** 上传时间间隔 */
	public long interval;
	/** 用户角色，xcDept：巡查单位 问题上报、核实、核查, managerDept：处置单位 问题上报、处理，yhDept:养护单位 */
	public String rolename;

}
