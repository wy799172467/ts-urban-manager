package com.geone.inspect.threepart_ts.activity;

import java.io.IOException;
import java.io.InputStream;

import com.example.touch.TouchImageView;
import com.geone.inspect.threepart_ts.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class PicViewActivity extends Activity implements OnClickListener {

	private static final String LOG_TAG = "PicViewActivity";

	private ProgressBar mProgressBar;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// setTitle(R.string.record_sound);
		setContentView(R.layout.pic_viewer);
		String image_path = getIntent().getStringExtra("image_path");
		String image_url = getIntent().getStringExtra("image_url");
		final RelativeLayout mLinearLayout = (RelativeLayout) findViewById(R.id.background_layout);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

		final TouchImageView imageView = (TouchImageView) findViewById(R.id.imageView1);
		imageView.setMaxZoom(4);
		mLinearLayout.setOnClickListener(this);
		imageView.setOnClickListener(this);
		if (image_path == null || image_path.isEmpty()) {
			new LoadPicAsyncTask().execute(imageView, image_url, true);// true:从url获取图片,false:从本地获取图片
		} else {
			new LoadPicAsyncTask().execute(imageView, image_path, false);
		}

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private Bitmap getBitmap(ImageView imageView, String file_path) {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = imageView.getWidth();
		int targetH = imageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file_path, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = 4;
		// bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(file_path, bmOptions);
		int rotate_degree = getBitmapDegree(file_path);
		if (rotate_degree != 0) {
			bitmap = rotateBitmapByDegree(bitmap, rotate_degree);
		}
		System.gc();
		return bitmap;
	}

	/**
	 * 读取图片的旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return 图片的旋转角度
	 */
	private int getBitmapDegree(String path) {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	private Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
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
			bm.recycle();
		}
		return returnBm;
	}

	// edit by liyl 2014-12-1----
	/** 通过url获取网络图片 */
	private Bitmap getBitmapFromUrl(ImageView imageView, String file_url) {
		String url = file_url;
		Bitmap tmpBitmap = null;
		try {
			InputStream iStream = new java.net.URL(url).openStream();
			tmpBitmap = BitmapFactory.decodeStream(iStream);
			iStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("下载图片失败-", e.getMessage());
		}
		return tmpBitmap;

	}

	@Override
	public void onClick(View arg0) {
		finish();

	}

	private void showProgress() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	private void hideProgress() {
		mProgressBar.setVisibility(View.GONE);
	}

	class LoadPicAsyncTask extends AsyncTask<Object, Void, Bitmap> {

		private ImageView mImageView;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showProgress();
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			mImageView = (ImageView) params[0];
			String filePath = (String) params[1];
			boolean isFromUrl = (Boolean) params[2];
			if (isFromUrl) {
				return getBitmapFromUrl(mImageView, filePath);
			} else {
				return getBitmap(mImageView, filePath);
			}

		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			hideProgress();
			mImageView.setImageBitmap(result);
		}

	}
}