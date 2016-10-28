package com.geone.inspect.threepart_ts.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.geone.inspect.threepart_ts.activity.AppApplication;
import com.geone.inspect.threepart_ts.activity.HSHCCLActivity;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.activity.ReportActivity;
import com.geone.inspect.threepart_ts.adapter.CaseListAdapter;
import com.geone.inspect.threepart_ts.adapter.HSHCCLCaseListAdapter;
import com.geone.inspect.threepart_ts.adapter.TabPagerAdapter;
import com.geone.inspect.threepart_ts.bean.Accept;
import com.geone.inspect.threepart_ts.bean.Case;
import com.geone.inspect.threepart_ts.bean.Category;
import com.geone.inspect.threepart_ts.http.HttpQuery;
import com.geone.inspect.threepart_ts.sql.MyDatabase;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.MapEnum;
import com.geone.inspect.threepart_ts.util.Utils;
import java.util.ArrayList;
import java.util.Calendar;

//https://github.com/chrisbanes/Android-PullToRefresh   ----very very old library

/**
 * A fragment that launches other parts of the demo application.
 */

/** 问题上报、核实、核查、处理案件列表 */
public class CaseReportFragment extends Fragment implements
		OnItemSelectedListener, OnClickListener, OnScrollListener,
		OnItemClickListener {

	/** 问题上报 */
	public static final int REQUEST_CASE_SB = 1;
	/** 待核实 */
	public static final int REQUEST_CASE_HS = 2;
	/** 待核查 */
	public static final int REQUEST_CASE_HC = 3;
	/** 待处理 */
	public static final int REQUEST_CASE_CL = 4;
	/** 我签收 */
	public static final int REQUEST_CASE_QS = 5;
	private int mTabIndex;
	private String mPageTitle = "";
	private ImageButton btn_new;
	/** 用于显示已保存和已上报事件的list */
	private ListView mListView;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private ListView mActualListView;
	private LinearLayout mListLoadingLayout;

	private Spinner caseTypeSpinner;
	private Spinner casePeriodSpinner;
	/** casePeriodSpinner选择项，默认空 */
	private String mSelectPeriod = "";
	/** caseTypeSpinner选择项（不带编号），默认空 */
	private String mSelectType = "";

	private Button btn_chooseDate;
	private TextView tv_divider;
	private LinearLayout layout_sb;
	private TextView tv_empty;

	private CaseListAdapter mCaseListAdapter;
	private HSHCCLCaseListAdapter mHSHCCaseListAdapter;

	private ArrayList<Category> largeClassList;

	private ArrayList<Case> caseList;
	private static String gridID;

	private Context mContext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Bundle bundle_arguments = getArguments();
		mContext = this.getActivity();
		mTabIndex = bundle_arguments.getInt("tab_index");
		mPageTitle = bundle_arguments.getString("pageTitle");
		View rootView = initViews(inflater, container, mTabIndex);
		gridID = AppApplication.mUser.gridID;

		return rootView;
	}

	private View initViews(LayoutInflater inflater, ViewGroup container,
			int tab_index) {
		View rootView = inflater.inflate(R.layout.fragment_report_new, container,
				false);
		btn_new = (ImageButton) rootView.findViewById(R.id.btn_new);
		tv_divider = (TextView) rootView.findViewById(R.id.textView1);
		layout_sb = (LinearLayout) rootView.findViewById(R.id.layout_sb);

		mListView = (ListView) rootView
				.findViewById(R.id.caseListView);
		tv_empty = (TextView) rootView.findViewById(R.id.empty);
		mListView.setEmptyView(tv_empty);


		mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				String label = DateUtils.formatDateTime(getActivity(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				//refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("上次更新: " + label);

				// 下载内容
				updateHSHCCLCaseListView();
			}
		});


		mActualListView = mListView;
		mActualListView.setOnItemClickListener(this);
		if (TabPagerAdapter.PAGETITLES_XC[0].equalsIgnoreCase(mPageTitle)) {// 问题上报
			initReportView(inflater, rootView);
		} else {// 核实核查处理签收
			initHSHCCLView(inflater, rootView, mPageTitle, mTabIndex);
		}
		return rootView;
	}

	/**
	 * 初始化核实核查处理签收界面
	 *
	 * @param inflater
	 * @param rootView
	 * @param tabIndex
	 * @param pageTitle
	 */
	private void initHSHCCLView(LayoutInflater inflater, View rootView,
			final String pageTitle, int tabIndex) {

		mHSHCCaseListAdapter = new HSHCCLCaseListAdapter(inflater);
		mActualListView.setAdapter(mHSHCCaseListAdapter);
		tv_empty.setText(MapEnum.getEmpty(pageTitle));
		updateHSHCCLCaseListView();
//		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
//			@Override
//			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
//
//			}
//		});
	}

	/**
	 * 初始化问题上报界面
	 *
	 * @param inflater
	 * @param rootView
	 */
	private void initReportView(LayoutInflater inflater, View rootView) {
		layout_sb.setVisibility(View.VISIBLE);
		btn_new.setOnClickListener(this);
		// 初始化事件列表————————————
		mCaseListAdapter = new CaseListAdapter(inflater);
		// 获得网格里的事件列表,gridID可以认为是“分区编号”
		mActualListView.setAdapter(mCaseListAdapter);
		mActualListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						Case item = (Case) parent.getItemAtPosition(position);

						int reportCode = Integer.parseInt(item.ProcessResult);
						switch (reportCode) {
						case 0:
							buildDeleteCaseDialog(mContext, item).show();
							break;
						default:
							Toast.makeText(mContext,
									R.string.tip_refuse_delete,
									Toast.LENGTH_SHORT).show();
							break;
						}
						return true;
					}
				});
		// 初始化两个spinner————————————
		// 获得所有大类，并包装成“所有类型”
		largeClassList = AppApplication.myDataBase
				.getCaseLargeClassWithoutDefByType(null);
		int size = largeClassList.size();
		ArrayList<String> typeArrayList = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			Category mCategory = largeClassList.get(i);
			typeArrayList.add(mCategory.value + " - " + mCategory.description);
		}

		// 添加所有类型选项
		typeArrayList.add(0, "所有类型");
		// case type Spinner
		caseTypeSpinner = (Spinner) rootView.findViewById(R.id.spin_type);
		ArrayAdapter<String> adapter_type = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item,
				typeArrayList);
		adapter_type
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		caseTypeSpinner.setAdapter(adapter_type);// 注册监听前不触发选择事件，默认选择第0项
		caseTypeSpinner.setOnItemSelectedListener(this);

		// case period Spinner
		casePeriodSpinner = (Spinner) rootView.findViewById(R.id.spin_period);

		ArrayAdapter<CharSequence> adapter_period = ArrayAdapter
				.createFromResource(getActivity(), R.array.case_period_array,
						android.R.layout.simple_spinner_item);
		adapter_period
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		casePeriodSpinner.setAdapter(adapter_period);
//		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
//			@Override
//			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
//				String label = DateUtils.formatDateTime(getActivity(),
//						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
//								| DateUtils.FORMAT_SHOW_DATE
//								| DateUtils.FORMAT_ABBREV_ALL);
//
//				// Update the LastUpdatedLabel
//				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(
//						"上次更新: " + label);
//				if (caseList == null || caseList.size() == 0) {
//					mListView.onRefreshComplete();
//				} else {
//					new CheckAcceptAsyncTask().execute(caseList);
//				}
//				// new GetHSHCCLCaseAsyncTask().execute(
//				// InspectorApplication.mUser.gridID, Case.WFTYPES[1]);
//			}
//
//		});

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				String label = DateUtils.formatDateTime(getActivity(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				//refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("上次更新: " + label);
				if (caseList == null || caseList.size() == 0) {
                    mSwipeRefreshLayout.setRefreshing(false);
					//mSwipeRefreshLayout.onRefreshComplete();
				} else {
					new CheckAcceptAsyncTask().execute(caseList);
				}
			}
		});
		casePeriodSpinner.setOnItemSelectedListener(this);
		casePeriodSpinner.setSelection(2, true);// 默认选择第2项

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		switch (parent.getId()) {
		case R.id.spin_type:
			if (pos == 0) {// 查看所有类型
				// caseList = InspectorApplication.myDataBase
				// .getCaseListInGrid(InspectorApplication.mUser.gridID);
				mSelectType = "";

			} else {
				mSelectType = largeClassList.get(pos - 1).description;
			}
			LogUtils.d("CaseReportFragment", "类型:" + mSelectType);
			// mCaseListAdapter = new CaseListAdapter(
			// LayoutInflater.from(getActivity()));
			// // 获得网格里的事件列表
			// mCaseListAdapter.eventList = caseList;
			// mActualListView.setAdapter(mCaseListAdapter);
			break;
		case R.id.spin_period:
			switch (pos) {
			case 0:// 所有时段
				mSelectPeriod = "";
				break;
			case 1:// 本日
					// selectPeriod = "2014-11-26 00:00:00";
				mSelectPeriod = Utils.formatDate(Calendar.getInstance()
						.getTime(), "yyyy-MM-dd");
				LogUtils.d("CaseReportFragment", "本日:" + mSelectPeriod);
				break;
			case 2:// 本周
					// selectPeriod = "2014-11-24 00:00:00";
				Calendar c = Calendar.getInstance();
				c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// 默认时，每周第一天为星期日，需要更改一下
				mSelectPeriod = Utils.formatDate(c.getTime(), "yyyy-MM-dd");
				LogUtils.d("CaseReportFragment", "本周:" + mSelectPeriod);

				break;
			case 3:// 本月
					// selectPeriod = "2014-11-00 00:00:00";
				mSelectPeriod = Utils.formatDate(Calendar.getInstance()
						.getTime(), "yyyy-MM");
				LogUtils.d("CaseReportFragment", "本月:" + mSelectPeriod);
				break;
			default:
				break;
			}
			break;

		default:
			break;
		}
		updateSBCaseListView();

		// caseList = InspectorApplication.myDataBase
		// .getCaseListByTypeAndPeriod(new String[] { mSelectType,
		// mSelectPeriod });
		// if (caseList == null || caseList.size() == 0) {
		// mCaseListAdapter.eventList.clear();
		// mCaseListAdapter.notifyDataSetChanged();
		// } else {
		// new CheckAcceptAsyncTask().execute(caseList);
		// }

		// mCaseListAdapter.eventList = caseList;
		// mCaseListAdapter.notifyDataSetChanged();

	}



	/** 检测案件是否未受理并刷新列表 */
	class CheckAcceptAsyncTask extends AsyncTask<ArrayList<Case>, Void, Object> {
		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			// Utils.showWaitingDialog(mContext);
		}

		@Override
		protected Object doInBackground(ArrayList<Case>... params) {
			ArrayList<Case> caseList = params[0];
			String problemNos = "";
			for (int i = 0; i < caseList.size(); i++) {
				problemNos += caseList.get(i).ProblemNo;
				if (i != caseList.size() - 1) {
					problemNos += ",";
				}
			}
			LogUtils.d("CaseReportFragment", "problemNos:" + problemNos);
			if ("".equalsIgnoreCase(problemNos)) {
				return new Exception("暂无数据");
			}
			return HttpQuery.checkAccept(problemNos);
		}

		// @Override
		// protected Object doInBackground(String... params) {
		//
		// return HttpQuery.checkAccept(params[0]);
		// }

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			//mListView.onRefreshComplete();
            mSwipeRefreshLayout.setRefreshing(false);
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			/** 组合数据 */
			if (result instanceof ArrayList) {
				ArrayList<Accept> acceptList = (ArrayList<Accept>) result;
				if (acceptList == null || acceptList.size() == 0) {
					return;
				}
				// for (int i = 0; i < caseList.size(); i++) {
				// Case tempCase = caseList.get(i);
				// for (int j = 0; j < acceptList.size(); j++) {
				// Accept tempAccept = acceptList.get(j);
				// if (tempCase.ProblemNo
				// .equalsIgnoreCase(tempAccept.ProblemNo)) {
				// // tempCase.isAccept = "0";
				// // caseList.set(i, tempCase);
				// if ("0".equalsIgnoreCase(tempAccept.IsAccept)) {// 不受理
				// tempCase.isAccept = tempAccept.IsAccept;
				// caseList.set(i, tempCase);
				// }
				//
				// }
				// }
				// }
				for (int i = 0; i < acceptList.size(); i++) {
					Accept tempAccept = acceptList.get(i);
					if ("0".equalsIgnoreCase(tempAccept.IsAccept)) {// 不受理
						for (int j = 0; j < caseList.size(); j++) {
							Case tempCase = caseList.get(j);
							if (tempCase.ProblemNo == tempAccept.ProblemNo) {
								tempCase.isAccept = tempAccept.IsAccept;
								caseList.set(j, tempCase);
							}
						}
					}
				}
				mCaseListAdapter.eventList = caseList;
				mCaseListAdapter.notifyDataSetChanged();
			}
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onClick(View v) {
		// showDatePickerDialog();
		switch (v.getId()) {
		case R.id.btn_new:
			if (Utils.isNetworkAvailable(mContext)) {
				new GetProblemNoAsyncTask()
						.execute(AppApplication.mUser.userID);
			} else {
				Toast.makeText(mContext, R.string.no_network,
						Toast.LENGTH_SHORT).show();
			}
			break;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CASE_SB:
			if (resultCode == FragmentActivity.RESULT_OK) {
				// refresh ListView
				// caseList = InspectorApplication.myDataBase
				// .getCaseListInGrid(InspectorApplication.mUser.gridID);

				// caseList = InspectorApplication.myDataBase
				// .getCaseListByUser(InspectorApplication.mUser.userID);
				// new CheckAcceptAsyncTask().execute(caseList);
				// 刷新列表
				updateSBCaseListView();

			}
			break;
		case REQUEST_CASE_HS:// 核实
			if (resultCode == HSHCCLActivity.RESULT_CODE_UPDATELIST) {
				updateHSHCCLCaseListView();
			}

			break;
		case REQUEST_CASE_HC:// 核查

			if (resultCode == HSHCCLActivity.RESULT_CODE_UPDATELIST) {
				updateHSHCCLCaseListView();
			}

			break;
		case REQUEST_CASE_CL:// 处理

			if (resultCode == HSHCCLActivity.RESULT_CODE_UPDATELIST) {
				updateHSHCCLCaseListView();
			}
			break;
		case REQUEST_CASE_QS:// 签收

			if (resultCode == HSHCCLActivity.RESULT_CODE_UPDATELIST) {
				updateHSHCCLCaseListView();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	/** 刷新问题上报Case列表 */
	@SuppressWarnings("unchecked")
	private void updateSBCaseListView() {
		caseList = AppApplication.myDataBase
				.getCaseListByTypeAndPeriod(new String[] { mSelectType,
						mSelectPeriod });
		if (caseList == null || caseList.size() == 0) {
			mCaseListAdapter.eventList.clear();
			mCaseListAdapter.notifyDataSetChanged();
		} else {
			new CheckAcceptAsyncTask().execute(caseList);
		}
	}

	/** 更新核实核查处理收录列表 */
	public void updateHSHCCLCaseListView() {
		// 注意获取待处理列表的请求参数与核实核查不同
		new GetHSHCCLCaseAsyncTask().execute(AppApplication.mUser.gridID,
				AppApplication.mUser.userID, mPageTitle);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Case sbhshcclCase = (Case) parent.getItemAtPosition(position);
		if (sbhshcclCase == null) {
			return;
		}
		switch (mTabIndex) {
		case 0:
			Intent intent = new Intent(getActivity(), ReportActivity.class);
			if ("0".equalsIgnoreCase(sbhshcclCase.ProcessResult)) {// 已保存
				intent.putExtra("isDraft", true);
				intent.putExtra("case", sbhshcclCase);
				intent.putExtra("problemNo", sbhshcclCase.ProblemNo);
				startActivityForResult(intent, REQUEST_CASE_SB);
				// getActivity().overridePendingTransition(R.anim.left_in,
				// R.anim.stable);
			} else if ("1".equalsIgnoreCase(sbhshcclCase.ProcessResult)) {// 已上报
				intent.putExtra("isFinished", true);
				intent.putExtra("case", sbhshcclCase);
				startActivity(intent);
				// getActivity().overridePendingTransition(R.anim.left_in,
				// R.anim.stable);
			}
			break;
		case 1:
			// 核实、处理
			// Case hsclCase = (Case) parent.getItemAtPosition(position);
			// 检查任务是否已经被别人处理
			new CheckHaveDoneAsyncTask().execute(sbhshcclCase);

			break;
		case 2:
			// Case hcCase = (Case) parent.getItemAtPosition(position);
			// 检查任务是否已经被别人处理
			new CheckHaveDoneAsyncTask().execute(sbhshcclCase);
			break;

		default:
			break;
		}

	}

	private Dialog buildDeleteCaseDialog(Context context, final Case item) {
		Builder builder = new Builder(context);
		builder.setTitle(R.string.hint);
		builder.setMessage(R.string.msg_delete);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (AppApplication.myDataBase == null) {
							// 初始化数据库
							AppApplication.myDataBase = new MyDatabase(
									mContext);
						}
						AppApplication.myDataBase.deleteCase(item);// 删除相关记录
						// 刷新列表
						updateSBCaseListView();

					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});

		return builder.create();

	}

	// 跳转到待核实、待核查、待处理界面
	private void goHSHCCLActivity(String pageTitle, Case inCase) {
		if ("0".equalsIgnoreCase(inCase.IsRead)) { // 未读
			new SubmitHaveReadAsyncTask().execute(inCase);
		}
		Intent intent = new Intent(getActivity(), HSHCCLActivity.class);
		intent.putExtra("pageTitle", pageTitle);
		intent.putExtra("case", inCase);
		startActivityForResult(intent, MapEnum.getRequestCode(pageTitle));
		getActivity().overridePendingTransition(R.anim.left_in, R.anim.stable);
	}

	/** 获取核实、核查、处理列表 */
	class GetHSHCCLCaseAsyncTask extends AsyncTask<String, Void, Object> {

		private String pageTitle;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Utils.showWaitingDialog(getActivity());
		}

		@Override
		protected Object doInBackground(String... args) {

			pageTitle = args[2];
			return HttpQuery.getHSHCCLCaseList(args[0], args[1], args[2]);
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Utils.hideWaitingDialog();
            mSwipeRefreshLayout.setRefreshing(false);
            //mListView.onRefreshComplete();
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(getActivity(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
				tv_empty.setText(e.getMessage());
				return;
			}

			if (result instanceof Error) {
				Error e = (Error) result;
				Toast.makeText(getActivity(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
				tv_empty.setText(e.getMessage());
				return;

			} else if (result instanceof ArrayList) {
				ArrayList<Case> caseList = (ArrayList<Case>) result;
				// 如果存在本地记录，则组合本地记录和服务器端数据
				if (caseList == null || caseList.size() == 0) {
				} else {
					for (int i = 0; i < caseList.size(); i++) {
						Case hshcclcase = caseList.get(i);
						Case localCase = AppApplication.myDataBase
								.getHSHCCLCase(hshcclcase.CaseID,
										hshcclcase.wftype);
						if (localCase != null) {
							hshcclcase.isLocalExist = true;
							hshcclcase.DealComment = localCase.DealComment;
							hshcclcase.Advice = localCase.Advice;
							hshcclcase.imageList = localCase.imageList;
							hshcclcase.recordList = localCase.recordList;
							caseList.set(i, hshcclcase);
						}
					}

				}
				mHSHCCaseListAdapter.eventList.clear();
				mHSHCCaseListAdapter.eventList.addAll(caseList);
				// List<Case> mFailedCaseList = getFaliedHSHCCaseList(wftype);
				// if (mFailedCaseList != null && mFailedCaseList.size() > 0) {
				// mHSHCCaseListAdapter.eventList.addAll(mFailedCaseList);
				// }
				mHSHCCaseListAdapter.notifyDataSetChanged();

			}

		}
	}

	/** 提交已读通知 */
	class SubmitHaveReadAsyncTask extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Object doInBackground(Object... args) {
			Case mCase = (Case) args[0];
			return HttpQuery.submitHaveRead(mCase);
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(getActivity(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
				return;
			} else if (result instanceof String) {
				String returnStr = (String) result;
				// Toast.makeText(HSHCActivity.this, returnStr,
				// Toast.LENGTH_SHORT)
				// .show();
				//
			}
		}

	}

	/** 获取案件编号 */
	class GetProblemNoAsyncTask extends AsyncTask<String, Void, Object> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Utils.showWaitingDialog(mContext);
		}

		@Override
		protected Object doInBackground(String... args) {
			String userId = args[0];
			return HttpQuery.getProblemNo(userId);

		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Utils.hideWaitingDialog();
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
			} else if (result instanceof String) {
				String newProblemNo = (String) result;
				Intent intent = new Intent(getActivity(), ReportActivity.class);
				intent.putExtra("newProblemNo", newProblemNo);
				startActivityForResult(intent, REQUEST_CASE_SB);
				// getActivity().overridePendingTransition(R.anim.left_in,
				// R.anim.stable);
			}
		}
	}

	/** 提交任务已处理回执 RS=1:未处理，可以提交；0：已处理，不能再提交 */
	class CheckHaveDoneAsyncTask extends AsyncTask<Object, Void, Object> {
		Case hshcclCase = new Case();

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Object doInBackground(Object... args) {
			hshcclCase = (Case) args[0];
			return HttpQuery.checkHaveDone(hshcclCase);
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
					// 案件未上报过可以上报
					goHSHCCLActivity(mPageTitle, hshcclCase);
				} else {
					Toast.makeText(getActivity(), R.string.tip_have_done,
							Toast.LENGTH_SHORT).show();
					// 若案件已被别人处理则删除本地记录
					AppApplication.myDataBase
							.deleteHSHCCLCase(hshcclCase);
					// 刷新列表
					updateHSHCCLCaseListView();
					return;
				}
			}

		}

	}

}
