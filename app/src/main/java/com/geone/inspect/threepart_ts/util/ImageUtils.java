package com.geone.inspect.threepart_ts.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.geone.inspect.threepart_ts.http.BitmapWorkerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

public class ImageUtils {
	/** 图片尺寸WITH_HEIGHTS = { { 500, 700} } */
	public static final int[][] WITH_HEIGHTS = { { 500, 700 } };// 建议600*800
	/** 缩略图尺寸THUMB_WITH_HEIGHTS = { { 171, 171 } } */
	public static final int[][] THUMB_WITH_HEIGHTS = { { 171, 171 } };

	/**
	 * 获取图片的缩略图
	 * 
	 * @param path
	 *            图片全路径，含扩展名
	 * @param outWith
	 *            输出宽度px
	 * @param outHeight
	 *            输出高度px
	 * @param
	 * @return
	 */
	public static Bitmap getBitmapThumbnail(String path, int outWith,
			int outHeight) {
		Bitmap bm = null;
		// 重新读入图片，读取缩放后的bitmap
		bm = getScaledBitmap(path, outWith, outHeight);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bm = ThumbnailUtils.extractThumbnail(bm, outWith, outHeight,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bm;
	}

	/**
	 * 获取图片的缩略图
	 * 
	 * @param bitmap
	 *            Bitmap对象
	 * @param outWith
	 *            输出宽度px
	 * @param outHeight
	 *            输出高度px
	 * @return
	 */
	public static Bitmap getBitmapThumbnail(Bitmap bitmap, int outWith,
			int outHeight) {
		return ThumbnailUtils.extractThumbnail(bitmap, outWith, outHeight,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

	}

	/**
	 * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 * 
	 * @param videoPath
	 *            视频的路径
	 * @param width
	 *            指定输出视频缩略图的宽度
	 * @param height
	 *            指定输出视频缩略图的高度度
	 * @param kind
	 *            参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	private Bitmap getVideoThumbnail(String videoPath, int width, int height,
			int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		System.out.println("w" + bitmap.getWidth());
		System.out.println("h" + bitmap.getHeight());
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 压缩Bitmap并转换为byte[],大小不超过maxKB
	 * 
	 * @param path
	 *            图片路径
	 * @param outWith
	 *            图片目标宽度
	 * @param outHeight
	 *            图片目标高度
	 * @param maxKB
	 *            输出图片的最大KB，如200,则压缩后图片不超过200k(待验证)
	 * @return
	 */
	public static byte[] getCompressedBitmapBytes(String path, int outWith,
			int outHeight, long maxKB) {
		Bitmap bm = null;
		bm = getScaledBitmap(path, outWith, outHeight);
		return compressBitmapToBytes(bm, maxKB);

	}

	/**
	 * 压缩Bitmap并转换为byte[],大小不超过maxKB
	 * 
	 * @param path
	 *            图片路径
	 * @param outWith
	 *            图片目标宽度
	 * @param outHeight
	 *            图片目标高度
	 * @param maxKB
	 *            输出图片的最大KB，如200,则压缩后图片不超过200k(待验证)
	 * @return
	 */
	public static byte[] getCompressedPrintBitmapBytes(String path,
			int outWith, int outHeight, long maxKB) {
		byte[] bytes = null;
		Bitmap bm = null;
		bm = getScaledBitmap(path, outWith, outHeight);
		bm = printWord(bm, ImageUtils.getDateTime(path), null);
		bytes = compressBitmapToBytes(bm, maxKB);
		if (bm != null) {
			bm.recycle();
		}
		return bytes;

	}

	/**
	 * 获取压缩后的图像
	 * 
	 * @param path
	 *            图片路径
	 * @param outWith
	 *            图片目标宽度
	 * @param outHeight
	 *            图片目标高度
	 * @param maxKB
	 *            输出图片的最大KB，如200,则压缩后图片不超过200k
	 * @return
	 */
	public static Bitmap getCompressedBitmap(String path, int outWith,
			int outHeight, long maxKB) {
		Bitmap bm = null;
		bm = getScaledBitmap(path, outWith, outHeight);
		bm = compressBitmap(bm, maxKB);
		return bm;
	}

	/**
	 * 
	 * 获取缩放的图像-按照固定比例进行缩放
	 *
	 * @param path
	 *            图片路径
	 * @param outWith
	 *            图片目标宽度
	 * @param outHeight
	 *            图片目标高度
	 * @return
	 */
	public static Bitmap getScaledBitmap(String path, int outWith, int outHeight) {
		Bitmap bm = null;
		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, bmOptions);
		bmOptions.inSampleSize = caculateInSampleSizeByShort(bmOptions,
				outWith, outHeight);
		// bmOptions.inSampleSize = caculateInSampleSize(bmOptions, outWith,
		// outHeight);
		bmOptions.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeFile(path, bmOptions);
		bm = getAdjustBitmap(path, bm);
		return bm;
	}

	/**
	 * 
	 * 获取缩放的图像-按照短边尺寸进行缩放 存在问题：此种方法创建两次bitmap内存消耗较大。测试40M以内都达标，以上图片未测试。
	 * 下步工作：1.优化算法，尽量少创建Bitmap；2.降低位深从32降低到24。
	 *
	 * @param path
	 *            图片路径
	 * @param outWith
	 *            图片目标宽度
	 * @param outHeight
	 *            图片目标高度
	 * @return
	 */
	public static Bitmap getScaledBitmap1(String path, int outWith,
			int outHeight) {
		Bitmap bm = null;
		bm = getScaledBitmapBySampleSize(path, outWith, outHeight);
		// 缩放图片的尺寸
		int bmpWidth = bm.getWidth();
		int bmpHeight = bm.getHeight();
		// 确定短边
		int inShortEdge = bmpWidth <= bmpHeight ? bmpWidth : bmpHeight;
		int outShortEdge = outWith <= outHeight ? outWith : outHeight;
		float scaleShort = (float) outShortEdge / inShortEdge; // 按固定大小缩放,以短边为准
		// float scaleHeight = (float) outHeight / bmpHeight;
		Matrix matrix = new Matrix();// 缩放矩阵
		matrix.postScale(scaleShort, scaleShort);// 产生缩放后的Bitmap对象
		bm = Bitmap.createBitmap(bm, 0, 0, bmpWidth, bmpHeight, matrix, false);
		bm = getAdjustBitmap(path, bm);
		System.gc();
		return bm;
	}

	/**
	 * 在图片上印字
	 * 
	 * @param bitmap
	 *            源图片
	 * @param text
	 *            印上去的字
	 * @param param
	 *            字体参数分别为：颜色,大小,是否加粗,起点x,起点y; 比如：{color : 0xFF000000, size : 30,
	 *            bold : true, x : 20, y : 20}
	 * @return Bitmap
	 */
	public static Bitmap printWord(Bitmap bitmap, String text,
			Map<String, Object> param) {
		if (TextUtils.isEmpty(text)) {
			return bitmap;
		}
		if (param == null) {
			param = new HashMap<String, Object>();
		}
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		Paint paint = new Paint();
		paint.setColor(null != param.get("color") ? (Integer) param
				.get("color") : Color.RED);
		paint.setTextSize(null != param.get("size") ? (Integer) param
				.get("size") : 40);
		paint.setFakeBoldText(null != param.get("bold") ? (Boolean) param
				.get("bold") : true);// 默认加粗
		canvas.drawText(text, null != param.get("x") ? (Integer) param.get("x")
				: 50, null != param.get("y") ? (Integer) param.get("y") : 50,
				paint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return newBitmap;
	}

	/**
	 * 根据设置的宽高来计算压缩比例
	 * 
	 * @param
	 * @return
	 */
	public static int caculateInSampleSize(BitmapFactory.Options options,
			int outWith, int outHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (outWith == 0 || outHeight == 0)
			return 1;
		if (height > outHeight || width > outWith) {
			final int heightRatio = Math.round((float) height
					/ (float) outHeight);
			final int widthRatio = Math.round((float) width / (float) outWith);
			// inSampleSize = heightRatio < widthRatio ? heightRatio :
			// widthRatio;
			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}
		return inSampleSize;
	}

	/**
	 * 
	 * 基于质量的压缩算法， 此方法 解决压缩后图像失真问题。可先调用比例压缩适当压缩图片后，再调用此方法可解决上述问题
	 *
	 * @param bts
	 * @param maxKB压缩后的图像最大大小单位为KB
	 *            ，非绝对准确可能会超一点
	 * @return
	 */
	public static Bitmap compressBitmap(Bitmap bitmap, long maxKB) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int quality = 100;// 压缩比例
			bitmap.compress(CompressFormat.JPEG, quality, baos);// 此处若设置为PNG则无效。
			LogUtils.d("ImageUtils", "压缩前：" + baos.toByteArray().length / 1024);
			while (baos.toByteArray().length > (maxKB * 1024) && quality > 0) {
				baos.reset();
				bitmap.compress(CompressFormat.JPEG, quality, baos);
				quality -= 8;// 依次降低8%
			}
			LogUtils.d("ImageUtils", "压缩后：" + baos.toByteArray().length / 1024);
			byte[] bts = baos.toByteArray();
			Bitmap bmp = BitmapFactory.decodeByteArray(bts, 0, bts.length);// ？为什么这里会增加图像的大小
			baos.close();
			return bmp;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * 压缩Bitmap并转换为byte[],大小不超过maxKB
	 *
	 * @param bitmap
	 * @param maxKB压缩后的图像最大大小单位为KB
	 *            ，非绝对准确可能会超一点
	 * @return
	 */
	public static byte[] compressBitmapToBytes(Bitmap bitmap, long maxKB) {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int quality = 70;// 压缩比例
			bitmap.compress(CompressFormat.JPEG, quality, baos);// 此处若设置为PNG则无效。
			LogUtils.d("ImageUtils", "压缩前：" + baos.toByteArray().length / 1024);
			while (baos.toByteArray().length > (maxKB * 1024) && quality > 0) {
				baos.reset();
				bitmap.compress(CompressFormat.JPEG, quality, baos);
				quality -= 5;// 依次降低5%
			}
			// bitmap.compress(CompressFormat.JPEG, 75, baos);
			LogUtils.d("ImageUtils", "压缩后：" + baos.toByteArray().length / 1024);
			byte[] bytes = baos.toByteArray();
			baos.close();
			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Bitmap bytesToBitmap(byte[] bytes) {
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}

	public static byte[] bitmapToBytes(Bitmap bitmap, int quality) {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.JPEG, quality, baos);// 此处若设置为PNG则无效。
			byte[] bytes = baos.toByteArray();
			baos.close();
			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/** 将Bitmap保存到本地 */
	public static boolean saveBitmapToExternalStorage(Context context,
			Bitmap image, String filePath, String fileName) {
		try {
			File dir = new File(filePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			OutputStream fOut = null;
			File file = new File(filePath, fileName + ".jpg");
			file.createNewFile();
			fOut = new FileOutputStream(file);
			// 100 means no compression, the lower you go, the stronger the
			// compression
			image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();
			MediaStore.Images.Media.insertImage(context.getContentResolver(),
					file.getAbsolutePath(), file.getName(), file.getName());
			return true;

		} catch (Exception e) {
			LogUtils.d("ImageUtils", "保存图片失败：" + e.getMessage());
			return false;
		}
	}

	/**
	 * 获取图片的拍摄时间,优先从Exif中取，不可得时用文件的最后修改时间代替
	 * 
	 * @param filePath
	 *            图片全路径
	 */
	public static String getDateTime(String filePath) {
		String time = "";
		ExifInterface exifInterface = getImageExif(filePath);
		if (exifInterface != null) {
			time = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);// 2015:07:01
																			// 19:40:20
			Date date = CalendarUtils.parseStringToDate(time);
			if (date != null) {
				time = CalendarUtils.formatDate(date, "yyyy-MM-dd HH:mm:ss");// 2015-07-01
																				// 19:40:20
			} else {
				time = FileUtils.getFileLastModifiedTime(filePath,
						"yyyy-MM-dd HH:mm:ss");
			}
		}
		return time;
	}

	/**
	 * 获取经过旋转校正的bitmap，如三星手机对图片的旋转
	 * 
	 * @param path
	 *            图片路径
	 * @param bm
	 *            待处理的bitmap
	 * @return 经过旋转校正的bitmap
	 */
	private static Bitmap getAdjustBitmap(String path, Bitmap bm) {
		int rotate_degree = getBitmapDegree(path);
		if (rotate_degree != 0) {
			bm = rotateBitmapByDegree(bm, rotate_degree);
		}
		System.gc();
		return bm;
	}

	/** 异步加载图片 */
	public static void loadBitmap(String file_path, ImageView imageView) {
		BitmapWorkerTask task = new BitmapWorkerTask(imageView);
		task.execute(file_path, THUMB_WITH_HEIGHTS[0][0],
				THUMB_WITH_HEIGHTS[0][1]);
	}

	// private--------------------------

	/**
	 * 
	 * 获取缩放的图像-按照固定比例进行缩放
	 *
	 * @param path
	 *            图片路径
	 * @param outWith
	 *            图片目标宽度
	 * @param outHeight
	 *            图片目标高度
	 * @return
	 */
	private static Bitmap getScaledBitmapBySampleSize(String path, int outWith,
			int outHeight) {
		Bitmap bm = null;
		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, bmOptions);
		bmOptions.inSampleSize = caculateInSampleSizeByShort(bmOptions,
				outWith, outHeight);
		bmOptions.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeFile(path, bmOptions);// 直接创建Bitmap遇到大图片容易OOM
		return bm;
	}

	/**
	 * 根据设置的宽高来计算缩放比例，以短边为准
	 * 
	 * @param
	 * @return
	 */
	private static int caculateInSampleSizeByShort(
			BitmapFactory.Options options, int outWith, int outHeight) {
		int inSampleSize = 1;
		if (options == null || outWith < 1 || outHeight < 1) {
			return inSampleSize;
		}
		final int height = options.outHeight;
		final int width = options.outWidth;
		if (outWith == 0 || outHeight == 0)
			return 1;
		// 确定短边
		int inShortEdge = width <= height ? width : height;
		int outShortEdge = outWith <= outHeight ? outWith : outHeight;
		// 根据短边计算比例
		// inSampleSize = Math.round(inShortEdge / (float) outShortEdge);
		inSampleSize = (int) Math.ceil(inShortEdge / (double) outShortEdge);// 向上取整

		// if (height > outHeight || width > outWith) {
		//
		// final int heightRatio = Math.round((float) height
		// / (float) outHeight);
		// final int widthRatio = Math.round((float) width / (float) outWith);
		// // inSampleSize = heightRatio < widthRatio ? heightRatio :
		// // widthRatio;
		// inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		// }
		return inSampleSize;
	}

	/** 获取图片的Exif信息 */
	private static ExifInterface getImageExif(String filePath) {
		ExifInterface exifInterface = null;
		try {
			exifInterface = new ExifInterface(filePath);
			return exifInterface;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 读取图片的旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return 图片的旋转角度
	 */
	private static int getBitmapDegree(String path) {
		int degree = 0;
		try {
			// 从指定路径下读取图片，并获取其EXIF信息
			ExifInterface exifInterface = new ExifInterface(path);
			// 获取图片的旋转信息
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
			return degree;
		} catch (IOException e) {
			e.printStackTrace();
			return degree;
		}
	}

	/** 旋转图片一定角度 */
	private static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
		Bitmap returnBm = null;
		// 根据旋转角度，生成旋转矩阵
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		try {
			// 将原始图片按照旋转矩阵进行旋转，并得到新的图片
			returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
					bm.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
		}
		if (returnBm == null) {
			returnBm = bm;
		}
		if (bm != returnBm) {
			// bm.recycle();
		}
		return returnBm;
	}

}
