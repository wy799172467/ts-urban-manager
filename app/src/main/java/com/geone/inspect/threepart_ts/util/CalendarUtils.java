package com.geone.inspect.threepart_ts.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class CalendarUtils {
	/**
	 * 获取当前日期时间
	 * 
	 * @param format
	 *            示例：yyyy-MM-dd HH:mm:ss
	 */
	public static String getCurrentTime(String format) {
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	public static Date parseStringToDate(String dateString) {
		Date convertedDate = null;
		SimpleDateFormat dateFormat = null;

		try {
			dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
			convertedDate = dateFormat.parse(dateString);
			return convertedDate;

		} catch (Exception ex) {			
			try {
				dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				convertedDate = dateFormat.parse(dateString);
				return convertedDate;
			} catch (Exception ex1) {				
				return null;
			}

		}

	}

	/**
	 * 格式化日期时间
	 * 
	 * @param format
	 *            示例：yyyy-MM-dd HH:mm:ss
	 */
	public static String formatDate(Date date, String format) {
		String dateString = "";
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(date);
		}
		return dateString;
	}
	/* private---- */
}
