package com.geone.inspect.threepart_ts.activity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.esri.core.geometry.Point;
import com.example.android.photobyintent.AlbumStorageDirFactory;
import com.example.android.photobyintent.BaseAlbumDirFactory;
import com.example.android.photobyintent.FroyoAlbumDirFactory;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.adapter.TabPagerAdapter;
import com.geone.inspect.threepart_ts.bean.Case;
import com.geone.inspect.threepart_ts.bean.CaseImage;
import com.geone.inspect.threepart_ts.bean.CaseRecord;
import com.geone.inspect.threepart_ts.bean.Idiom;
import com.geone.inspect.threepart_ts.bean.Layer;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.js.JsCallAndroidObject;
import com.geone.inspect.threepart_ts.sql.MyDatabase;
import com.geone.inspect.threepart_ts.util.ImageUtils;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.MapEnum;
import com.geone.inspect.threepart_ts.util.Utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "SetJavaScriptEnabled", "SimpleDateFormat" })
/**核实核查处理签收界面*/
public class HSHCCLActivity extends FragmentActivity implements OnClickListener {
	// private static final String DETAIL_URL =
	// "http://58.210.9.131/sbjcz/DSFXCService/View/ViewCase.aspx?problemno=";
	// private static final String DETAIL_URL =
	// "http://172.25.89.10/sbjcz/dsfxcservice/view/ViewCase.aspx?problemNo=";
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private static final int img_bg_count = 3;
	private static final int record_bg_count = 3;
	private static final int ACTION_TAKE_PHOTO = 1;
	private static final int ACTION_CHOOSE_PHOTO = 2;
	private static final int ACTION_RECORD_SOUND = 3;
	private static final int ACTION_CHOOSE_PHOTO_KITKAT = 4;
	/** 保存，返回码 */
	// public static final int RESULT_SAVE=2;
	/** LABELS = { "图片", "声音" } */
	private static final String[] LABELS = { "图片", "声音" };
	/** 更新案件列表RESULT_CODE_UPDATELIST = 10 */
	public static final int RESULT_CODE_UPDATELIST = 10;
	/** 申请延时请求码 */
	public static final int REQUEST_CODE_DELAY = 12;
	/** 查看延时请求码 */
	public static final int REQUEST_CODE_DELAY_CHECK = 13;
	/** 查看拒签请求码 */
	public static final int REQUEST_CODE_REJCET_CHECK = 14;
	/** 申请缓办请求码 */
	public static final int REQUEST_CODE_POSTPONE = 16;
	/** 查看缓办请求码 */
	public static final int REQUEST_CODE_POSTPONE_CHECK = 17;

	private Context mContext;
	private WebView mWebView;
	private ProgressBar mProgressBar;

	private ImageButton button_takePhoto;
	private ImageButton button_recordSound;
	private Button btn_takephoto;
	private Button btn_pickphoto;
	private Button btn_cancel;
	// 图片声音
	private LinearLayout layout_gallery;
	private LinearLayout layout_sounds;
	/** 图片路径列表，与imglist保持一致 */
	private List<String> mPicPathList = new ArrayList<String>();
	/** 音频路径列表，与recordList保持一致 */
	private List<String> mSoundPathList = new ArrayList<String>();

	private LinearLayout layout_bottom_menu;

	private Switch mSwitch;
	/** 意见描述 */
	private AutoCompleteTextView autoSuggestDetail;
	private ArrayAdapter<Idiom> suggestAdapter;
	private ArrayList<Idiom> suggestList = new ArrayList<Idiom>();
	private TextView tv_suggest_title;
	/** 案件定位 */
	private Button btnLocateCase;

	private static String mCurrentPhotoPath;
	private static String mCurrentAudioPath;

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	private Case mCase;

	private static Animation bottom_in;
	private static Animation bottom_out;

	private ArrayList<CaseImage> imgList;
	private ArrayList<CaseRecord> recordList;
	private int request_code;
	private boolean isDraft;
	/** 是否正在上报，用于决定是否提交可读。默认false，上报菜单被点击时为true */
	private boolean isReportSelected = false;
	/** 是否处于延时申请中、延时已通过、延时未通过阶段，默认false */
	private boolean isDelay = false;
	/** 是否处于拒签阶段，默认false */
	private boolean isReject = false;
	/** 是否处于缓办阶段，默认false */
	private boolean isPostpone = false;
	// 退回案件
	private View mRejectView;
	private EditText etxtReject;
	private Dialog mRejectDialog;
	// Volley
	private RequestQueue mQueue;
	/** 案件位置点 */
	private Point mCasePnt;
	private String mPageTitle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mQueue = Volley.newRequestQueue(this);

