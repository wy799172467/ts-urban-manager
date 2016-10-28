package com.geone.inspect.threepart_ts.activity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.LocationDisplayManager.AutoPanMode;
import com.esri.android.map.TiledLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.example.android.photobyintent.AlbumStorageDirFactory;
import com.example.android.photobyintent.BaseAlbumDirFactory;
import com.example.android.photobyintent.FroyoAlbumDirFactory;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.Case;
import com.geone.inspect.threepart_ts.bean.CaseImage;
import com.geone.inspect.threepart_ts.bean.CaseRecord;
import com.geone.inspect.threepart_ts.bean.Category;
import com.geone.inspect.threepart_ts.bean.Condition;
import com.geone.inspect.threepart_ts.bean.IBean;
import com.geone.inspect.threepart_ts.bean.Layer;
import com.geone.inspect.threepart_ts.bean.User;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.sql.MyDatabase;
import com.geone.inspect.threepart_ts.tianditu.TianDiTuLayer;
import com.geone.inspect.threepart_ts.tianditu.TianDiTuLayerTypes;
import com.geone.inspect.threepart_ts.util.GeometryUtils;
import com.geone.inspect.threepart_ts.util.ImageUtils;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.Utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

public class ReportActivity extends FragmentActivity implements
		OnClickListener, OnItemSelectedListener, OnTouchListener,OnCheckedChangeListener {

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	/** EMERGENCYS={ { "正常上报", "0" },{ "随手处置", "1" } } */
	public static final String[][] EMERGENCYS = { { "正常上报", "0" },
			{ "随手处置", "1" } };

	/** LABELS = { "图片", "声音" } */
	private static final String[] LABELS = { "图片", "声音" };

	private Context mContext;

	private static final int ACTION_TAKE_PHOTO = 1;
	private static final int ACTION_CHOOSE_PHOTO = 2;
	private static final int ACTION_RECORD_SOUND = 3;
	/** 跳转至手动选点界面RequestCode，部件 */
	private static final int REQUEST_PICK_BJ = 4;
	/** 跳转至手动选点界面RequestCode，事件 */
	private static final int REQUEST_PICK_SJ = 5;

	private static final int UPDATE_USER_LOCATION = 0;

	public static Handler postHandler;

	private RadioGroup mTypeRadioGroup;
	private RadioButton radio0;
	private Spinner largeClassSpinner;
	private Spinner smallClassSpinner;
	private Spinner subClassSpinner;

	private ImageButton button_takePhoto;
	private ImageButton button_recordSound;
	private Button btn_takephoto;
	private Button btn_pickphoto;
	private Button btn_cancel;

	private MapView mMapView;
	private LocationDisplayManager mLocationDisplayManager;
	// private EditText et_caseDetails;
	/** 问题描述 */
	private AutoCompleteTextView et_caseDetails;
	/** 当前位置地址信息 */
	private EditText et_caseAddr;
	private LinearLayout layout_gallery;
	private LinearLayout layout_sounds;
	private LinearLayout layout_bottom_menu;

	private static String mCurrentPhotoPath;
	private static String mCurrentAudioPath;

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	private String case_type = "10"; // 10 部件 20 事件
	// private String case_type_description = "部件";
	private String caseLargeClass = ""; // 大类
	private String caseSmallClass = ""; // 小类
	private String caseSubClass = ""; // 子类

	private ArrayList<Category> largeClassList = new ArrayList<Category>();
	private ArrayList<Category> smallClassList = new ArrayList<Category>();
	private ArrayList<Category> subClassList = new ArrayList<Category>();

	private ArrayAdapter<Category> adapter_largeClass;
	private ArrayAdapter<Category> adapter_smallClass;
	private ArrayAdapter<Category> adapter_subClass;
	/** 立案条件 */
	private ArrayList<Condition> conditionList = new ArrayList<Condition>();
	private ArrayAdapter<Condition> adapter_condition;
	private Spinner conditionSpinner;
	/** 立案条件 */
	private Switch swtEmergency;

	/** 根据大小子类获取到的专题图层的Id */
	private String mLayerId;
	/** 用户所选部件的id */
	private String mThingId;
	private int largeIndex = 0, smallIndex = 0, subIndex = 0;

	private String createTime;

	// 特别关注几个状态---
	/** 是否处于选点状态 ，默认false，使用当前位置（不一定是用户GPS位置，可能是手动选择的点）未被选中时选点状态为true */
	private boolean isSelectingCasePnt = false;
	/** 是否使用当前位置 ，新建默认true，手动选点完成后 为false；取消后再选中用当前位置为true；编辑状态下默认为false */
	private boolean isUseCurrentPnt = true;

	/** 判断是否新建，默认true */
	private boolean isNew = true;
	/** 是否处于编辑状态，区别于新建 */
	private boolean isDraft = false;
	/** 是否是已上报的case */
	private boolean isFinished = false;
	private Case mDraftCase;
	private String newProblemNo; // 新建上报案件时，要获取的problemNo

	private ArrayList<CaseImage> imgList;
	private ArrayList<CaseRecord> recordList;
	/** 图片路径列表，与imglist保持一致 */
	private List<String> mPicPathList = new ArrayList<String>();
	/** 音频路径列表，与recordList保持一致 */
	private List<String> mSoundPathList = new ArrayList<String>();

	private static final int img_bg_count = 3;
	private static final int record_bg_count = 3;

	private static Animation bottom_in;
	private static Animation bottom_out;

	// Volley
	private RequestQueue mQueue;
	/** 手动获取位置图标 */
	// private ImageView imgLocationGreen;
	/** 使用GPS当前位置 */
	private Button btnCurrentPnt;
	/** 手动选点 */
	private Button btnSelectPntManual;
	/** 手动获取位置取消 */
	// private Button btnSelectPntCancel;
	/** 用户选择的点（gps或部件）所在GraphicsLayer */
	private GraphicsLayer casePntGlayer;
	/** 问题位置，手动获取或使用当前位置 */
	private Point mCasePnt;
	/** 手动选取的位置点 */
	// private Point pickPnt;
	/** 用户当前位置点 */
	private Point currentPnt;

	/** 编辑状态下是否使用上次选择的点，默认false */
	private boolean isUseDraftPnt = false;
	SharedPreferences mDefaultPrefs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mQueue = Volley.newRequestQueue(this);
		mDefaultPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		setContentView(R.layout.event);
		setTitle(R.string.case_report);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		postHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case UPDATE_USER_LOCATION:
					if(isFollowingLocation){
						getCurrentLocation(false);
					}
					break;

				default:
					break;
				}
			}
		};
		// 一数据---
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
		Intent mIntent = getIntent();
		isDraft = mIntent.getBooleanExtra("isDraft", false);
		isFinished = mIntent.getBooleanExtra("isFinished", false);
		newProblemNo = mIntent.getStringExtra("newProblemNo");
		if (isDraft || isFinished) {
			isNew = false;// 用于初始化UI的控制
		}
		if (isDraft) {
			mDraftCase = (Case) mIntent.getSerializableExtra("case");
			// setupBaseData(mDraftCase);
			// setupDraft(mDraftCase);

			// isUseCurrentPnt = false;// 编辑状态下默认不使用当前位置
			isUseDraftPnt = true;
		} else {
			if (isFinished) {
				mDraftCase = (Case) mIntent.getSerializableExtra("case");
				// setupBaseData(mDraftCase);
				// setupDraft(mDraftCase);
				setTitle(R.string.case_details);
			}
			// else {
			// requestProblemNoUsingVolley();
			// }
		}
		// 二地图---
		initMapView();
		// 三界面---
		initView();
		initAnimation();

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
		// Call MapView.pause to suspend map rendering while the activity is
		// paused, which can save battery usage.
		if (mMapView != null) {
			mMapView.pause();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// Call MapView.unpause to resume map rendering when the activity
		// returns to the foreground.
		if (mMapView != null) {
			mMapView.unpause();
		}
		ArcGISRuntime.setClientId("kduw6jknnq8Ayn0d");
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(0, R.anim.right_out);
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

	private void initView() {
		Calendar c = Calendar.getInstance();
		String formattedTime = Utils.formatDate(c.getTime(),
				"yyyy-MM-dd HH:mm:ss");
		createTime = formattedTime;
		// 部件、事件---
		mTypeRadioGroup = (RadioGroup) findViewById(R.id.radioGroup_type);


		mButtonLocateMe = (Button) findViewById(R.id.buttonLocateMe);
		mButtonLocateMe.setOnClickListener(this);

		// if (isDraft || isFinished) {
		// //初始化完UI以后再选择
		// } else {
		// mTypeRadioGroup.check(R.id.radio0);// 默认选择第一个
		// }
		mTypeRadioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {// 新建、编辑时都有可能触发
						// String case_type_description = "部件";
						switch (checkedId) {
						case R.id.radio0:
							case_type = "10";
							if (isFinished) {
							} else {
								btnSelectPntManual.setVisibility(View.VISIBLE);
							}
							// case_type_description = "部件";
							break;
						case R.id.radio1:
							case_type = "20";
							// btnSelectPntManual.setVisibility(View.GONE);
							// case_type_description = "事件";
							break;
						default:
							break;
						}
						largeClassList = AppApplication.myDataBase
								.getCaseLargeClassByType(case_type);
						if (adapter_largeClass == null) {
							adapter_largeClass = new ArrayAdapter<Category>(
									mContext,
									android.R.layout.simple_spinner_item,
									largeClassList);
							adapter_largeClass
									.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

						} else {
							adapter_largeClass.clear();
							adapter_largeClass.addAll(largeClassList);
							adapter_largeClass.notifyDataSetChanged();
						}
						largeClassSpinner.setAdapter(adapter_largeClass);// 触发ItemSelected事件
					}
				});
		// init spinner
		largeClassSpinner = (Spinner) findViewById(R.id.spin_category_1);
		smallClassSpinner = (Spinner) findViewById(R.id.spin_category_2);
		subClassSpinner = (Spinner) findViewById(R.id.spin_category_3);
		// 设置上报类型
		swtEmergency = (Switch) findViewById(R.id.swtEmergency);
		swtEmergency.setTextOff(EMERGENCYS[0][0]);// 正常上报
		swtEmergency.setTextOn(EMERGENCYS[1][0]);// 随手处置
		// 立案条件
		conditionSpinner = (Spinner) findViewById(R.id.spin_condition);
		if (adapter_condition == null) {
			adapter_condition = new ArrayAdapter<Condition>(mContext,
					android.R.layout.simple_spinner_item, conditionList);
			adapter_condition
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			conditionSpinner.setAdapter(adapter_condition);
		}
		et_caseDetails = (AutoCompleteTextView) findViewById(R.id.et_details);
		// 获取用户设置的日常用语用于初始化AutoCompleteTextView的下拉列表
		// Set<String> commentSet = mDefaultPrefs.getStringSet(
		// CCommentsListActivity.COMMENT_LIST, new HashSet<String>());
		// Utils.initAutoCompleteTextView(this, et_caseDetails, commentSet);

		et_caseAddr = (EditText) findViewById(R.id.et_addr);
		layout_gallery = (LinearLayout) findViewById(R.id.layout_gallery);
		layout_sounds = (LinearLayout) findViewById(R.id.layout_sounds);
		layout_bottom_menu = (LinearLayout) findViewById(R.id.layout_bottom_menu);

		button_takePhoto = (ImageButton) findViewById(R.id.btn_take_photo);
		button_recordSound = (ImageButton) findViewById(R.id.btn_record_sound);
		initScrollViewBgWithImageView(layout_gallery, img_bg_count);
		initScrollViewBgWithImageView(layout_sounds, record_bg_count);

		// 地图————————————————————————————————————————————————————

		btnCurrentPnt = (Button) findViewById(R.id.btnCurrentPnt);
		btnSelectPntManual = (Button) findViewById(R.id.btnSelectPntManual);
		btnCurrentPnt.setOnClickListener(this);
		btnSelectPntManual.setOnClickListener(this);
		button_takePhoto.setOnClickListener(this);
		button_recordSound.setOnClickListener(this);
		initBottomView();
		// 还原数据
		if (isNew) {
			mTypeRadioGroup.check(R.id.radio0);// 默认选中第一个
		} else {
			if (isFinished) {
				// 上报后隐藏选点模块
				btnCurrentPnt.setVisibility(View.GONE);
				btnSelectPntManual.setVisibility(View.GONE);
			}
			if (Case.TYPE_DESCS[0].equalsIgnoreCase(mDraftCase.CaseTypeDesc)) {// 部件
				mTypeRadioGroup.check(R.id.radio0);
			} else {
				mTypeRadioGroup.check(R.id.radio1);

			}
			setupBaseData(mDraftCase);
			setupDraft(mDraftCase);// 初始化数据
		}
		largeClassSpinner.setOnItemSelectedListener(this);
		smallClassSpinner.setOnItemSelectedListener(this);
		subClassSpinner.setOnItemSelectedListener(this);

	}

	private void initBottomView() {

		btn_takephoto = (Button) findViewById(R.id.btn_takephoto);
		btn_pickphoto = (Button) findViewById(R.id.btn_pickphoto);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		btn_takephoto.setOnClickListener(this);
		btn_pickphoto.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);

	}

	private void initMapView() {
		mMapView = (MapView) findViewById(R.id.mapView);
		initBaseMap();

		casePntGlayer = new GraphicsLayer();


		Point p = new Point(119.030095667, 32.0293513673);
		// mMapView.centerAt(p, true);
//		mCasePnt = p;
//		markLocation(p);
		mMapView.zoomToScale(p, mMapView.getScale() / 4);



		mMapView.addLayer(casePntGlayer);

		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(Object source, STATUS status) {
				// Check if a layer is failed to be loaded due to security
				if (source == mMapView && status == STATUS.INITIALIZED) {
					mLocationDisplayManager = mMapView
							.getLocationDisplayManager();
					mLocationDisplayManager.setAccuracyCircleOn(true);
					mLocationDisplayManager.setAutoPanMode(AutoPanMode.OFF);
					mLocationDisplayManager.start();
					if (isDraft || isFinished) {// 保存或上报后打开
						if (mDraftCase.X != 0) {
							// 点位为null时不再标记和缩放
							Point p = new Point(mDraftCase.X, mDraftCase.Y);
							// mMapView.centerAt(p, true);
							mCasePnt = p;
							markLocation(p);
//							mMapView.zoomToScale(p, mMapView.getScale() / 4);
							mMapView.zoomToScale(p, mMapView.getScale() / Math.pow(2,14));
						} else {
							Toast.makeText(mContext, "温馨提示：上次您没有保存位置信息！",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						//Message msg = postHandler.obtainMessage(UPDATE_USER_LOCATION);
						//postHandler.sendMessageDelayed(msg, 4000);
						Point p = new Point(119.030095667, 32.0293513673);
						mMapView.zoomToScale(p, mMapView.getScale() / Math.pow(2,14));
					}

				}
			}

		});

	}

	private void initBaseMap() {
		TiledLayer tiledLayer = null;
		try {
			tiledLayer = Utils.getLocalLayer(AppApplication.BASE_MAP_TPK_NAME);
			if (tiledLayer == null) {
//				String basemap_url = Utils.getPublicVpnUrl("Mapserver_Public");
//				tiledLayer = Utils.getTiledLayer(basemap_url);
				com.esri.android.map.Layer mapLayer = new TianDiTuLayer(TianDiTuLayerTypes.TIANDITU_VECTOR_2000);
				mMapView.addLayer(mapLayer);
				com.esri.android.map.Layer annotationLayer = new TianDiTuLayer(
						TianDiTuLayerTypes.TIANDITU_VECTOR_ANNOTATION_CHINESE_2000);
				mMapView.addLayer(annotationLayer);

			}
			mMapView.addLayer(tiledLayer);
		} catch (Exception ex) {
//			String basemap_url = Utils.getPublicVpnUrl("Mapserver_Public");
//			tiledLayer = Utils.getTiledLayer(basemap_url);
			com.esri.android.map.Layer mapLayer = new TianDiTuLayer(TianDiTuLayerTypes.TIANDITU_VECTOR_2000);
			mMapView.addLayer(mapLayer);
			com.esri.android.map.Layer annotationLayer = new TianDiTuLayer(
					TianDiTuLayerTypes.TIANDITU_VECTOR_ANNOTATION_CHINESE_2000);
			mMapView.addLayer(annotationLayer);
//			mMapView.addLayer(tiledLayer);
		}

	}

	/** 设置无关联数据 */
	private void setupBaseData(Case mCase) {
		createTime = mCase.createTime;
		et_caseDetails.setText(mCase.ReportCaseDesc);
		et_caseAddr.setText(mCase.ReportAddress);
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
		// 设置随手处置
		if (mCase.emergency.equalsIgnoreCase(EMERGENCYS[1][1])) {
			swtEmergency.setChecked(true);// 打开开关
		} else {
			swtEmergency.setChecked(false);// 关闭开关
		}

	}

	/** 设置关联数据（三级联动） */
	private void setupDraft(Case mCase) {

		largeIndex = setSpinnerSelection(largeClassSpinner, largeClassList,
				mCase.CaseClassIDesc);
		// 处理smallClassSpinner——————
		smallClassList = AppApplication.myDataBase
				.getCaseSmallClassByType(largeClassList.get(largeIndex).value);
		if (smallClassList.size() == 0) {
			smallClassSpinner.setVisibility(View.GONE);
		} else {
			smallClassSpinner.setVisibility(View.VISIBLE);
			if (adapter_smallClass == null) {
				adapter_smallClass = new ArrayAdapter<Category>(this,
						android.R.layout.simple_spinner_item, smallClassList);
				adapter_smallClass
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			} else {
				adapter_smallClass.clear();
				adapter_smallClass.addAll(smallClassList);
				adapter_smallClass.notifyDataSetChanged();
			}
			smallClassSpinner.setAdapter(adapter_smallClass);
			smallIndex = setSpinnerSelection(smallClassSpinner, smallClassList,
					mCase.CaseClassIIDesc);
			// 处理subClassSpinner——————
			subClassList = AppApplication.myDataBase
					.getCaseSubClassByType(smallClassList.get(smallIndex).value);
			if (subClassList.size() == 0) {
				subClassSpinner.setVisibility(View.GONE);
			} else {
				subClassSpinner.setVisibility(View.VISIBLE);
				if (adapter_subClass == null) {
					adapter_subClass = new ArrayAdapter<Category>(this,
							android.R.layout.simple_spinner_item, subClassList);
					adapter_subClass
							.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				} else {
					adapter_subClass.clear();
					adapter_subClass.addAll(subClassList);
					adapter_subClass.notifyDataSetChanged();
				}
				subClassSpinner.setAdapter(adapter_subClass);
				subIndex = setSpinnerSelection(subClassSpinner, subClassList,
						mCase.CaseClassIIIDesc);
			}
		}
		// 设置立案条件
		if (largeIndex + smallIndex < 2) {
			resetConditionSpinner();
		} else {
			String largeClass = ((Category) largeClassSpinner.getSelectedItem()).value;
			String smallClass = ((Category) smallClassSpinner.getSelectedItem()).value;
			requestConditionUsingVolley(largeClass, smallClass, "",
					mCase.caseCondition);
		}

	}

	private int setSpinnerSelection(Spinner mSpinner,
			ArrayList<Category> categoryList, String selectValue) {
		for (int i = 0; i < categoryList.size(); i++) {
			Category mCategory = categoryList.get(i);
			if (mCategory.description.equals(selectValue)) {
				mSpinner.setSelection(i, true);// ？这个方法直接选中却不触发ItemSelected方法，且此处用setSelection(position)无效果却触发
				return i;
			}
		}
		return 0;

	}

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

	/** liyl 2015-7-7 扫描更新媒体库，以便显示刚创建的文件 */
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

