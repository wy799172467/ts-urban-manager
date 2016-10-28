package com.geone.inspect.threepart_ts.util;

import android.annotation.SuppressLint;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressLint("SimpleDateFormat")
public class FileUtils {

	/**
	 * 获取文件的最后修改时间
	 * 
	 * @param filePath
	 *            文件路径
	 * @param format
	 *            示例：yyyy-MM-dd HH:mm:ss
	 */
	public static String getFileLastModifiedTime(String filePath, String format) {
		String modifiedTime = "";
		try {
			File f = new File(filePath);
			Calendar calendar = Calendar.getInstance();
			long time = f.lastModified();
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			calendar.setTimeInMillis(time);
			// 输出：修改时间 2015-06-30 10:32:38
			modifiedTime = sdf.format(calendar.getTime());
			return modifiedTime;
		} catch (Exception ex) {
			return modifiedTime;
		}
	}

}
