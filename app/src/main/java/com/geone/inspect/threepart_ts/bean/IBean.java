package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONObject;

import com.esri.core.geometry.Geometry;

public class IBean implements Serializable, Comparable<IBean> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8894952203184993009L;

	public String id;

	public Geometry shape;
	/** 图层类型，用于区分城市部件和绿化部件 */
	public String iLayerType;
	/** 对应i查结果中的showlist */
	public HashMap<String, String> showlistMap;

	public String getAttrJSONObjectString() {
		JSONObject mObject = new JSONObject(showlistMap);
		return mObject.toString();
	}

	// 按名称排序
	@Override
	public int compareTo(IBean another) {
		if (id == null || another.id == null) {
			return 0;
		}
		return id.compareToIgnoreCase(another.id);
	}
}
