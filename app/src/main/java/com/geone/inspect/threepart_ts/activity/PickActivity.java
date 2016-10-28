package com.geone.inspect.threepart_ts.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.LocationDisplayManager.AutoPanMode;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledLayer;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.adapter.IPagerAdapter;
import com.geone.inspect.threepart_ts.bean.Case;
import com.geone.inspect.threepart_ts.bean.IBean;
import com.geone.inspect.threepart_ts.bean.Layer;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.tianditu.TianDiTuLayer;
import com.geone.inspect.threepart_ts.tianditu.TianDiTuLayerTypes;
import com.geone.inspect.threepart_ts.util.GeometryUtils;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.SymbolUtils;
import com.geone.inspect.threepart_ts.util.Utils;
import com.viewpagerindicator.LinePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//import android.app.Activity;

public class PickActivity extends FragmentActivity implements OnClickListener {
	private MapView mMapView;
	private LocationDisplayManager mLocationDisplayManager;
	private Handler postHandler;
	private static final int UPDATE_USER_LOCATION = 0;
	/** 使用气泡位置（地图中心） */
	public static final int RESULT_USE_MAP_CENTER = -2;
	// private static final String BASE_MAPLAYER_URL =
	// "http://58.210.9.131/SIPSD/rest/services/PUBLIC/Public_PADMAP/MapServer";
	private static final int[] DETAIL_TYPE = {
			R.drawable.detailbackground_blue, R.drawable.detailbackground_red,
			R.drawable.detailbackground_green };
	/** 使用气泡（地图中心）位置 */
	private Button btnUseMapCenter;
	/** 手动获取位置图标 */
	private ImageView imgLocationGreen;
	/** 手动获取的位置点所在GraphicsLayer */
	private GraphicsLayer casePntGlayer;
	/** i查询结果所在GraphicsLayer */
	private GraphicsLayer infoGLayer;
	// 专题图层---
	/** 根据大小子类获取的layer */
	private Layer mLayer;
	private Context mContext;
	/** 类型：部件、事件 */
	private String caseTypeDesc;
	// 部件事件定位----
	/** 部件事件位置 */
	private Point mCasePnt;
	/** 部件id，用于显示详细信息 */
	private String mThingId;
	/** 用于区分手动选点还是案件定位，true是案件定位 */
	private boolean isLocateCase;
	// Volley
	private RequestQueue mQueue;
	private ArrayList<IBean> iBeanList = new ArrayList<IBean>(0);
	/** viewpager中当前IBean */
	private IBean currentIBean;
	private int currentIItemPos;
	private Point currentIPoint;
	private String currentLayerIDs;
	private int currentIBeanGraphicID;
	private IPagerAdapter adapter;

	// popup
	private View iBeanPopView;
	private ViewPager mPager;
	private LinePageIndicator mIndicator;
	private LinearLayout layout_i_tools;
	private ImageButton btn_i_ok;
	private ImageButton ib_i_cancel;
	private TextView tv_count;
	private LinearLayout layout_detailboard;

	// 动画
	private static Animation top_in;
	private static Animation top_out;
	private static Animation bottom_in;
	private static Animation bottom_out;
	private static Animation left_in;
	private static Animation right_out;