//	/** 异步加载图片 */
//	public void loadBitmap(String file_path, ImageView imageView) {
//		BitmapWorkerTask task = new BitmapWorkerTask(imageView);
//		task.execute(file_path, ImageUtils.THUMB_WITH_HEIGHTS[0][0],
//				ImageUtils.THUMB_WITH_HEIGHTS[0][1]);
//	}

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

	// private Bitmap getBitmap(String picPath) {
	// Bitmap bm = null;
	// /* Get the size of the image */
	// BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	// bmOptions.inJustDecodeBounds = true;
	// BitmapFactory.decodeFile(picPath, bmOptions);
	// bmOptions.inSampleSize = ImageUtils.caculateInSampleSize(bmOptions,
	// 720, 1280);// S3截屏尺寸720, 1280
	// bmOptions.inJustDecodeBounds = false;
	// bmOptions.inPurgeable = true;
	// /* Decode the JPEG file into a Bitmap */
	// bm = BitmapFactory.decodeFile(picPath, bmOptions);
	// bm = ImageUtils.compressBitmap(bm, 200);// 图片质量压缩到200K以内
	// return bm;
	// // return BitmapFactory.decodeFile(picPath, bmOptions);
	// }

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

	/**
	 * 添加拍照
	 * 
	 * @param picPath
	 *            图片路径
	 * @param //outWith
	 *            输出宽度px
	 * @param //outHeight
	 *            输出高度px
	 */
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
				Intent intent = new Intent(ReportActivity.this,
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
		CaseImage caseImage = new CaseImage();
		if (isNew) {
			caseImage.caseID = newProblemNo;
		} else {
			caseImage.caseID = mDraftCase.ProblemNo;
		}

		caseImage.path = picPath;
		// caseImage.processStage = "上报";
		imgList.add(caseImage);

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

	/** 添加声音 */
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
				Intent intent = new Intent(ReportActivity.this,
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
		CaseRecord caseRecord = new CaseRecord();
		if (isNew) {
			caseRecord.caseID = newProblemNo;
		} else {
			caseRecord.caseID = mDraftCase.ProblemNo;
		}
		caseRecord.path = soundPath;
		// caseRecord.processStage = "上报";
		recordList.add(caseRecord);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.d("EventActivity", " --- onConfigurationChanged() ---");
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
				// imageView_sound.setImageResource(R.drawable.record_image);
				addSoundToMySoundsPool(mCurrentAudioPath);
			}
			break;
		// 手动选点结果处理
		case REQUEST_PICK_BJ:// 部件
			if (resultCode == RESULT_CANCELED
					|| resultCode == RESULT_FIRST_USER) {
				return;
			}
			if (resultCode == RESULT_OK) {
				// 使用部件位置
				IBean ibean = (IBean) data.getSerializableExtra("currentIBean");
				if (GeometryUtils.isPoint(ibean.shape)) {
					mCasePnt = (Point) ibean.shape;

				} else {
					mCasePnt = (Point) data.getSerializableExtra("centerPnt");
				}
				mThingId = ibean.id;

			} else if (resultCode == PickActivity.RESULT_USE_MAP_CENTER) {
				// 使用地图中心只有位置信息
				mCasePnt = (Point) data.getSerializableExtra("centerPnt");
				mThingId = null;

			}
			markLocation(mCasePnt);
			mMapView.centerAt(mCasePnt, false);
			mMapView.zoomToScale(mCasePnt, mMapView.getScale() / 4);
			// 根据位置获取地址
			new GetAddressAsyncTask().execute(mCasePnt);

			break;
		case REQUEST_PICK_SJ:// 事件
			if (resultCode == RESULT_OK) {
				mCasePnt = (Point) data.getSerializableExtra("centerPnt");
				mLayerId = null;
				mThingId = null;
				markLocation(mCasePnt);
				mMapView.centerAt(mCasePnt, false);
				mMapView.zoomToScale(mCasePnt, mMapView.getScale() / 4);
				// 根据位置获取地址
				new GetAddressAsyncTask().execute(mCasePnt);
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isFinished) {
			return false;
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.report_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.report:

			if (mCasePnt == null) {
				// 位置为空时不允许上报
				buildNullPntReportDialog(mContext).show();
			} else if ("0".equalsIgnoreCase(((Condition) conditionSpinner
					.getSelectedItem()).code)
					|| "".equalsIgnoreCase(et_caseDetails.getText().toString())
					|| "".equalsIgnoreCase(et_caseAddr.getText().toString())
					|| imgList == null
					|| imgList.size() == 0
					|| (!smallClassSpinner.isShown())) {
				buildNullReportDialog(mContext).show();
			} else {
				buildReportDialog(mContext).show();
				// report(mContext, getConfigedCase());
			}
			return true;
		case R.id.save:
			// 测试1：
			// saveJPG(mContext, getConfigedCase());
			// 测试2：读取文件最后修改时间（图片拍摄时间）
			// String date = ImageUtils.getDateTime(imgList.get(0).path);
			// Toast.makeText(mContext, "图片拍摄时间：" + date, Toast.LENGTH_SHORT)
			// .show();
			if (mCasePnt == null) {
				// 位置为空时提示
				buildNullPntDialog(mContext).show();
			} else {

				save(mContext, getConfigedCase());
			}
			return true;
		case R.id.cancel:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/** 上传case */
	private void report(Context context, Case inCase) {
		inCase.postTime = Utils.formatDate(Calendar.getInstance().getTime(),
				"yyyy-MM-dd HH:mm:ss");
		new ReportAsyncTask().execute(AppApplication.mUser, inCase);
	}

	/** 将case保存到本地 */
	private void save(Context context, Case inCase) {
		inCase.postTime = Utils.formatDate(Calendar.getInstance().getTime(),
				"yyyy-MM-dd HH:mm:ss");
		long saveValue = saveCaseToLocal(inCase);
		if (saveValue == 1) {
			Toast.makeText(context, "已保存在本地", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "问题保存失败", Toast.LENGTH_SHORT).show();
		}
		setResult(RESULT_OK);
		finish();
		overridePendingTransition(0, R.anim.right_out);
	}

	/** 测试：保存图片 */
	private void saveJPG(Context context, Case inCase) {
		for (int i = 0; i < inCase.imageList.size(); i++) {
			CaseImage mImage = inCase.imageList.get(i);
			byte[] bytes = ImageUtils.getCompressedPrintBitmapBytes(
					mImage.path, ImageUtils.WITH_HEIGHTS[0][0],
					ImageUtils.WITH_HEIGHTS[0][1], 100);// 图片质量压缩到200K以内

			// LogUtils.d("ImageUtils", "保存图片：" + bytes.length / 1024);
			// Bitmap bmp = ImageUtils.getBitmapThumbnail(mImage.path,
			// ImageUtils.WITH_HEIGHTS[0][0],
			// ImageUtils.WITH_HEIGHTS[0][1]);

			// Bitmap bmp = ImageUtils.getScaledBitmap(mImage.path,
			// ImageUtils.WITH_HEIGHTS[0][0],
			// ImageUtils.WITH_HEIGHTS[0][1]);
			// byte[] bytes = ImageUtils.bitmapToBytes(bmp, 60);
			Bitmap bmp = ImageUtils.bytesToBitmap(bytes);
			LogUtils.d("ImageUtils", "保存图片：" + bytes.length / 1024);
			ImageUtils.saveBitmapToExternalStorage(mContext, bmp,
					Utils.getSDCardPath() + "/test/", "打印文字_" + i);
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.spin_condition:// 立项条件

			// String largeClass = largeClassSpinner.isShown() ? ((Category)
			// largeClassSpinner
			// .getSelectedItem()).value : "";
			// String smallClass = smallClassSpinner.isShown() ? ((Category)
			// smallClassSpinner
			// .getSelectedItem()).value : "";
			// String subClass = subClassSpinner.isShown() ? ((Category)
			// subClassSpinner
			// .getSelectedItem()).value : "";
			// requestConditionUsingVolley(largeClass, smallClass, subClass);
			break;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		String largeClass = "";
		String smallClass = "";
		String subClass = "";
		switch (v.getId()) {
		case R.id.spin_condition:// 立项条件

			// largeClass = largeClassSpinner.isShown() ? ((Category)
			// largeClassSpinner
			// .getSelectedItem()).value : "";
			// smallClass = smallClassSpinner.isShown() ? ((Category)
			// smallClassSpinner
			// .getSelectedItem()).value : "";
			// subClass = subClassSpinner.isShown() ? ((Category)
			// subClassSpinner
			// .getSelectedItem()).value : "";
			// requestConditionUsingVolley(largeClass, smallClass, subClass);
			break;
		case R.id.btn_take_photo:
			showBottomMenu();
			break;
		case R.id.btn_record_sound:
			Intent i = new Intent(this, AudioRecordActivity.class);
			startActivityForResult(i, ACTION_RECORD_SOUND);
			break;
		case R.id.btn_takephoto:
			hideBottomMenu();
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
			break;
		case R.id.btn_pickphoto:
			pickImage();
			hideBottomMenu();
			break;
		case R.id.btn_cancel:
			hideBottomMenu();
			break;
		// 使用GPS位置---
		case R.id.btnCurrentPnt:
			if (!smallClassSpinner.isShown()) {
				Toast.makeText(mContext, "请先选择小类", Toast.LENGTH_SHORT).show();
				break;
			}
			Point point = mLocationDisplayManager.getPoint();
			if (point == null) {
				buildCurrentPntNullDialog(this).show();
			} else {
				buildUseCurrentPntDialog(this, point).show();
			}
			break;
		case R.id.buttonLocateMe:
			if (!smallClassSpinner.isShown()) {
				Toast.makeText(mContext, "请先选择小类", Toast.LENGTH_SHORT).show();
				break;
			}
			Point p = mLocationDisplayManager.getPoint();
			if (p == null) {
				buildCurrentPntNullDialog(this).show();
			} else {
				mMapView.centerAt(p,true);
				buildUseCurrentPntDialog(this, p).show();
			}
			break;

		// 手动获取位置---
		case R.id.btnSelectPntManual:
			if (!smallClassSpinner.isShown()) {
				Toast.makeText(mContext, "请先选择小类", Toast.LENGTH_SHORT).show();
				break;
			}
//			if (R.id.radio0 == mTypeRadioGroup.getCheckedRadioButtonId()) {
//				largeClass = largeClassSpinner.isShown() ? ((Category) largeClassSpinner
//						.getSelectedItem()).value : "";
//				smallClass = smallClassSpinner.isShown() ? ((Category) smallClassSpinner
//						.getSelectedItem()).value : "";
//				subClass = subClassSpinner.isShown() ? ((Category) subClassSpinner
//						.getSelectedItem()).value : "";
//				requestMapServerUsingVolley(largeClass, smallClass, subClass);
//			} else {
//				Intent intent = new Intent(mContext, PickActivity.class);
//				intent.putExtra("caseTypeDesc", Case.TYPE_DESCS[1]);
//				startActivityForResult(intent, REQUEST_PICK_SJ);
//			}

			Intent intent = new Intent(mContext, PickActivity.class);
			intent.putExtra("caseTypeDesc", Case.TYPE_DESCS[1]);
			startActivityForResult(intent, REQUEST_PICK_SJ);

			break;
		default:
			break;
		}

	}


	private Button mButtonLocateMe = null;

	private boolean isFollowingLocation = false;

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
		if(b){
			isFollowingLocation = true;
		}else {
			isFollowingLocation = false;
		}
	}
	/**
	 * 获得当前位置 isFirstLocate 首次定位，默认缩放到一个比例 isMarkTrace 临时做法，首次start巡查时避开speed的约束
	 */
	private Point getCurrentLocation(boolean isFirstLocate) {



		Point point = mLocationDisplayManager.getPoint();

		if (point == null) {
			Toast.makeText(ReportActivity.this, "暂无法获取当前位置", Toast.LENGTH_SHORT)
					.show();
		} else {
			mMapView.zoomToScale(point, mMapView.getScale() / 8);
			return point;
		}
		return null;

	}

	/** 在地图上标记点位 */
	private void markLocation(Point mapPoint) {
		casePntGlayer.removeAll();
		PictureMarkerSymbol pictureSymbol = new PictureMarkerSymbol(
				getResources().getDrawable(R.drawable.ic_img_location_red));
		pictureSymbol.setOffsetY(15);
		Graphic graphic = new Graphic(mapPoint, pictureSymbol);
		casePntGlayer.addGraphic(graphic);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		switch (parent.getId()) {
		case R.id.spin_category_1:
			Category selectedCategory = largeClassList.get(pos);
			smallClassList = AppApplication.myDataBase
					.getCaseSmallClassByType(selectedCategory.value);
			if (smallClassList.size() == 0) {
				smallClassSpinner.setVisibility(View.GONE);
				subClassSpinner.setVisibility(View.GONE);
				resetConditionSpinner();
				// break;
			} else {
				smallClassSpinner.setVisibility(View.VISIBLE);
			}

			if (adapter_smallClass == null) {
				adapter_smallClass = new ArrayAdapter<Category>(this,
						android.R.layout.simple_spinner_item, smallClassList);
				adapter_smallClass
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			} else {
				adapter_smallClass.clear();
				adapter_smallClass.addAll(smallClassList);
				adapter_smallClass.notifyDataSetChanged();
			}
			smallClassSpinner.setAdapter(adapter_smallClass);// 若Spinner处于隐藏状态将不触发ItemSelected事件
			break;
		case R.id.spin_category_2:
			selectedCategory = smallClassList.get(pos);
			// 根据大小子类获取立案条件
			if (pos == 0) {
				resetConditionSpinner();
			} else {
				String largeClass = ((Category) largeClassSpinner
						.getSelectedItem()).value;
				String smallClass = selectedCategory.value;
				requestConditionUsingVolley(largeClass, smallClass, "", "");
			}
			subClassList = AppApplication.myDataBase
					.getCaseSubClassByType(selectedCategory.value);
			if (subClassList.size() > 0) {
				subClassSpinner.setVisibility(View.VISIBLE);

			} else {
				subClassSpinner.setVisibility(View.GONE);
				// break;
			}
			if (adapter_subClass == null) {
				adapter_subClass = new ArrayAdapter<Category>(this,
						android.R.layout.simple_spinner_item, subClassList);
				adapter_subClass
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			} else {
				adapter_subClass.clear();
				adapter_subClass.addAll(subClassList);
				adapter_subClass.notifyDataSetChanged();
			}
			subClassSpinner.setAdapter(adapter_subClass);// 若Spinner处于隐藏状态将不触发ItemSelected事件
			break;
		case R.id.spin_category_3:

			if (subClassList.size() > 0) {
				selectedCategory = subClassList.get(pos);
			}

			break;
		case R.id.spin_condition:// 立案条件
			// 自动填充问题描述：监督员号、区域描述、创建时间、位置信息、小类、立案条件
			// String userName = mDefaultPrefs.getString(LoginActivity.USERNAME,
			// "");
			// String createTime = Utils.formatDate(Calendar.getInstance()
			// .getTime(), "HH:mm");
			// String caseClassIIDesc = smallClassSpinner.isShown() ?
			// ((Category) smallClassSpinner
			// .getSelectedItem()).description : "";
			// String caseCondition = ((Condition) conditionSpinner
			// .getSelectedItem()).cname;// 中文
			// String caseDetails = userName + "，" + "未知区域，" + createTime + "，"
			// + "未知位置，" + caseClassIIDesc + "，" + caseCondition;
			// et_caseDetails.setText(caseDetails);
			break;

		default:
			break;
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	/** 收集用户编辑信息含点位 */
	private Case getConfigedCase() {
		Case uploadCase = null;

		if (isDraft) {
			// 保存后重新打开进入编辑状态---
			uploadCase = mDraftCase;

		} else {
			// 新建状态下---
			uploadCase = new Case();
			// uploadCase.addr = "苏州工业园区地理测绘大楼";
			uploadCase.gridID = AppApplication.mUser.gridID;
			uploadCase.ProcessStageDesc = "上报";
			uploadCase.ProcessResult = Case.REPORT_STATUS_CODE[0];// 待保存
			uploadCase.ProblemNo = newProblemNo;
		}
		// 收集点位信息
		if (mCasePnt == null) {

		} else {
			uploadCase.X = mCasePnt.getX();
			uploadCase.Y = mCasePnt.getY();
		}
		uploadCase.createTime = createTime;
		uploadCase.InspectorID = AppApplication.mUser.userID;
		// 结合mTypeRadioGroup的选中状态来收集数据
		switch (mTypeRadioGroup.getCheckedRadioButtonId()) {
		case R.id.radio0:
			uploadCase.CaseTypeDesc = "部件";
			break;
		case R.id.radio1:
			uploadCase.CaseTypeDesc = "事件";
			break;
		default:
			break;
		}
		// 上报类型
		if (swtEmergency.isChecked()) {
			uploadCase.emergency = EMERGENCYS[1][1];
		} else {
			uploadCase.emergency = EMERGENCYS[0][1];
		}
		// 立案条件
		uploadCase.caseCondition = ((Condition) conditionSpinner
				.getSelectedItem()).code;// 非中文
		// 结合spinner的可见性来收集用户选择的数据
		uploadCase.CaseClassIDesc = largeClassSpinner.isShown() ? ((Category) largeClassSpinner
				.getSelectedItem()).description : "";
		uploadCase.CaseClassIIDesc = smallClassSpinner.isShown() ? ((Category) smallClassSpinner
				.getSelectedItem()).description : "";
		uploadCase.CaseClassIIIDesc = subClassSpinner.isShown() ? ((Category) subClassSpinner
				.getSelectedItem()).description : "";
		// 大小子类名称编码,v1.1
		uploadCase.CaseClassI = largeClassSpinner.isShown() ? ((Category) largeClassSpinner
				.getSelectedItem()).value : "";
		uploadCase.CaseClassII = smallClassSpinner.isShown() ? ((Category) smallClassSpinner
				.getSelectedItem()).value : "";
		uploadCase.CaseClassIII = subClassSpinner.isShown() ? ((Category) subClassSpinner
				.getSelectedItem()).value : "";

		uploadCase.ReportCaseDesc = et_caseDetails.getText().toString();
		uploadCase.ReportAddress = et_caseAddr.getText().toString();

		// if (imgList != null && imgList.size() > 0) {
		// uploadCase.imageList = imgList;
		// }
		//
		// if (recordList != null && recordList.size() > 0) {
		// uploadCase.recordList = recordList;
		// }
		if (imgList != null) {
			uploadCase.imageList = imgList;
		}
		if (recordList != null) {
			uploadCase.recordList = recordList;
		}
		if (mLayerId != null) {
			uploadCase.layerId = mLayerId;
		}
		if (mThingId != null) {
			uploadCase.thingId = mThingId;
		}

		return uploadCase;
	}

	/**
	 * 是否使用用户当前位置对话框
	 * 
	 * @param context
	 * @param point
	 */
	private Dialog buildUseCurrentPntDialog(Context context, final Point point) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage("确定使用您的当前位置？");
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						markLocation(point);
						mCasePnt = point;
						mMapView.centerAt(point, true);
						mLayerId = null;
						mThingId = null;
						// 根据位置获取地址
						new GetAddressAsyncTask().execute(mCasePnt);

					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		return builder.create();
	}

	/** GPS位置点为null提示稍后重试 */
	private Dialog buildCurrentPntNullDialog(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage("暂无法获取您的位置，请稍后重试！");
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		return builder.create();
	}

	/** 先结束选点状态再保存 */
	private Dialog buildSelectingPntDialog(final Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage("选点状态中无法保存，请结束后重试！");
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		return builder.create();
	}

	/** 位置点为null不可以提交 */
	private Dialog buildNullPntReportDialog(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage("位置点为null无法上报，请确定位置后重试！");
		builder.setPositiveButton(R.string.alert_dialog_ok, null);
		return builder.create();
	}

	/** 必填项为空不能提交 */
	private Dialog buildNullReportDialog(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage("必填（选）项为空无法上报，请完善数据后重试！");
		builder.setPositiveButton(R.string.alert_dialog_ok, null);
		return builder.create();
	}

	/** 确认上报 */
	private Dialog buildReportDialog(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage(R.string.dialog_msg_report);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						report(mContext, getConfigedCase());
					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		return builder.create();
	}

	/** 位置点为null可以保存但不可以提交 */
	private Dialog buildNullPntDialog(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage("位置点为null，确定要保存？");
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						save(mContext, getConfigedCase());
					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		return builder.create();
	}

	private long saveCaseToLocal(Case mCase) {
		if (AppApplication.myDataBase == null) {
			// 初始化数据库
			AppApplication.myDataBase = new MyDatabase(this);
		}
		long returnValue = AppApplication.myDataBase.insertCase(mCase);
		return returnValue;
	}

	/** 上报后更新 为已上报 */
	private Case updateCaseResult(Case inCase) {

		inCase.ProcessResult = Case.REPORT_STATUS_CODE[1];// 已上报

		// if (inCase.imageList != null && inCase.imageList.size() > 0) {
		// for (int i = 0; i < inCase.imageList.size(); i++) {
		// CaseImage mCaseImage = inCase.imageList.get(i);
		// if (mCaseImage.processStage.equals(inCase.ProcessStageDesc)) {
		// mCaseImage.processResult = inCase.ProcessResult1;
		// inCase.imageList.set(i, mCaseImage);
		// break;
		// }
		// }
		// }
		//
		// if (inCase.recordList != null && inCase.recordList.size() > 0) {
		// for (int i = 0; i < inCase.recordList.size(); i++) {
		// CaseRecord mCaseRecord = inCase.recordList.get(i);
		// if (mCaseRecord.processStage.equals(inCase.ProcessStageDesc)) {
		// mCaseRecord.processResult = inCase.ProcessResult1;
		// inCase.recordList.set(i, mCaseRecord);
		// break;
		// }
		// }
		// }

		return inCase;
	}

	//
	// private void requestProblemNoUsingVolley() {
	// // Volley
	// String URL = HttpQuery.serviceMap.get("GetProblemNo");
	// int end = URL.lastIndexOf("=");
	// String querySQL = URL.substring(end + 1);
	// querySQL = querySQL.replaceAll("#UserID#",
	// InspectorApplication.mUser.userID);
	// try {
	// querySQL = URLEncoder.encode(querySQL, "UTF-8");
	// } catch (UnsupportedEncodingException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// URL = URL.substring(0, end + 1);
	//
	// String url = URL + querySQL;
	// Log.d("ReportActivity", "requestProblemNo:" + url);
	// mQueue.add(new JsonArrayRequest(url, new Listener<JSONArray>() {
	// @Override
	// public void onResponse(JSONArray array) {
	// if (array == null) {
	// Toast.makeText(ReportActivity.this, "没有获到ProblemNO",
	// Toast.LENGTH_SHORT).show();
	// return;
	// }
	// try {
	// int length = array.length();
	// if (length > 0) {
	// JSONObject obj = (JSONObject) array.get(0);
	// newProblemNo = obj.getString("problemno");
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }
	// }, new ErrorListener() {
	//
	// @Override
	// public void onErrorResponse(VolleyError arg0) {
	//
	// }
	//
	// }));
	//
	// mQueue.start();
	// }

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
		LogUtils.d("ReportActivity", "url:" + url);
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
						// 记录一下图层id，用于上报
						mLayerId = layer.id;
						layer.url = obj.optString("Url");
						layer.subIds = obj.optString("subIds");

						// String maplayerURL = obj.getString("serverUrl");

						Intent intent = new Intent(mContext, PickActivity.class);
						intent.putExtra("caseTypeDesc", Case.TYPE_DESCS[0]);
						intent.putExtra("layer", layer);
						startActivityForResult(intent, REQUEST_PICK_BJ);
						// overridePendingTransition(R.anim.left_in,
						// R.anim.stable);
					}
				} catch (JSONException e) {
//					e.printStackTrace();
					Toast.makeText(mContext, "服务访问出错", Toast.LENGTH_SHORT)
							.show();
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

	/** 根据大小子类获取立项条件 ,subClass统一为""，根据conditionCode确定选中位置 */
	private void requestConditionUsingVolley(String largeClass,
			String smallClass, String subClass, final String conditionCode) {

		// Volley
		String URL = "";
		if (AppApplication.isPubllic) {
			URL = HttpQuery.serviceMap.get("GetCaseCondition");
		} else {
			URL = HttpQuery.serviceMap.get("GetCaseCondition_vpn");
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
		LogUtils.d("ReportActivity", "立项条件:" + url);
		mQueue.add(new JsonArrayRequest(url, new Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray array) {
				Utils.hideWaitingDialog();

				if (array == null || array.length() == 0
						|| array.toString().contains("error")) {
					Toast.makeText(mContext, R.string.no_data,
							Toast.LENGTH_SHORT).show();
					resetConditionSpinner();
					return;
				}
				try {
					int length = array.length();
					if (length > 0) {
						ArrayList<Condition> list = new ArrayList<Condition>();
						for (int i = 0; i < length; i++) {
							Condition condition = new Condition();
							JSONObject obj = (JSONObject) array.get(i);
							condition.code = obj.optString("code", "");
							condition.cname = obj.optString("cname", "");
							condition.xorder = obj.optInt("xorder", 0);
							list.add(condition);
						}
						// if (isNew) {// 新建时选取前监听
						// conditionSpinner
						// .setOnItemSelectedListener(ReportActivity.this);
						// }
						if (length == 1) {// 直接选上
							conditionList.clear();
							conditionList.addAll(list);
							adapter_condition.notifyDataSetChanged();
							conditionSpinner.setSelection(0, true);
						} else {
							conditionList.clear();
							conditionList.add(new Condition("0",
									Condition.CONDITIONS_DEF[0], 0));// 请选择立案条件
							conditionList.addAll(list);
							Collections.sort(conditionList);
							adapter_condition.notifyDataSetChanged();

							int position = getPositionFromConditionList(
									conditionList, conditionCode);
							conditionSpinner.setSelection(position, true);

						}
						// if (!isNew) {// 保存或提交后打开时先选取再监听
						// conditionSpinner
						// .setOnItemSelectedListener(ReportActivity.this);
						// }

					}
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(mContext, R.string.no_data,
							Toast.LENGTH_SHORT).show();
					resetConditionSpinner();

				}
			}

		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {

				Toast.makeText(mContext, R.string.no_data, Toast.LENGTH_SHORT)
						.show();
				conditionList.clear();
				conditionList.add(new Condition("0",
						Condition.CONDITIONS_DEF[0], 0));
				adapter_condition.notifyDataSetChanged();
			}

		}));

//		mQueue.start();
//		mQueue.start()会导致com.android.volley.NoConnection error, java.io.InterruptedIOException
	}

	/** 重置立案条件 */
	private void resetConditionSpinner() {
		conditionList.clear();
		conditionList.add(new Condition("0", Condition.CONDITIONS_DEF[0], 0));// 请选择条件
		adapter_condition.notifyDataSetChanged();
	}

	/** 从立案条件中获取conditionCode的位置 ,默认0 */
	private int getPositionFromConditionList(
			ArrayList<Condition> conditionList, String conditionCode) {
		if (conditionList == null || conditionList.size() == 0) {
			return -1;
		} else if (conditionCode == null || "".equalsIgnoreCase(conditionCode)) {
			return 0;
		} else {
			for (int i = 0; i < conditionList.size(); i++) {
				if (conditionCode.equalsIgnoreCase(conditionList.get(i).code)) {
					return i;
				}
			}
		}
		return 0;

	}




	/** 通过点位获取对应地址信息 */
	class GetAddressAsyncTask extends AsyncTask<Object, Void, Object> {
		// 自动填充问题描述：监督员号、区域描述、创建时间、位置信息、小类、立案条件
		String userName = mDefaultPrefs.getString(LoginActivity.USERNAME, "");
		String createTime = Utils.formatDate(Calendar.getInstance().getTime(),
				"HH:mm");
		// String caseClassIIDesc = smallClassSpinner.isShown() ? ((Category)
		// smallClassSpinner
		// .getSelectedItem()).description : "选择小类";
		String caseClassIIDesc = ((Category) smallClassSpinner
				.getSelectedItem()).description;

		String caseCondition = ((Condition) conditionSpinner.getSelectedItem()).cname;// 中文
		String caseDetails = userName + "，" + "未知区域，" + createTime + "，"
				+ "未知位置，" + "号路灯杆，" + caseClassIIDesc + "，" + caseCondition;
//		String caseDetails = userName + "，" + "，" + createTime + "，"
//				+ "，" + "号路灯杆，" + caseClassIIDesc + "，" + caseCondition;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			Utils.showWaitingDialog(mContext);
			et_caseDetails.setText(caseDetails);
		}

		@Override
		protected Object doInBackground(Object... params) {
			Point p = (Point) params[0];
			return HttpQuery.getAddressByPoint(p);
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Utils.hideWaitingDialog();
			if (result instanceof Exception) {
				Exception ex = (Exception) result;
				Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (result instanceof String) {
				String[] regionPosition = result.toString().split(",");

				// 自动填充位置信息
				et_caseAddr.setText(regionPosition[1]);
				// 自动填充问题描述：监督员号、区域描述、创建时间、位置信息、小类、立案条件
				String caseDetails = userName + "，" + regionPosition[0] + "，"
						+ createTime + "，" + regionPosition[1] + "，" + "号路灯杆，"
						+ caseClassIIDesc + "，" + caseCondition;
				et_caseDetails.setText(caseDetails);
			}
		}
	}

	class ReportAsyncTask extends AsyncTask<Object, Void, Object> {

		private Case currentCase;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Utils.showWaitingDialog(ReportActivity.this);
		}

		@Override
		protected Object doInBackground(Object... args) {
			User mUser = (User) args[0];
			Case mCase = (Case) args[1];
			currentCase = mCase;

			return HttpQuery.doReport(mUser, mCase);
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Utils.hideWaitingDialog();

			if (result instanceof Exception) { // ??????
				Exception e = (Exception) result;
				Toast.makeText(ReportActivity.this, e.getMessage() + "，保存到本地",
						Toast.LENGTH_SHORT).show();
			} else if (result instanceof Error) {
				Error e = (Error) result;
				Toast.makeText(ReportActivity.this, e.getMessage() + "，保存到本地",
						Toast.LENGTH_SHORT).show();
			} else {
				String returnStr = (String) result;
				Toast.makeText(ReportActivity.this, returnStr,
						Toast.LENGTH_SHORT).show();
				currentCase = updateCaseResult(currentCase);
			}

			// long saveValue = saveCaseToLocal(currentCase);
			// if (saveValue == 0) {
			// Toast.makeText(ReportActivity.this, "问题保存失败",
			// Toast.LENGTH_SHORT).show();
			// }
			long saveValue = saveCaseToLocal(currentCase);
			if (saveValue == 1) {
				Toast.makeText(mContext, "已保存至本地", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "问题保存失败", Toast.LENGTH_SHORT).show();
			}

			setResult(RESULT_OK);
			finish();
			overridePendingTransition(0, R.anim.right_out);

		}

	}

}