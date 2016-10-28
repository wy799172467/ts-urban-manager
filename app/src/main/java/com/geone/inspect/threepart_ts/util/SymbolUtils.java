package com.geone.inspect.threepart_ts.util;

import android.content.Context;

import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.geone.inspect.threepart_ts.activity.AppApplication;

public class SymbolUtils {
	private static Context mContext = AppApplication.getApplication();

	/**
	 * 根据资源id构建PictureMarkerSymbol
	 * 
	 * @param resId
	 *            图片资源id
	 * @return PictureMarkerSymbol
	 */
	public static PictureMarkerSymbol getPictureMarkerSymbol(int resId) {
		return new PictureMarkerSymbol(mContext, mContext.getResources()
				.getDrawable(resId));

	}

	/**
	 * 构建SimpleFillSymbol
	 * 
	 * @param fillColor
	 *            背景填充色
	 * @param style
	 *            背景填充样式，如对角线
	 * @param outLineColor
	 *            边线颜色
	 * @param width
	 *            边线宽度
	 * 
	 * 
	 * @return SimpleFillSymbol
	 */
	public static SimpleFillSymbol getSimpleFillSymbol(int fillColor,
			SimpleFillSymbol.STYLE style, int outLineColor, float width) {
		SimpleFillSymbol sfSymbol = new SimpleFillSymbol(fillColor, style);
		sfSymbol.setOutline(new SimpleLineSymbol(outLineColor, width));
		return sfSymbol;
	}

	/**
	 * 构建SimpleFillSymbol
	 * 
	 * @param fillColor
	 *            背景填充色
	 * @param style
	 *            背景填充样式，如对角线
	 * @param slSymbol
	 *            边线符号
	 * 
	 * @return SimpleFillSymbol
	 */
	public static SimpleFillSymbol getSimpleFillSymbol(int fillColor,
			SimpleFillSymbol.STYLE style, SimpleLineSymbol slSymbol) {
		SimpleFillSymbol sfSymbol = new SimpleFillSymbol(fillColor, style);
		sfSymbol.setOutline(slSymbol);
		return sfSymbol;
	}

	/**
	 * Instantiates a SimpleLineSymbol with the given color and width.
	 * 
	 * @param color
	 *            边线颜色
	 * @param width
	 *            边线宽度
	 * 
	 * 
	 * @return SimpleLineSymbol
	 */
	public static SimpleLineSymbol getSimpleLineSymbol(int color, float width) {
		SimpleLineSymbol slSymbol = new SimpleLineSymbol(color, width);
		return slSymbol;
	}

}