	// private IPagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;
		postHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case UPDATE_USER_LOCATION:
					// 获取用户位置，有坐标时缩放到当前位置，null时缩放到当前地图中心
					Point point = mLocationDisplayManager.getPoint();
					if (point == null || point.getX() == 0) {
						Toast.makeText(mContext, "暂无法获取当前位置",
								Toast.LENGTH_SHORT).show();
//						mMapView.zoomToScale(mMapView.getCenter(),
//								mMapView.getScale() / 4);
						//由于xml文件中设置了MapView的范围，滁州的范围目前不知道，先写死底图中心点坐标
//						Point center=new Point(514105.51461975684,3573679.758500106);
//						mMapView.zoomToScale(center,
//								mMapView.getScale() / 4);
//						mMapView.centerAt(new Point(119.030095667, 32.0293513673),true);
						mMapView.zoomToScale(new Point(119.030095667, 32.0293513673), mMapView.getScale() / Math.pow(2,14));
					} else {

//						mMapView.zoomToScale(point, mMapView.getScale() / 4);
						mMapView.zoomToScale(point, mMapView.getScale() / Math.pow(2,14));
					}
					break;
				default:
					break;
				}
			}
		};
		Intent intent = getIntent();
		caseTypeDesc = intent.getStringExtra("caseTypeDesc");
		mLayer = (Layer) intent.getSerializableExtra("layer");// 事件时为null
		mCasePnt = (Point) intent.getSerializableExtra("casePnt");// 部件事件位置
		mThingId = intent.getStringExtra("thingId");// 部件id
		if (mCasePnt != null) {
			isLocateCase = true;
			setTitle(R.string.title_locate_case);
		}

		mQueue = Volley.newRequestQueue(this);
		initMapView();
		initView();

	}

	private void initView() {
		imgLocationGreen = (ImageView) findViewById(R.id.imgLocationGreen);
		if (isLocateCase) {
			imgLocationGreen.setVisibility(View.GONE);
		}
		layout_detailboard = (LinearLayout) findViewById(R.id.layout_detailboard);
		btnUseMapCenter = (Button) findViewById(R.id.btnUseMapCenter);
		btnUseMapCenter.setOnClickListener(this);

		initIPopView();
		initAnimations();

	}

	private void initAnimations() {
		top_in = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_in_from_top);
		top_out = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_out_to_top);
		bottom_in = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_in_from_bottom);
		bottom_out = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_out_to_bottom);
		left_in = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.left_in);
		right_out = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.right_out);
	}

	private void initMapView() {
		mMapView = (MapView) findViewById(R.id.mapView);
		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -7371458236288482586L;

			@Override
			public void onStatusChanged(Object source, STATUS status) {
				// Check if a layer is failed to be loaded due to security
				if (status == STATUS.INITIALIZED && source == mMapView) {
					mLocationDisplayManager = mMapView
							.getLocationDisplayManager();
					// mLocationDisplayManager =
					// ReportActivity.mLocationDisplayManager;

					mLocationDisplayManager.setAccuracyCircleOn(true);
					mLocationDisplayManager.setAutoPanMode(AutoPanMode.OFF);
					mLocationDisplayManager.start();
					if (isLocateCase) {// 案件定位

						if (Case.TYPE_DESCS[0].equalsIgnoreCase(caseTypeDesc)
								&& (!(mThingId == null || ""
										.equalsIgnoreCase(mThingId)))) {// 部件请求详细信息
							requestIUsingVolley(mCasePnt, mLayer.id);
						}
						mMapView.centerAt(mCasePnt, true);
						mMapView.zoomToScale(mCasePnt, mMapView.getScale() / 3);
						markLocation(mCasePnt);
					} else {
						// 手动选点
						Message msg = postHandler
								.obtainMessage(UPDATE_USER_LOCATION);
						postHandler.sendMessageDelayed(msg, 2000);
					}

				}
			}

		});
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5432072421333559701L;

			@Override
			public void onSingleTap(float x, float y) {
				Point point = mMapView.toMapPoint(x, y);
				mMapView.centerAt(point, true);
			}
		});
		// 基础地图
		initBaseMap();
		// 部件时初始化专题图层，事件不加载专题图层
		if (Case.TYPE_DESCS[0].equalsIgnoreCase(caseTypeDesc)) {
//			ArcGISDynamicMapServiceLayer dlayerZT = new ArcGISDynamicMapServiceLayer(
//					mLayer.url, Utils.parseString2IntArray(mLayer.subIds),
//					Utils.getUserCredentials());
			//去掉获取图层时，使用的token
			//服务，或者显示图层的确定方法有问题，所需要的图层不能正常显示，有待协商解决
//			ArcGISDynamicMapServiceLayer dlayerZT = new ArcGISDynamicMapServiceLayer(
//					mLayer.url);
			//后台服务已经改掉,加入了父节点图层的ID,可以直接应用显示
			ArcGISDynamicMapServiceLayer dlayerZT = new ArcGISDynamicMapServiceLayer(
					mLayer.url, Utils.parseString2IntArray(mLayer.subIds));

//			ArcGISLayerInfo[] layers = dlayerZT.getLayers();
//			layers[2].setVisible(false);
//			layers[138].setVisible(false);
//			layers[274].setVisible(false);
//			layers[410].setVisible(false);

			LogUtils.d("PickActivity", "部件URL：" + mLayer.url + "/"
					+ mLayer.subIds);

			mMapView.addLayer(dlayerZT);

			infoGLayer = new GraphicsLayer();
			infoGLayer.setSelectionColor(Color.MAGENTA);// 设置选中图形的颜色
			mMapView.addLayer(infoGLayer);

		}
		// 问题上报点所在图层
		casePntGlayer = new GraphicsLayer();
		mMapView.addLayer(casePntGlayer);

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
//			mMapView.addLayer(tiledLayer);
			com.esri.android.map.Layer mapLayer = new TianDiTuLayer(TianDiTuLayerTypes.TIANDITU_VECTOR_2000);
			mMapView.addLayer(mapLayer);
			com.esri.android.map.Layer annotationLayer = new TianDiTuLayer(
					TianDiTuLayerTypes.TIANDITU_VECTOR_ANNOTATION_CHINESE_2000);
			mMapView.addLayer(annotationLayer);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		ArcGISRuntime.setClientId("kduw6jknnq8Ayn0d");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pick, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isLocateCase) {
			menu.getItem(0).setVisible(false);// 定位案件时隐藏“确定”菜单

		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		// overridePendingTransition(0, R.anim.right_out);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.menuOk:

			if (Case.TYPE_DESCS[0].equalsIgnoreCase(caseTypeDesc)) {
				// String layerIds = "BEC2B77B-0D41-4935-BDD9-C000B9D2468C";
				Point centerPnt = mMapView.getCenter();
				requestIUsingVolley(centerPnt, mLayer.id);
			} else {
				// 事件
				Point centerPnt = mMapView.getCenter();
				Intent intent = new Intent(this, ReportActivity.class);
				intent.putExtra("centerPnt", centerPnt);
				setResult(RESULT_OK, intent);
				finish();

			}

			break;
		case R.id.menuCancel:
			if (Case.TYPE_DESCS[0].equalsIgnoreCase(caseTypeDesc)
					&& (!isLocateCase)) {
				imgLocationGreen.setVisibility(View.VISIBLE);
				infoGLayer.removeAll();
				// casePntGlayer.removeAll();
				hideDetailboard();

			} else {
				// 事件,案件定位
				onBackPressed();
			}

			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	// 方法——————————————
	/** 屏幕下方用于展示查询结果的pop */
	private void initIPopView() {
		iBeanPopView = getLayoutInflater().inflate(R.layout.details_i, null);
		layout_i_tools = (LinearLayout) iBeanPopView
				.findViewById(R.id.layout_i_tools);
		if (isLocateCase) {// 案件定位时不显示
			layout_i_tools.setVisibility(View.GONE);
		}
		// layout_i_tools.setOnClickListener(this);
		btn_i_ok = (ImageButton) iBeanPopView.findViewById(R.id.btn_i_ok);
		// ib_i_cancel = (ImageButton)
		// iBeanPopView.findViewById(R.id.ib_i_cancel);
		tv_count = (TextView) iBeanPopView.findViewById(R.id.tv_count);

		btn_i_ok.setOnClickListener(this);
		// ib_i_cancel.setOnClickListener(this);

		mPager = (ViewPager) iBeanPopView.findViewById(R.id.pager);
		mIndicator = (LinePageIndicator) iBeanPopView
				.findViewById(R.id.indicator);

		mIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// infoGLayer.clearSelection();
				// int[] ids = infoGLayer.getGraphicIDs();
				// Arrays.sort(ids);//
				// sort的原因是addGraphic后，getGraphicIDs居然是倒序的。所以调整一下顺序，与业务顺序对应
				// currentIBeanGraphicID = ids[arg0];
				// infoGLayer.setSelectedGraphics(
				// new int[] { currentIBeanGraphicID }, true);
				// currentIBean = iBeanList.get(arg0);
				// currentIItemPos = arg0;
				highLightGraphic(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// arg0 ==1的时候表示正在滑动，arg0==2的时候表示滑动完毕了，arg0==0的时候表示什么都没做
			}
		});

		// init adapter　
		// adapter = new IPagerAdapter(_context, getSupportFragmentManager(),
		// iBeanList);
	}

	/** 向服务器发起查询请求，参数：点击位置，打开的专题图图层 */
	private void requestIUsingVolley(final Point p, String layerIds) {
		// Volley
		String bjURL = "";
		if (AppApplication.isPubllic) {
			bjURL = HttpQuery.serviceMap.get("GetBJInfoByPoint");
		} else {
			bjURL = HttpQuery.serviceMap.get("GetBJInfoByPoint_vpn");
		}
		LogUtils.d("PickActivity", "bjURL:" + bjURL);
		int end = bjURL.lastIndexOf("?");
		String querySQL = bjURL.substring(end + 1);
		querySQL = querySQL.replaceAll("#x#", Double.toString(p.getX()));
		querySQL = querySQL.replaceAll("#y#", Double.toString(p.getY()));
		querySQL = querySQL.replaceAll("#layerIds#", layerIds);
		// try {
		// querySQL = URLEncoder.encode(querySQL, "UTF-8");
		// } catch (UnsupportedEncodingException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		bjURL = bjURL.substring(0, end + 1);
		String url = bjURL + querySQL;
		LogUtils.d("PickActivity", "url: " + url);
		if (!isLocateCase) {
			Utils.showWaitingDialog(this);
		}

		mQueue.add(new JsonArrayRequest(url, new Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray array) {
				Utils.hideWaitingDialog();
				if (array == null || array.toString().contains("error")) {
					Toast.makeText(mContext, "暂无数据", Toast.LENGTH_SHORT).show();
					if (!isLocateCase) {
						btnUseMapCenter.setVisibility(View.VISIBLE);
					}
					return;
				}
				try {
					int length = array.length();
					if (length > 0) {
						iBeanList = new ArrayList<IBean>(length);
						for (int i = 0; i < length; i++) {
							JSONObject obj = (JSONObject) array.get(i);
							IBean mIBean = Utils.parseJSONToBean(obj);
							if (mIBean != null) {
								if (isLocateCase) {// 案件定位时过滤其他案件
									if (mThingId.equalsIgnoreCase(mIBean.id)) {
										iBeanList.add(mIBean);
									}
								} else {
									iBeanList.add(mIBean);
								}
							}
						}
						if (iBeanList.size() == 0) {
							Toast.makeText(mContext, "暂无数据", Toast.LENGTH_SHORT)
									.show();
							return;
						}
						// 每次返回bean的顺序不固定，对其按照名称进行排序
						Collections.sort(iBeanList);
						adapter = new IPagerAdapter(mContext,
								getSupportFragmentManager(), iBeanList);
						mPager.setOffscreenPageLimit(iBeanList.size());
						mPager.setAdapter(adapter);
						mIndicator.setViewPager(mPager, currentIItemPos);

						if (iBeanList.size() <= 1) {
							mIndicator.setVisibility(View.INVISIBLE);
							tv_count.setVisibility(View.GONE);
						} else {
							mIndicator.setVisibility(View.VISIBLE);
							tv_count.setVisibility(View.VISIBLE);
							tv_count.setText("共" + iBeanList.size() + "个结果");
						}
						// 获得当前的IBean
						currentIBean = iBeanList.get(currentIItemPos);
						currentIPoint = p;

						updateILayer(iBeanList);// 绘制图形不加加亮
						highLightGraphic(0);// 高亮显示第0个
						showDetailboard(0, p, iBeanPopView);
						imgLocationGreen.setVisibility(View.GONE);
						btnUseMapCenter.setVisibility(View.GONE);

					} else {
						if (!isLocateCase) {
							btnUseMapCenter.setVisibility(View.VISIBLE);
						}
						Toast.makeText(mContext, "暂无数据", Toast.LENGTH_SHORT)
								.show();
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				Utils.hideWaitingDialog();
				try {
					arg0.printStackTrace();
					Toast.makeText(mContext, "i查询异常", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Toast.makeText(mContext, "i查询异常", Toast.LENGTH_SHORT)
							.show();
				}
			}

		}));

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

	private void showDetailboard(int detail_type, Point p, View v) {
		layout_detailboard.setBackgroundResource(DETAIL_TYPE[detail_type]);
		mMapView.centerAt(p, true);

		if (layout_detailboard.isShown()) {
			layout_detailboard.removeAllViews();
			layout_detailboard.addView(v);
			return;
		}

		layout_detailboard.startAnimation(bottom_in);
		layout_detailboard.setVisibility(View.VISIBLE);
		layout_detailboard.removeAllViews();
		layout_detailboard.addView(v);
	}

	private void hideDetailboard() {
		if (!layout_detailboard.isShown()) {
			return;
		}
		layout_detailboard.startAnimation(bottom_out);
		layout_detailboard.setVisibility(View.GONE);
	}

	/**
	 * 绘制图形不加加亮
	 * 
	 * @param inIBeanList
	 */
	private void updateILayer(ArrayList<IBean> inIBeanList) {
		Graphic g = null;
		// 点状符号，转化为面状符号

		// 线状符号
		SimpleLineSymbol slSymbol = SymbolUtils.getSimpleLineSymbol(Color.GRAY,
				2);

		// 面状符号
		SimpleFillSymbol sfSymbol = SymbolUtils.getSimpleFillSymbol(
				Color.DKGRAY, SimpleFillSymbol.STYLE.NULL, slSymbol);
		sfSymbol.setAlpha(50);

		infoGLayer.removeAll();

		for (int i = 0; i < inIBeanList.size(); i++) {
			IBean bean = inIBeanList.get(i);
			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.putAll(bean.showlistMap);

			if (Geometry.isPoint(bean.shape.getType().value())) {//点
				Point mPoint = (Point) bean.shape;
				g = new Graphic(Utils.getCircle(mPoint, 4), sfSymbol,
						attributes);
			} else if (Geometry.isArea(bean.shape.getType().value())) {// 面
				g = new Graphic(bean.shape, sfSymbol, attributes);
			} else {// 线

				g = new Graphic(bean.shape, slSymbol, attributes);
			}

			infoGLayer.addGraphic(g);

			// glInfo.addGraphic(new Graphic(bean.shape, mPictureMarkerSymbol,
			// attributes));
		}
		// infoGLayer.clearSelection();
		// int[] ids = infoGLayer.getGraphicIDs();
		// Arrays.sort(ids); //
		// sort的原因是addGraphic后，getGraphicIDs居然是倒序的。所以调整一下顺序，与业务顺序对应
		// currentIBeanGraphicID = ids[currentIItemPos];
		// infoGLayer.setSelectedGraphics(new int[] { currentIBeanGraphicID },
		// true);

	}

	/** 高亮显示图形 */
	private void highLightGraphic(int position) {
		int[] ids = infoGLayer.getGraphicIDs();
		Arrays.sort(ids); // sort的原因是addGraphic后，getGraphicIDs居然是倒序的。所以调整一下顺序，与业务顺序IBeanlist对应
		infoGLayer.clearSelection();
		infoGLayer.setSelectedGraphics(new int[] { ids[position] }, true);
		// showCallout(Utils.getGeometryCenter(iBeanList.get(position).shape),
		// mCalloutView);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.btn_i_ok:
			intent = new Intent(this, ReportActivity.class);
			intent.putExtra("currentIBean", currentIBean);
			if (!GeometryUtils.isPoint(currentIBean.shape)) {
				intent.putExtra("centerPnt", mMapView.getCenter());
			}

			setResult(RESULT_OK, intent);
			finish();
			overridePendingTransition(0, R.anim.right_out);
			break;
		case R.id.btnUseMapCenter:
			Point centerPnt = mMapView.getCenter();
			intent = new Intent(this, ReportActivity.class);
			intent.putExtra("centerPnt", centerPnt);
			setResult(RESULT_USE_MAP_CENTER, intent);
			finish();
			overridePendingTransition(0, R.anim.right_out);
			break;
		default:
			break;
		}

	}
}
