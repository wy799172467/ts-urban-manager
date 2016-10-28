package com.geone.inspect.threepart_ts.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.geone.inspect.threepart_ts.bean.Layer;

public class ModelUtils {
	/** 离线图层Layer */
	public static Layer parseJSONToLayer(JSONObject obj) {
		Layer layer = new Layer();
		try {
			if (obj.has("error")) {
				return null;
			}
			layer.id = obj.getString("id");
			layer.name = obj.getString("name");
			layer.offline_name = obj.getString("filename");
			layer.offline_size = Double.parseDouble(obj.getString("filesize"));
			layer.offline_time = Long.parseLong(obj.getString("updatetime"));
			layer.offline_url = obj.getString("url");
			return layer;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	// private----------

}
