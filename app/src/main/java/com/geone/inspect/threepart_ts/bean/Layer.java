package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;

public class Layer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2171083200060939005L;
	public static final int NOT_DOWNLOAD = 0;
	public static final int DOWNLOADING = -1;
	public static final int DOWNLOADED = 1;
	// 在线图层{------
	public String id;
	public String url;
	public String subIds; // dynamic图层下可见图层;
	// 在线图层}------
	// 离线地图{------
	// id、url共用
	public String name;
	public String offline_name;
	public String offline_url;
	public long offline_time; // 更新时间
	public double offline_size;
	public int download_status; // 下载状态
	public boolean hasUpdate; // 是否有更新
	// 离线地图}------
}