		Intent mIntent = getIntent();
		mPageTitle = mIntent.getStringExtra("pageTitle");
		mCase = (Case) mIntent.getSerializableExtra("case");
		// request_code = mIntent.getIntExtra("request_code", -1);
		// isDraft = mIntent.getBooleanExtra("isDraft", false);
		setContentView(R.layout.event_hs);
		setTitle(R.string.case_verify);
		try {
			// 解决溢出菜单不显示问题
			ViewConfiguration mconfig = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(mconfig, false);
			}
		} catch (Exception ex) {
		}
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// 待处理模块：申请延时审批
		if ("2".equalsIgnoreCase(mCase.IsRead)
				|| "3".equalsIgnoreCase(mCase.IsRead)
				|| "4".equalsIgnoreCase(mCase.IsRead)) {
			isDelay = true;// menu显示查看延时
		} else if ("5".equalsIgnoreCase(mCase.IsRead)) {// 拒签阶段
			isReject = true;
		} else if ("11".equalsIgnoreCase(mCase.IsRead)
				|| "12".equalsIgnoreCase(mCase.IsRead)
				|| "13".equalsIgnoreCase(mCase.IsRead)) {// 缓办阶段
			isPostpone = true;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
		initView();
		// 获取常用语
		new GetSuggestIdiomAsyncTask().execute(mCase.wftype);
		if (mCase.isLocalExist) {
			setupDraft(mCase);
		}

	}

	@SuppressLint("JavascriptInterface")
	private void initView() {
		// 退回案件对话框
		mRejectView = initRejectView(getLayoutInflater());
		mWebView = (WebView) findViewById(R.id.webHSHCCL);
		mWebView.getSettings().setJavaScriptEnabled(true);
		// mWebView.getSettings().setDefaultFontSize(18);//设置默认字体大小
		// int textZoom=120;
		// settings.setTextZoom(textZoom);
		// settings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
		// edit by liyl2014-12-1-----
		// settings.setBuiltInZoomControls(true); // 是否支持触控缩放,默认false
		// settings.setSupportZoom(false); // 是否支持双击缩放，默认true
		// settings.setDefaultZoom(ZoomDensity.CLOSE);// 默认缩放模式
		// settings.setUseWideViewPort(true);// 为图片添加放大缩小功能，可以上下左右滑动
		// 将对象暴露给JavaScript，供其调用其中的方法
		mWebView.addJavascriptInterface(new JsCallAndroidObject(this),
				"JsCallAndroidObject");
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

		// 重写setWebViewClient阻止系统利用自带浏览器打开网页
		mWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				mProgressBar.setVisibility(View.VISIBLE);
			}

			public void onPageFinished(WebView view, String url) {
				mProgressBar.setVisibility(View.GONE);
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {

			}
		});
		String wfType = MapEnum.getWftype(mPageTitle);
		String url = "";
		if (AppApplication.isPubllic) {
			url = HttpQuery.serviceMap.get("ViewCase");
		} else {
			url = HttpQuery.serviceMap.get("ViewCase_vpn");
		}
		int end = url.lastIndexOf("=");
		String queryPre = url.substring(0, end + 1);
		String queryUrl = queryPre + mCase.ProblemNo + "&wftype=" + wfType
				+ "&cmdid=" + mCase.CmdID;

		// String url = DETAIL_URL + mCase.ProblemNo + "&wftype=" + wfType
		// + "&cmdid=" + mCase.CmdID;
		// queryUrl="http://news.baidu.com/";//测试
		mWebView.loadUrl(queryUrl);
		LogUtils.d("HSHCCLAcitivity", "webview:" + queryUrl);

		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

		mSwitch = (Switch) findViewById(R.id.switch1);
		mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

			}
		});
		btnLocateCase = (Button) findViewById(R.id.btnLocateCase);
		tv_suggest_title = (TextView) findViewById(R.id.tv_suggest_title);
		autoSuggestDetail = (AutoCompleteTextView) findViewById(R.id.autoSuggestDetail);
		suggestAdapter = Utils.initAutoCompleteTextView(mContext,
				autoSuggestDetail, suggestList);

		button_takePhoto = (ImageButton) findViewById(R.id.btn_take_photo);
		button_recordSound = (ImageButton) findViewById(R.id.btn_record_sound);
		layout_gallery = (LinearLayout) findViewById(R.id.layout_gallery);
		layout_sounds = (LinearLayout) findViewById(R.id.layout_sounds);
		layout_bottom_menu = (LinearLayout) findViewById(R.id.layout_bottom_menu);

		btnLocateCase.setOnClickListener(this);
		button_takePhoto.setOnClickListener(this);
		button_recordSound.setOnClickListener(this);

		setTitle(mPageTitle);
		String[] switchOffOn = MapEnum.getSwitch(mPageTitle);
		mSwitch.setTextOff(switchOffOn[0]);
		mSwitch.setTextOn(switchOffOn[1]);
		tv_suggest_title.setText(MapEnum.getSuggest(mPageTitle));

		if (Case.WFTYPES[2].equalsIgnoreCase(mCase.wftype)
				&& (!mCase.IsCanLSJA.equalsIgnoreCase("1"))) {// 不可以临时结案时屏蔽mSwitch开关
			Toast.makeText(mContext, "提示：此案件不可以临时结案！", Toast.LENGTH_SHORT)
					.show();
			mSwitch.setClickable(false);

		}
		initScrollViewBgWithImageView(layout_gallery, img_bg_count);
		initScrollViewBgWithImageView(layout_sounds, record_bg_count);
		initAnimation();
		initBottomView();

	}

	private View initRejectView(LayoutInflater layoutInflater) {
		View rejectView = layoutInflater.inflate(R.layout.dialog_reject, null);
		etxtReject = (EditText) rejectView.findViewById(R.id.etxtRejectReason);
		return rejectView;
	}

	/** 初始化界面数据 */
	private void setupDraft(Case mCase) {
		// 核实
		if (mCase.wftype.equalsIgnoreCase(Case.WFTYPES[0])) {
			if (Case.SWITCHS[0][1].equalsIgnoreCase(mCase.DealComment)) {
				mSwitch.setChecked(true);
			}
		}
		// 核查
		if (mCase.wftype.equalsIgnoreCase(Case.WFTYPES[1])) {
			if (Case.SWITCHS[1][1].equalsIgnoreCase(mCase.DealComment)) {
				mSwitch.setChecked(true);
			}
		}
		// 处理
		if (mCase.wftype.equalsIgnoreCase(Case.WFTYPES[2])) {
			if (Case.SWITCHS[2][1].equalsIgnoreCase(mCase.DealComment)) {
				mSwitch.setChecked(true);
			}
		}
		autoSuggestDetail.setText(mCase.Advice);
		if (mCase.imageList != null && mCase.imageList.size() > 0) {
			for (CaseImage image : mCase.imageList) {
				addPicToMyGallery(image.path);
			}
		}
		if (mCase.recordList != null && mCase.recordList.size() > 0) {
			for (CaseRecord record : mCase.recordList) {
				addSoundToMySoundsPool(record.path);
			}
		}

	}

	private void initAnimation() {
		bottom_in = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_in_from_bottom);
		bottom_out = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_out_to_bottom);
	}

	private void showBottomMenu() {
		layout_bottom_menu.startAnimation(bottom_in);
		layout_bottom_menu.setVisibility(View.VISIBLE);
	}

	private void hideBottomMenu() {
		layout_bottom_menu.startAnimation(bottom_out);
		layout_bottom_menu.setVisibility(View.GONE);
	}

	private void initBottomView() {

		btn_takephoto = (Button) findViewById(R.id.btn_takephoto);
		btn_pickphoto = (Button) findViewById(R.id.btn_pickphoto);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		btn_takephoto.setOnClickListener(this);
		btn_pickphoto.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);

	}

	/** liyl2015-7-16 选取图片，兼容4.0-5.0+ */
	private void pickImage() {
		Intent intent = new Intent();
		intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		// intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(
				Intent.createChooser(intent,
						getResources().getString(R.string.choose_img)),
				ACTION_CHOOSE_PHOTO);
	}

	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.app_name);
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory
					.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						LogUtils.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File image = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				getAlbumDir());
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	private File setUpPhotoFile() throws IOException {

		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();

		return f;
	}

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}

	private void handlePhoto() {

		if (mCurrentPhotoPath != null) {
			galleryAddPic();
			addPicToMyGallery(mCurrentPhotoPath);
		}

	}

	private void dispatchTakePictureIntent(int actionCode) {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		switch (actionCode) {
		case ACTION_TAKE_PHOTO:
			File f = null;

			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
			}
			break;

		default:
			break;
		} // switch

		startActivityForResult(takePictureIntent, actionCode);
	}

	private Bitmap getBitmap(String picPath) {
		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picPath, bmOptions);
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = 4;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		return BitmapFactory.decodeFile(picPath, bmOptions);
	}

	private void initScrollViewBgWithImageView(LinearLayout layout, int bg_count) {
		for (int i = 0; i < bg_count; i++) {
			final ImageView mImageView = new ImageView(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					171, 171);
			params.setMargins(5, 5, 10, 5);
			mImageView.setLayoutParams(params);
			mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

			mImageView.setImageResource(R.drawable.item_bg);
			layout.addView(mImageView, i);
		}

	}

	/** 恢复图片或声音的占位符 */
	private void resetScrollViewBgWithImageView(LinearLayout layout,
			int bg_count, int size) {
		if (size >= bg_count)
			return;
		for (int i = size; i < bg_count; i++) {
			final ImageView mImageView = new ImageView(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					171, 171);
			params.setMargins(5, 5, 10, 5);
			mImageView.setLayoutParams(params);
			mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

			mImageView.setImageResource(R.drawable.item_bg);
			if (layout.getChildAt(i) == null) {
				layout.addView(mImageView, i);
			}
		}

	}

	private void addPicToMyGallery(final String picPath) {

		// final Bitmap bitmap = ImageUtils.getBitmapThumbnail(picPath,
		// ImageUtils.THUMB_WITH_HEIGHTS[0][0],
		// ImageUtils.THUMB_WITH_HEIGHTS[0][1]);

		final ImageView imageview = new ImageView(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				ImageUtils.THUMB_WITH_HEIGHTS[0][0],
				ImageUtils.THUMB_WITH_HEIGHTS[0][1]);
		params.setMargins(5, 5, 10, 5);
		imageview.setLayoutParams(params);
		imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
		ImageUtils.loadBitmap(picPath, imageview);
		// imageview.setImageBitmap(bitmap);
		imageview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(HSHCCLActivity.this,
						PicViewActivity.class);
				intent.putExtra("image_path", picPath);
				startActivity(intent);
			}
		});
		mPicPathList.add(picPath);
		imageview.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				int position = getPositionByPath(mPicPathList, picPath);
				buildEditPicSoundDialog(mContext, imageview, position,
						LABELS[0]).show();
				return true;
			}
		});

		if (imgList == null) {
			imgList = new ArrayList<CaseImage>();
		}
		int size = imgList.size();
		if (size < img_bg_count) {
			layout_gallery.removeViewAt(size);
			layout_gallery.addView(imageview, size);
		} else {
			layout_gallery.addView(imageview);
		}

		// 保存入列表
		CaseImage mImage = new CaseImage();
		mImage.path = picPath;
		// mImage.processStage = "上报";
		imgList.add(mImage);

	}

	private void addSoundToMySoundsPool(final String soundPath) {

		final ImageView imageview = new ImageView(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(171,
				171);
		params.setMargins(5, 5, 10, 5);
		imageview.setLayoutParams(params);
		imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);

		imageview.setBackgroundResource(R.drawable.button_sound);
		imageview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(HSHCCLActivity.this,
						AudioPlayActivity.class);
				intent.putExtra("audio_path", soundPath);
				startActivity(intent);
			}
		});
		mSoundPathList.add(soundPath);
		imageview.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				int position = getPositionByPath(mSoundPathList, soundPath);
				buildEditPicSoundDialog(mContext, imageview, position,
						LABELS[1]).show();
				return true;
			}
		});
		if (recordList == null) {
			recordList = new ArrayList<CaseRecord>();
		}
		int size = recordList.size();
		if (size < record_bg_count) {
			layout_sounds.removeViewAt(size);
			layout_sounds.addView(imageview, size);
		} else {
			layout_sounds.addView(imageview);
		}

		// 保存入列表
		CaseRecord mRecord = new CaseRecord();
		mRecord.path = soundPath;
		// mRecord.processStage = "上报";
		recordList.add(mRecord);

	}

	/** 根据path获取其在list中位置,默认-1 */
	private int getPositionByPath(List<String> pathList, String path) {
		if (pathList == null || pathList.size() == 0) {
			return -1;
		}
		for (int i = 0; i < pathList.size(); i++) {
			if (path.equalsIgnoreCase(pathList.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/** 上报 */
	protected void report(Case inCase) {
		// isReportSelected = true;
		// Case uploadCase = getConfigedCase();

		// if (inCase == null) {
		// return;
		// }
		// if (Case.WFTYPES[2].equalsIgnoreCase(inCase.wftype)) {
		// // 处理人员图片可以为空
		// } else if (imgList == null || imgList.size() == 0) {
		// Toast.makeText(context, R.string.no_image, Toast.LENGTH_SHORT)
		// .show();
		// return;
		// }

		// 检查任务是否已经被别人处理
		new CheckHaveDoneAsyncTask().execute(inCase);
	}

	/** 收录 */
	private void sign(String caseID, String userID) {
		new SignCaseAsyncTask().execute(caseID, userID);
	}

	/** 取消收录 */
	private void unsign(String caseID, String userID) {
		new UnsignCaseAsyncTask().execute(caseID, userID);
	}

	/** 确认取消收录 */
	private Dialog buildUnsignDialog(final Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage(R.string.dialog_msg_unsign);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						unsign(mCase.CaseID, AppApplication.mUser.userID);
					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		return builder.create();
	}

	/** 确认收录 */
	private Dialog buildSignDialog(final Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage(R.string.dialog_msg_sign);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sign(mCase.CaseID, AppApplication.mUser.userID);
					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		return builder.create();
	}

	/**
	 * 确认上报
	 * 
	 * @param inCase
	 */
	private Dialog buildReportDialog(final Context context, final Case inCase) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage(R.string.dialog_msg_report);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						report(inCase);
					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		return builder.create();
	}

	/** 拒签案件 */
	private Dialog buildRejectCaseDialog(final Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("案件拒签");
		builder.setView(mRejectView);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String rejectReason = etxtReject.getText().toString();
						if (rejectReason == null
								|| "".equalsIgnoreCase(rejectReason)) {
							Toast.makeText(mContext, "申请原因不能为空",
									Toast.LENGTH_SHORT).show();
							return;
						}
						new RejectCaseAsyncTask().execute(
								AppApplication.mUser.userID, mCase.CaseID,
								rejectReason);

					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		return builder.create();
	}

	private Dialog buildEditPicSoundDialog(Context context,
			final ImageView imgeview, final int position, final String label) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		String msg = "";
		if ("图片".equalsIgnoreCase(label)) {
			msg = "确定删除该图片？";
		} else {
			msg = "确定删除该声音？";
		}
		builder.setMessage(msg);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (LABELS[0].equalsIgnoreCase(label)) {
							layout_gallery.removeView(imgeview);
							imgList.remove(position);
							mPicPathList.remove(position);
							resetScrollViewBgWithImageView(layout_gallery,
									img_bg_count, imgList.size());
						} else {
							layout_sounds.removeView(imgeview);
							recordList.remove(position);
							mSoundPathList.remove(position);
							resetScrollViewBgWithImageView(layout_sounds,
									record_bg_count, recordList.size());
						}
					}

				});
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		return builder.create();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		findViewById(R.id.layout_1).requestFocus();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		if ("0".equalsIgnoreCase(mCase.IsRead)) {
			setResult(RESULT_CODE_UPDATELIST);// 未读时通知刷新案件列表
		}
		super.onBackPressed();
		overridePendingTransition(0, R.anim.right_out);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		LogUtils.d("EventActivity", " --- onConfigurationChanged() ---");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				handlePhoto();
			}
			break;
		case ACTION_CHOOSE_PHOTO:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				mCurrentPhotoPath = filePath;
				cursor.close();
				addPicToMyGallery(filePath);
			}
			break;
		case ACTION_RECORD_SOUND:
			if (resultCode == RESULT_OK) {
				mCurrentAudioPath = data.getStringExtra("audio_path");
				addSoundToMySoundsPool(mCurrentAudioPath);
			}
			break;

		case REQUEST_CODE_DELAY:// 申请延时提交后
			if (resultCode == RESULT_OK) {
				isDelay = true;
				// 退出刷新case列表
				setResult(RESULT_CODE_UPDATELIST);
				finish();
			}
			break;
		case REQUEST_CODE_POSTPONE:// 申请缓办提交后
			if (resultCode == RESULT_OK) {
				isPostpone = true;
				// 退出刷新case列表
				setResult(RESULT_CODE_UPDATELIST);
				finish();
			}
			break;
		case REQUEST_CODE_REJCET_CHECK:// 查看拒签
			if (resultCode == DelayPostponeRejectCheckActivity.RESULT_REJECT_AGAIN) {
				// 显示拒签对话框
				if (mRejectDialog == null) {
					mRejectDialog = buildRejectCaseDialog(mContext);
				}
				mRejectDialog.show();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	// 只在初始化时调用一次
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.hshc_menu, menu);
		return true;
	}

	// 控制OptionsMenu显隐，每次显示菜单时都调用一次
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// 待处理or签收界面----
		if (Case.WFTYPES[2].equalsIgnoreCase(mCase.wftype)) {
			// 私有业务----
			if (TabPagerAdapter.PAGETITLES_MANAGE[1]
					.equalsIgnoreCase(mPageTitle)) {// 待处理
				menu.getItem(3).setVisible(true);// 签收
			} else {
				menu.getItem(4).setVisible(true);// 签收错误后退回
			}
			menu.getItem(2).setVisible(false);// 隐藏取消
			// 公共业务---

			if (isDelay) {// 延时阶段
				menu.getItem(5).setVisible(true);
				menu.getItem(6).setVisible(true);
				// menu.getItem(7).setVisible(true);//v2.9隐藏所有角色的缓办功能
				menu.getItem(5).setTitle(R.string.action_reject);// 拒签
				menu.getItem(6).setTitle(R.string.action_delay_check);// 查看延时
				menu.getItem(7).setTitle(R.string.action_postpone);// 缓办

			} else if (isReject) {// 拒签阶段
				menu.getItem(5).setVisible(true);
				menu.getItem(6).setVisible(true);// 隐藏延时菜单
				// menu.getItem(7).setVisible(true);
				menu.getItem(5).setTitle(R.string.action_reject_check);// 查看拒签
				menu.getItem(6).setTitle(R.string.action_delay);// 延时
				menu.getItem(7).setTitle(R.string.action_postpone);// 缓办

			} else if (isPostpone) {// 缓办阶段
				menu.getItem(5).setVisible(false);
				menu.getItem(6).setVisible(false);// 隐藏延时菜单
				// menu.getItem(7).setVisible(true);
				menu.getItem(7).setTitle(R.string.action_postpone_check);// 查看缓办

			} else {
				menu.getItem(5).setVisible(true);// 拒签
				menu.getItem(6).setVisible(true);// 延时
				// menu.getItem(7).setVisible(true);// 缓办
				menu.getItem(5).setTitle(R.string.action_reject);// 拒签
				menu.getItem(6).setTitle(R.string.action_delay);// 延时
				menu.getItem(7).setTitle(R.string.action_postpone);// 缓办

			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		Case uploadCase = null;
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.report:
			uploadCase = getConfigedCase();
			// 判断是否具备上报条件
			if (isEnableReport(uploadCase)) {
				buildReportDialog(mContext, uploadCase).show();
			} else {
				Toast.makeText(mContext, "必填（选）项为空无法上报，请完善数据后重试！",
						Toast.LENGTH_SHORT).show();
			}
			return true;
		case R.id.save:
			uploadCase = getConfigedCase();
			if (uploadCase == null) {
			} else {
				save(uploadCase);
				// 检查任务是否已经被别人处理
				// new CheckHaveDoneAsyncTask().execute(uploadCase);

			}
			return true;
		case R.id.cancel:
			onBackPressed();
			// String imgUrl =
			// "http://pic7.nipic.com/20100530/4865350_212226029138_2.jpg";
			// showWebImg(imgUrl);
			// String audioUrl =
			// "http://www.mobvcasting.com/android/audio/goodmorningandroid.mp3";
			// playWebAudio(audioUrl);
			return true;
		case R.id.sign:// 收录
			// Toast.makeText(mContext, "收录", Toast.LENGTH_SHORT).show();
			buildSignDialog(mContext).show();
			return true;
		case R.id.unsign:// 取消收录
			buildUnsignDialog(mContext).show();
			return true;
			// 申请延时、查看延时
		case R.id.delay:
			if (isDelay) {
				// new DelayCheckAsyncTask().execute(
				// "eeeb4b6f-349c-42fd-88c3-2c657b6a5ee4", "188");//示例
				new DelayPostponeRejectCheckAsyncTask().execute(
						AppApplication.mUser.userID, mCase.CaseID,
						DelayPostponeRejectCheckActivity.LABELS[0]);

			} else {
				intent = new Intent(mContext, DelayPostponeActivity.class);
				intent.putExtra("mCase", mCase);
				intent.putExtra("label", DelayPostponeActivity.LABELS[0]);
				startActivityForResult(intent, REQUEST_CODE_DELAY);
			}
			return true;
			// 拒绝处理
		case R.id.reject:
			if (isReject) {// 拒签阶段,查看拒签信息
				new DelayPostponeRejectCheckAsyncTask().execute(
						AppApplication.mUser.userID, mCase.CaseID,
						DelayPostponeRejectCheckActivity.LABELS[2]);
			} else {// 初始阶段

				if (mRejectDialog == null) {
					mRejectDialog = buildRejectCaseDialog(mContext);
				}
				mRejectDialog.show();
			}
			return true;
		case R.id.postpone:// 缓办
			if (isPostpone) {
				new DelayPostponeRejectCheckAsyncTask().execute(
						AppApplication.mUser.userID, mCase.CaseID,
						DelayPostponeRejectCheckActivity.LABELS[1]);
			} else {
				intent = new Intent(mContext, DelayPostponeActivity.class);
				intent.putExtra("mCase", mCase);
				intent.putExtra("label", DelayPostponeActivity.LABELS[1]);
				startActivityForResult(intent, REQUEST_CODE_POSTPONE);
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 判断是否具备上报条件
	 * 
	 * @param inCase
	 */
	private boolean isEnableReport(Case inCase) {

		if (inCase == null) {
			return false;
		}
		if (Case.WFTYPES[2].equalsIgnoreCase(inCase.wftype)) {
			// 处置人员:意见描述不能为空，照片可以--
			if (TextUtils.isEmpty(inCase.Advice)) {
				return false;
			}
		} else if (imgList == null || imgList.size() == 0) {
			// 巡查人员：意见可以为空，照片不可以
			return false;
		}
		return true;
	}

	private void save(Case uploadCase) {
		long saveValue = saveCaseToLocal(uploadCase);

		if (saveValue == -1) {
			Toast.makeText(mContext, "问题保存失败", Toast.LENGTH_SHORT).show();// 保存失败不退出当前界面
		} else {
			Toast.makeText(mContext, "已保存在本地", Toast.LENGTH_SHORT).show();
			setResult(RESULT_CODE_UPDATELIST);
			finish();
			overridePendingTransition(0, R.anim.right_out);
		}

	}

	/** 将Case保存到db，失败返回-1，其他成功 */
	private long saveCaseToLocal(Case inCase) {
		if (AppApplication.myDataBase == null) {
			// 初始化数据库
			AppApplication.myDataBase = new MyDatabase(this);
		}
		long returnValue = AppApplication.myDataBase.insertHSHCCLCase(inCase);
		return returnValue;
	}

	private void playWebAudio(String audioUrl) {
		Intent intent = new Intent(HSHCCLActivity.this, AudioPlayActivity.class);
		intent.putExtra("audio_path", audioUrl);
		startActivity(intent);
	}

	/** 开启一个Activity用于显示网络图片 */
	private void showWebImg(String url) {
		Intent intent = new Intent(HSHCCLActivity.this, PicViewActivity.class);
		intent.putExtra("image_url", url);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 案件位置
		case R.id.btnLocateCase:
			if (null == mCase.Shape || "".equalsIgnoreCase(mCase.Shape)) {
				Toast.makeText(mContext, "位置暂缺", Toast.LENGTH_SHORT).show();
			} else {
				mCasePnt = Utils.parseStringToPoint(mCase.Shape);
				if (Case.TYPE_DESCS[0].equalsIgnoreCase(mCase.CaseTypeDesc)) {
					// 部件
					requestMapServerUsingVolley(mCase.CaseClassI,
							mCase.CaseClassII, mCase.CaseClassIII);
				} else {
					// 事件
					Intent intent = new Intent(mContext, PickActivity.class);
					intent.putExtra("caseTypeDesc", Case.TYPE_DESCS[1]);
					intent.putExtra("casePnt", mCasePnt);
					startActivity(intent);
				}
			}
			break;
		case R.id.btn_take_photo:
			showBottomMenu();
			break;
		case R.id.btn_record_sound:
			Intent i = new Intent(this, AudioRecordActivity.class);
			startActivityForResult(i, ACTION_RECORD_SOUND);
			break;
		case R.id.btn_takephoto:
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
			hideBottomMenu();
			break;
		case R.id.btn_pickphoto:
			pickImage();
			hideBottomMenu();
			break;
		case R.id.btn_cancel:
			hideBottomMenu();
			break;
		default:
			break;
		}

	}

	private Case getConfigedCase() {

		mCase.InspectorID = AppApplication.mUser.userID;

		if (mSwitch.isChecked()) {
			mCase.DealComment = (String) mSwitch.getTextOn();
		} else {
			mCase.DealComment = (String) mSwitch.getTextOff();
		}
		mCase.Advice = autoSuggestDetail.getText().toString();
		if (imgList != null) {
			mCase.imageList = imgList;
		}
		if (recordList != null) {
			mCase.recordList = recordList;
		}

		return mCase;
	}

	/** 根据大小子类获取图层服务 */
	private void requestMapServerUsingVolley(String largeClass,
			String smallClass, String subClass) {
		Utils.showWaitingDialog(mContext);
		// Volley
		String URL = "";
		if (AppApplication.isPubllic) {
			URL = HttpQuery.serviceMap.get("GetMapServer");
		} else {
			URL = HttpQuery.serviceMap.get("GetMapServer_vpn");
		}

		int end = URL.lastIndexOf("=");
		String querySQL = URL.substring(end + 1);
		querySQL = querySQL.replaceAll("#classI#", largeClass);
		querySQL = querySQL.replaceAll("#classII#", smallClass);
		querySQL = querySQL.replaceAll("#classIII#", subClass);

		try {
			querySQL = URLEncoder.encode(querySQL, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		URL = URL.substring(0, end + 1);

		String url = URL + querySQL;
		// 公用设施，供水井盖
		// url =
		// "http://58.210.9.131/DataCenter/querySQL.ashx?DBTag=DSFXC&querySQL=exec+WF_M_GetMapserver+%271010%27%2C%27101001%27%2C%27%27";
		LogUtils.d("HSHCCLActivity", "专题图服务:" + url);
		mQueue.add(new JsonArrayRequest(url, new Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray array) {
				Utils.hideWaitingDialog();
				if (array == null || array.length() == 0
						|| array.toString().contains("error")) {
					Toast.makeText(mContext, "没有找到相关服务", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				try {
					int length = array.length();
					if (length > 0) {
						Layer layer = new Layer();
						JSONObject obj = (JSONObject) array.get(0);

						// layer.id="BEC2B77B-0D41-4935-BDD9-C000B9D2468C";
						// layer.url="http://58.210.9.131/sipsd/rest/services/CSBJ/GREEN/MapServer";
						// layer.subIds="0,2";
						layer.id = obj.optString("ID");
						layer.url = obj.optString("Url");
						layer.subIds = obj.optString("subIds");

						// String maplayerURL = obj.getString("serverUrl");
						// 测试用，使用时注释掉该行即可
						// mCasePnt = new Point(64577.28812553595,
						// 47617.84577565513);
						// mCase.thingId = "81399";
						Intent intent = new Intent(mContext, PickActivity.class);
						intent.putExtra("caseTypeDesc", Case.TYPE_DESCS[0]);
						intent.putExtra("layer", layer);
						intent.putExtra("casePnt", mCasePnt);
						intent.putExtra("thingId", mCase.thingId);
						startActivity(intent);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				Utils.hideWaitingDialog();
				Toast.makeText(mContext, "没有找到相关服务", Toast.LENGTH_SHORT).show();
			}

		}));

	}

	//上报请求
	class ReportAsyncTask extends AsyncTask<Object, Void, Object> {

		private Case currentCase;
		private String dealComment;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Utils.showWaitingDialog(HSHCCLActivity.this);
		}

		@Override
		protected Object doInBackground(Object... args) {
			Case mCase = (Case) args[0];
			currentCase = mCase;
			dealComment = mCase.DealComment;
			return HttpQuery.doHSHCCL(mCase);
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Utils.hideWaitingDialog();

			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(HSHCCLActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
				currentCase.status = "false";
				// saveCaseToLocal(currentCase);//需要插入数据库吗？

				return;
			} else if (result instanceof Error) {
				Error e = (Error) result;
				Toast.makeText(HSHCCLActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
				currentCase.status = "false";
				// saveCaseToLocal(currentCase);//需要插入数据库吗？

			} else {
				String returnStr = (String) result;
				Toast.makeText(HSHCCLActivity.this, returnStr,
						Toast.LENGTH_SHORT).show();

			}
			// Intent i = new Intent();
			// i.putExtra("processResult",
			// dealComment);//?2015 liyl
			// setResult(RESULT_OK, i);
			setResult(RESULT_CODE_UPDATELIST);
			finish();
			overridePendingTransition(0, R.anim.right_out);

		}

	}

	/** 取消收录案件请求 */
	class UnsignCaseAsyncTask extends AsyncTask<String, Void, Object> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Utils.showWaitingDialog(mContext);
		}

		@Override
		protected Object doInBackground(String... args) {

			return HttpQuery.unsign(args[0], args[1]);
		}

		@Override
		protected void onPostExecute(Object result) {
			Utils.hideWaitingDialog();
			super.onPostExecute(result);
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			// if (result instanceof String
			// && "1".equalsIgnoreCase((String) result)) {
			// Toast.makeText(mContext, "取消收录成功", Toast.LENGTH_SHORT).show();
			//
			// }
			// 退出并通知更新
			setResult(RESULT_CODE_UPDATELIST);
			finish();
			overridePendingTransition(0, R.anim.right_out);
		}

	}

	/** 收录案件请求 */
	class SignCaseAsyncTask extends AsyncTask<String, Void, Object> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Utils.showWaitingDialog(mContext);
		}

		@Override
		protected Object doInBackground(String... args) {

			return HttpQuery.sign(args[0], args[1]);
		}

		@Override
		protected void onPostExecute(Object result) {
			Utils.hideWaitingDialog();
			super.onPostExecute(result);
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			// if (result instanceof String
			// && "1".equalsIgnoreCase((String) result)) {
			// Toast.makeText(mContext, "收录成功", Toast.LENGTH_SHORT).show();
			//
			// }
			// 退出并通知更新
			setResult(RESULT_CODE_UPDATELIST);
			finish();
			overridePendingTransition(0, R.anim.right_out);
		}

	}

	/** 退回案件 */
	class RejectCaseAsyncTask extends AsyncTask<String, Void, Object> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Utils.showWaitingDialog(mContext);
		}

		@Override
		protected Object doInBackground(String... args) {

			return HttpQuery.rejectCase(args[0], args[1], args[2]);
		}

		@Override
		protected void onPostExecute(Object result) {
			Utils.hideWaitingDialog();
			super.onPostExecute(result);
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (result instanceof String) {
				if ("1".equalsIgnoreCase(result.toString())) {
					Toast.makeText(mContext, R.string.reject_success,
							Toast.LENGTH_SHORT).show();
					setResult(RESULT_CODE_UPDATELIST);
					finish();
				} else {
					// 失败，无操作
					Toast.makeText(mContext, R.string.reject_failure,
							Toast.LENGTH_SHORT).show();

				}
			}
		}

	}

	/** 获取意见描述常用语 */
	class GetSuggestIdiomAsyncTask extends AsyncTask<String, Void, Object> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			// Utils.showWaitingDialog(mContext);
		}

		@Override
		protected Object doInBackground(String... args) {
			// 参数wftype
			return HttpQuery.getSuggestIdiom(args[0]);
		}

		@Override
		protected void onPostExecute(Object result) {
			// Utils.hideWaitingDialog();
			super.onPostExecute(result);
			if (result instanceof Exception) {

				return;
			}
			if (result instanceof ArrayList) {
				ArrayList<Idiom> idiomList = (ArrayList<Idiom>) result;
				if (idiomList == null || idiomList.size() == 0) {
					return;
				}
				suggestList.clear();
				suggestList.addAll(idiomList);
				suggestAdapter.notifyDataSetChanged();
			}

		}

	}

	/** 查看延时、缓办、拒签 */
	class DelayPostponeRejectCheckAsyncTask extends
			AsyncTask<String, Void, Object> {
		private String label = DelayPostponeRejectCheckActivity.LABELS[0];// 默认查看延迟
		private boolean isReject;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Utils.showWaitingDialog(mContext);
		}

		@Override
		protected Object doInBackground(String... args) {

			// 0userid 1caseid 2 labels
			label = args[2];
			if (args[2]
					.equalsIgnoreCase(DelayPostponeRejectCheckActivity.LABELS[1])) {// 缓办
				return HttpQuery.getPostpone(args[0], args[1]);
			} else if (args[2]
					.equalsIgnoreCase(DelayPostponeRejectCheckActivity.LABELS[2])) {// 拒签
				return HttpQuery.getReject(args[0], args[1]);
			} else {
				// 默认延迟
				return HttpQuery.getDelay(args[0], args[1]);
			}

		}

		@Override
		protected void onPostExecute(Object result) {
			Utils.hideWaitingDialog();
			super.onPostExecute(result);
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (result instanceof Case) {
				Intent intent = new Intent(mContext,
						DelayPostponeRejectCheckActivity.class);
				intent.putExtra("label", label);
				if (label
						.equalsIgnoreCase(DelayPostponeRejectCheckActivity.LABELS[1])) {// 缓办
					intent.putExtra("postponeCase", (Case) result);
					startActivity(intent);
				} else if (label
						.equalsIgnoreCase(DelayPostponeRejectCheckActivity.LABELS[2])) {// 拒签
					intent.putExtra("rejectCase", (Case) result);
					startActivityForResult(intent, REQUEST_CODE_REJCET_CHECK);

				} else {
					// 默认延迟
					intent.putExtra("delayCase", (Case) result);
					startActivity(intent);

				}

			}

		}

	}

	/** 提交任务已处理回执 RS=1:未处理，可以提交；0：已处理，不能再提交 */
	class CheckHaveDoneAsyncTask extends AsyncTask<Object, Void, Object> {
		Case uploadCase = new Case();

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Object doInBackground(Object... args) {
			uploadCase = (Case) args[0];
			return HttpQuery.checkHaveDone(uploadCase);
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (result instanceof String) {
				if ("1".equalsIgnoreCase(result.toString())) {
					// Toast.makeText(HSHCActivity.this, "rs=1,任务未处理",
					// Toast.LENGTH_SHORT).show();
					uploadCase.postTime = Utils.formatDate(Calendar
							.getInstance().getTime(), "yyyy-MM-dd HH:mm:ss");
					new ReportAsyncTask().execute(uploadCase);
				} else {
					// 提示处理其他案件
					Toast.makeText(mContext, R.string.tip_have_done,
							Toast.LENGTH_SHORT).show();
					setResult(RESULT_CODE_UPDATELIST);
					finish();
					overridePendingTransition(0, R.anim.right_out);
				}
				// 不管有没有被别人处理都删除本地记录
				AppApplication.myDataBase.deleteHSHCCLCase(uploadCase);
			}

		}

	}

	/** 初始化AutoCompleteTextView */
	private void initCommentsAuto(AutoCompleteTextView atuoText,
			String[] stringArray, String initValue) {
		atuoText.setText(initValue);
		ArrayAdapter<String> commentsAdapter = new ArrayAdapter<String>(
				mContext, android.R.layout.simple_dropdown_item_1line,
				stringArray);
		atuoText.setAdapter(commentsAdapter);
		atuoText.setThreshold(1);
		// atuoText.setOnFocusChangeListener(this);
		// atuoText.setOnTouchListener(this);
	}
}