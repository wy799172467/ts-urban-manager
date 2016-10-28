package com.geone.inspect.threepart_ts.sql;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.geone.inspect.threepart_ts.activity.AppApplication;
import com.geone.inspect.threepart_ts.bean.Case;
import com.geone.inspect.threepart_ts.bean.CaseImage;
import com.geone.inspect.threepart_ts.bean.CaseRecord;
import com.geone.inspect.threepart_ts.bean.Category;
import com.geone.inspect.threepart_ts.bean.Layer;
import com.geone.inspect.threepart_ts.util.LogUtils;
import com.geone.inspect.threepart_ts.util.MapEnum;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class MyDatabase extends SQLiteAssetHelper {

	public static final int CASETYPE_BJ = 10;
	public static final int CASETYPE_SJ = 20;

	private static final String DATABASE_NAME = "LocalCase";
	private static final int DATABASE_VERSION = 3;
	private static final String TABLE_CASE_TYPE = "T_CaseType";
	// private static final String TABLE_CASE_LARGE_CLASS = "T_CaseLargeClass";
	// private static final String TABLE_CASE_SMALL_CLASS = "T_CaseSmallClass";
	// private static final String TABLE_CASE_SUB_CLASS = "T_CaseSubClass";
	/** TABLE_CATEGORYS={"T_CaseLargeClass","T_CaseSmallClass","T_CaseSubClass"} */
	public static final String[] TABLE_CATEGORYS = { "T_CaseLargeClass",
			"T_CaseSmallClass", "T_CaseSubClass" };
	private static final String TABLE_CASE = "T_Case";
	private static final String TABLE_IMAGE = "T_Image";
	private static final String TABLE_RECORD = "T_Record";
	private static final String TABLE_LAYER = "T_Layers";
	// 核实核查处理表----
	/** 核实核查处理案件,主键CaseID+wftype */
	private static final String TABLE_HSHCCL_CASE = "T_HSHCCLCase";
	/** 核实核查处理图片,主键_id */
	private static final String TABLE_HSHCCL_IMAGE = "T_HSHCCLImage";
	/** 核实核查处理声音,主键_id */
	private static final String TABLE_HSHCCL_RECORD = "T_HSHCCLRecord";

	public MyDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// call this method to force a database overwrite every time the version
		// number increments:
		// setForcedUpgrade();
	}

	public MyDatabase(Context context, String db_name) {
		super(context, db_name, null, DATABASE_VERSION);
		// call this method to force a database overwrite every time the version
		// number increments:
		// setForcedUpgrade();
	}

	/**
	 * @param isWithDef
	 *            ：是否带预选项，如“选择大类”
	 */
	private ArrayList<Category> getCategoryByParentCaseType(String categoryTag,
			String caseType, boolean isWithDef) {
		ArrayList<Category> categoryList = new ArrayList<Category>();
		ArrayList<Category> tempList = new ArrayList<Category>();

		SQLiteDatabase db = getWritableDatabase();
		String selection = "Value like ?";
		String[] selectionArgs = { caseType + "%" };
		if (caseType == null) {
			selection = null;
			selectionArgs = null;
		}

		Cursor c = db.query(MapEnum.getCategoryTable(categoryTag), null,
				selection, selectionArgs, null, null, null);
		LogUtils.d("MyDatabase", c.getColumnCount() + "");

		boolean hasValue = c.moveToFirst();
		if (hasValue) {
			int count = c.getCount();
			for (int i = 0; i < count; i++) {
				Category mCategory = new Category();
				mCategory.value = c.getString(c.getColumnIndex("Value"));
				mCategory.description = c.getString(c.getColumnIndex("Desp"));
				tempList.add(mCategory);
				c.moveToNext();
			}
			if (isWithDef) {
				// 添加预选项
				categoryList.add(0, MapEnum.getCategoryDef(categoryTag));
			}
			categoryList.addAll(tempList);
		}
		db.close();
		return categoryList;

	}

	private void insertCaseCategory(String tableName,
			ArrayList<Category> categoryList) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		for (int i = 0; i < categoryList.size(); i++) {
			Category mCategory = categoryList.get(i);
			ContentValues mContentValues = new ContentValues();
			mContentValues.put("Value", mCategory.value);
			mContentValues.put("Desp", mCategory.description);
			db.insert(tableName, null, mContentValues);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

	}

	/** 获取事件列表 */
	private ArrayList<Case> getCaseListWithParams(String selection,
			String[] selectionArgs) {
		ArrayList<Case> caseList = new ArrayList<Case>();
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TABLE_CASE, null, selection, selectionArgs, null,
				null, "CreateTime desc");
		if (c != null) {
			caseList = getCaseListByCursor(c);
			c.close();
		}
		db.close();
		return caseList;
	}

	// 启用状态：false
	// private ArrayList<Case> getCaseListWithParams(String selectionKey,
	// String selectionValue) {
	// ArrayList<Case> caseList = new ArrayList<Case>();
	// SQLiteDatabase db = getWritableDatabase();
	// String selection = selectionKey + " = ?";
	// String[] selectionArgs = { selectionValue };
	// Cursor c = db.query(TABLE_CASE, null, selection, selectionArgs, null,
	// null, "CreateTime desc");
	// caseList = getCaseListByCursor(c);
	// if (c != null) {
	// c.close();
	// }
	// db.close();
	// return caseList;
	// }

	/** 根据Cursor获取Case列表，获取Case的公共入口 */
	private ArrayList<Case> getCaseListByCursor(Cursor c) {
		ArrayList<Case> caseList = new ArrayList<Case>();
		if (c != null && c.moveToFirst()) {
			do {
				Case mCase = new Case();
				mCase.CaseID = c.getString(c.getColumnIndex("CaseID"));
				mCase.InspectorID = c
						.getString(c.getColumnIndex("InspectorID"));
				mCase.disposalID = c.getString(c.getColumnIndex("DisposalID"));
				mCase.AuditorID = c.getString(c.getColumnIndex("AuditorID"));
				mCase.CaseTypeDesc = c.getString(c.getColumnIndex("CaseType"));
				mCase.CaseClassIDesc = c.getString(c
						.getColumnIndex("CaseLargeClass"));
				mCase.CaseClassIIDesc = c.getString(c
						.getColumnIndex("CaseSmallClass"));
				mCase.CaseClassIIIDesc = c.getString(c
						.getColumnIndex("CaseSubClass"));
				// mCase.wftype = c.getString(c.getColumnIndex("wftype"));
				mCase.ProcessStageDesc = c.getString(c
						.getColumnIndex("ProcessStage"));
				mCase.ProcessResult = c.getString(c
						.getColumnIndex("ProcessResult"));
				mCase.createTime = c.getString(c.getColumnIndex("CreateTime"));
				mCase.postTime = c.getString(c.getColumnIndex("PostTime"));
				mCase.CurrentStageStart = c.getString(c
						.getColumnIndex("ReceiveTime"));
				mCase.ReportAddress = c.getString(c.getColumnIndex("Address"));
				mCase.ReportCaseDesc = c
						.getString(c.getColumnIndex("CaseDesc"));
				mCase.X = c.getDouble(c.getColumnIndex("X"));
				mCase.Y = c.getDouble(c.getColumnIndex("Y"));
				mCase.Shape = c.getString(c.getColumnIndex("Shape"));
				mCase.workFlowID = c.getString(c.getColumnIndex("WorkFlowID"));
				mCase.gridID = c.getString(c.getColumnIndex("GridID"));
				mCase.ProblemNo = c.getString(c.getColumnIndex("ProblemID"));
				// liyl 2015-4-8
				mCase.layerId = c.getString(c.getColumnIndex("LayerId"));
				mCase.thingId = c.getString(c.getColumnIndex("ThingId"));
				mCase.emergency = c.getString(c.getColumnIndex("Emergency"));
				mCase.caseCondition = c.getString(c
						.getColumnIndex("CaseCondition"));
				mCase.imageList = getCaseImage("caseID=?",
						new String[] { mCase.ProblemNo });

				mCase.recordList = getCaseRecord("caseID=?",
						new String[] { mCase.ProblemNo });
				caseList.add(mCase);
			} while (c.moveToNext());
			// c.close();
		}

		return caseList;
	}

	/** 根据类型和时段进行查询 */
	public ArrayList<Case> getCaseListByTypeAndPeriod(String[] selectionValues) {

		String[] selectionKeys = new String[] { "CaseLargeClass", "CreateTime",
				"InspectorID" };
		String selection = null;
		String[] selectionArgs = null;
		if (selectionValues[0].equals("") && selectionValues[1].equals("")) {
			selection = selectionKeys[2] + "=?";
			selectionArgs = new String[] { AppApplication.mUser.userID };

			// c = db.query(TABLE_CASE, null, null, null, null, null,
			// "CreateTime desc");
		} else if (!selectionValues[0].equals("")
				&& selectionValues[1].equals("")) {
			selection = selectionKeys[0] + "=? AND " + selectionKeys[2] + "=?";
			selectionArgs = new String[] { selectionValues[0],
					AppApplication.mUser.userID };
			// c = db.query(TABLE_CASE, null, selection, selectionArgs, null,
			// null, "CreateTime desc");
		} else if (selectionValues[0].equals("")
				&& !selectionValues[1].equals("")) {
			// selection = selectionKey1 + ">=" + selectionValues[1];
			selection = selectionKeys[1] + ">=? AND " + selectionKeys[2] + "=?";
			selectionArgs = new String[] { selectionValues[1],
					AppApplication.mUser.userID };
			// c = db.query(TABLE_CASE, null, selection, selectionArgs, null,
			// null, "CreateTime desc");
		} else {
			selection = selectionKeys[0] + "=? AND " + selectionKeys[1]
					+ ">=? AND " + selectionKeys[2] + "=?";
			selectionArgs = new String[] { selectionValues[0],
					selectionValues[1], AppApplication.mUser.userID };
			// c = db.query(TABLE_CASE, null, selection, selectionArgs, null,
			// null, "CreateTime desc");

		}
		return getCaseListWithParams(selection, selectionArgs);

	}

	// /** 启用状态:false */
	// public ArrayList<Case> getCaseListByStageAndStatus(String wftype,
	// String status) {
	// ArrayList<Case> caseList = new ArrayList<Case>();
	// SQLiteDatabase db = getWritableDatabase();
	//
	// String selection = "wftype=? AND Status=?";
	// String[] selectionArgs = { wftype, status };
	//
	// Cursor c = db.query(TABLE_CASE, null, selection, selectionArgs, null,
	// null, "CreateTime desc");
	// boolean hasValue = c.moveToFirst();
	// if (hasValue) {
	// int count = c.getCount();
	// caseList = new ArrayList<Case>(count);
	// for (int i = 0; i < count; i++) {
	// Case mCase = new Case();
	// mCase.CaseID = c.getString(c.getColumnIndex("CaseID"));
	// mCase.InspectorID = c
	// .getString(c.getColumnIndex("InspectorID"));
	// mCase.disposalID = c.getString(c.getColumnIndex("DisposalID"));
	// mCase.AuditorID = c.getString(c.getColumnIndex("AuditorID"));
	// mCase.CaseTypeDesc = c.getString(c.getColumnIndex("CaseType"));
	// mCase.CaseClassIDesc = c.getString(c
	// .getColumnIndex("CaseLargeClass"));
	// mCase.CaseClassIIDesc = c.getString(c
	// .getColumnIndex("CaseSmallClass"));
	// mCase.CaseClassIIIDesc = c.getString(c
	// .getColumnIndex("CaseSubClass"));
	// // mCase.wftype = c.getString(c.getColumnIndex("wftype"));
	// mCase.ProcessStageDesc = c.getString(c
	// .getColumnIndex("ProcessStage"));
	// mCase.ProcessResult1 = c.getString(c
	// .getColumnIndex("ProcessResult"));
	// mCase.createTime = c.getString(c.getColumnIndex("CreateTime"));
	// mCase.postTime = c.getString(c.getColumnIndex("PostTime"));
	// mCase.CurrentStageStart = c.getString(c
	// .getColumnIndex("ReceiveTime"));
	// mCase.ReportAddress = c.getString(c.getColumnIndex("Address"));
	// mCase.ReportCaseDesc = c
	// .getString(c.getColumnIndex("CaseDesc"));
	// mCase.X = c.getDouble(c.getColumnIndex("X"));
	// mCase.Y = c.getDouble(c.getColumnIndex("Y"));
	// mCase.Shape = c.getString(c.getColumnIndex("Shape"));
	// mCase.workFlowID = c.getString(c.getColumnIndex("WorkFlowID"));
	// mCase.gridID = c.getString(c.getColumnIndex("GridID"));
	// mCase.ProblemNo = c.getString(c.getColumnIndex("ProblemID"));
	//
	// mCase.imageList = getCaseImageOfProcessStage(mCase.CaseID,
	// mCase.ProcessStageDesc);
	//
	// mCase.recordList = getCaseRecordOfProcessStage(mCase.CaseID,
	// mCase.ProcessStageDesc);
	//
	// caseList.add(mCase);
	//
	// c.moveToNext();
	// }
	// }
	//
	// db.close();
	// return caseList;
	// }

	/**
	 * 获得大类
	 */
	public ArrayList<Category> getCaseLargeClassByType(String caseType) {
		return getCategoryByParentCaseType(Category.TAG_CATEGORYS[0], caseType,
				true);
	}

	/**
	 * 获得大类,不带预选项，如“选择大类”
	 */
	public ArrayList<Category> getCaseLargeClassWithoutDefByType(String caseType) {
		return getCategoryByParentCaseType(Category.TAG_CATEGORYS[0], caseType,
				false);
	}

	/**
	 * 获得小类
	 */
	public ArrayList<Category> getCaseSmallClassByType(String caseType) {
		return getCategoryByParentCaseType(Category.TAG_CATEGORYS[1], caseType,
				true);
	}

	/**
	 * 获得子类
	 */
	public ArrayList<Category> getCaseSubClassByType(String caseType) {
		return getCategoryByParentCaseType(Category.TAG_CATEGORYS[2], caseType,
				true);
	}

	/**
	 * 获得某用户的case列表
	 */
	// public ArrayList<Case> getCaseListByUser(String userID) {
	// return getCaseListWithParams("InspectorID=?", new String[] { userID });
	// }

	/**
	 * 启用状态：false获得网格中的case列表
	 */
	// public ArrayList<Case> getCaseListInGrid(String gridID) {
	// return getCaseListWithParams("GridID=?", new String[] { gridID });
	// }

	public void insertCaseTypeList(ArrayList<Category> list) {
		insertCaseCategory(TABLE_CASE_TYPE, list);
	}

	public void insertCaseLargeClassList(ArrayList<Category> list) {
		insertCaseCategory(TABLE_CATEGORYS[0], list);
	}

	public void insertCaseSmallClassList(ArrayList<Category> list) {
		insertCaseCategory(TABLE_CATEGORYS[1], list);
	}

	public void insertCaseSubClassList(ArrayList<Category> list) {
		insertCaseCategory(TABLE_CATEGORYS[2], list);
	}

	/** 通过CaseID+wftype获取声音列表 */
	public ArrayList<CaseRecord> getHSHCCLRecordList(String caseID,
			String wftype) {

		ArrayList<CaseRecord> recordList = new ArrayList<CaseRecord>();
		CaseRecord mCaseRecord = null;

		SQLiteDatabase db = getWritableDatabase();

		String selection = "caseID = ? and wftype = ? ";
		String[] selectionArgs = { caseID, wftype };

		Cursor c = db.query(TABLE_HSHCCL_RECORD, null, selection,
				selectionArgs, null, null, null);
		LogUtils.d("MyDatabase", c.getColumnCount() + "");
		boolean hasValue = c.moveToFirst();
		if (hasValue) {
			int count = c.getCount();
			for (int i = 0; i < count; i++) {
				mCaseRecord = new CaseRecord();
				mCaseRecord.caseID = c.getString(c.getColumnIndex("caseID"));
				// mCaseRecord.processStage = c.getString(c
				// .getColumnIndex("processStage"));
				mCaseRecord.path = c.getString(c.getColumnIndex("path"));
				recordList.add(mCaseRecord);
				c.moveToNext();
			}

		}

		db.close();

		return recordList;
	}

	/** 通过CaseID+wftype获取图片列表 */
	public ArrayList<CaseImage> getHSHCCLImageList(String caseID, String wftype) {
		ArrayList<CaseImage> imgList = new ArrayList<CaseImage>();
		CaseImage mCaseImage = null;

		SQLiteDatabase db = getWritableDatabase();

		String selection = "caseID = ? and wftype = ? ";
		String[] selectionArgs = { caseID, wftype };

		Cursor c = db.query(TABLE_HSHCCL_IMAGE, null, selection, selectionArgs,
				null, null, null);
		LogUtils.d("MyDatabase", c.getColumnCount() + "");
		boolean hasValue = c.moveToFirst();
		if (hasValue) {
			int count = c.getCount();
			for (int i = 0; i < count; i++) {
				mCaseImage = new CaseImage();
				mCaseImage.caseID = c.getString(c.getColumnIndex("caseID"));
				// mCaseImage.processStage = c.getString(c
				// .getColumnIndex("processStage"));
				mCaseImage.path = c.getString(c.getColumnIndex("path"));
				imgList.add(mCaseImage);
				c.moveToNext();
			}

		}

		db.close();

		return imgList;
	}

	/** 通过CaseID+wftype获取保存在本地的case信息,包括属性、图片和声音列表 ，不包括服务端数据 */
	public Case getHSHCCLCase(String caseID, String wftype) {
		Case hshcclCase = null;
		SQLiteDatabase db = getWritableDatabase();
		String selection = "caseID = ? and wftype = ? ";
		String[] selectionArgs = { caseID, wftype };
		Cursor c = db.query(TABLE_HSHCCL_CASE, null, selection, selectionArgs,
				null, null, null);
		boolean hasValue = c.moveToFirst();
		if (hasValue) {
			int count = c.getCount();
			for (int i = 0; i < count; i++) {
				hshcclCase = new Case();
				// hshcclCase.CaseID = c.getString(c.getColumnIndex("CaseID"));
				// hshcclCase.wftype = c.getString(c.getColumnIndex("wftype"));
				hshcclCase.DealComment = c.getString(c
						.getColumnIndex("DealComment"));
				hshcclCase.Advice = c.getString(c.getColumnIndex("Advice"));
				hshcclCase.imageList = getHSHCCLImageList(caseID, wftype);
				hshcclCase.recordList = getHSHCCLRecordList(caseID, wftype);
				c.moveToNext();
			}

		}
		db.close();
		return hshcclCase;
	}

	/** 删除本地记录成功返回1失败返回0 */
	public int deleteHSHCCLCase(Case inCase) {
		SQLiteDatabase db = getWritableDatabase();
		// 删除CaseID相关联的属性、图片和声音
		int returnValue1 = db.delete(TABLE_HSHCCL_CASE,
				"CaseID=? and wftype=?", new String[] { inCase.CaseID,
						inCase.wftype });
		int returnValue2 = db.delete(TABLE_HSHCCL_IMAGE,
				"caseID = ? and wftype = ?", new String[] { inCase.CaseID,
						inCase.wftype });
		int returnValue3 = db.delete(TABLE_HSHCCL_RECORD,
				"caseID = ? and wftype = ?", new String[] { inCase.CaseID,
						inCase.wftype });
		db.close();
		if (returnValue1 + returnValue2 + returnValue3 < 3) {
			return 0;
		} else {
			return 1;
		}
	}

	/** 删除上报阶段本地记录 */
	public void deleteCase(Case inCase) {
		SQLiteDatabase db = getWritableDatabase();
		// 删除ProblemNo相关联的属性、图片和声音
		db.delete(TABLE_CASE, "ProblemID=?", new String[] { inCase.ProblemNo });// 注意上报阶段CaseID为null
		db.delete(TABLE_IMAGE, "caseID=?", new String[] { inCase.ProblemNo });
		db.delete(TABLE_RECORD, "caseID=?", new String[] { inCase.ProblemNo });
		db.close();

	}

	/**
	 * 新增一条核实核查处理记录，失败返回-1，成功返回1；注意严格区分大小写,注意与前期insertCase较大不同，v1.3
	 */
	public long insertHSHCCLCase(Case inCase) {
		long returnValue1 = 0;// 新增记录成功失败标记,默认未操作0，成功1，失败-1
		long returnValue2 = 0;
		long returnValue3 = 0;
		SQLiteDatabase db = getWritableDatabase();
		// // 删除CaseID相关联的属性、图片和声音
		// db.delete(TABLE_HSHCCL_CASE, "CaseID=?", new String[] { inCase.CaseID
		// });
		// db.delete(TABLE_HSHCCL_IMAGE, "caseID=?",
		// new String[] { inCase.CaseID });
		// db.delete(TABLE_HSHCCL_RECORD, "caseID=?",
		// new String[] { inCase.CaseID });
		// 删除CaseID相关联的属性、图片和声音
		db.delete(TABLE_HSHCCL_CASE, "CaseID=? and wftype=?", new String[] {
				inCase.CaseID, inCase.wftype });
		db.delete(TABLE_HSHCCL_IMAGE, "caseID = ? and wftype = ?",
				new String[] { inCase.CaseID, inCase.wftype });
		db.delete(TABLE_HSHCCL_RECORD, "caseID = ? and wftype = ?",
				new String[] { inCase.CaseID, inCase.wftype });

		ContentValues mContentValues = new ContentValues();
		mContentValues.put("CaseID", inCase.CaseID);
		mContentValues.put("wftype", inCase.wftype);
		mContentValues.put("DealComment", inCase.DealComment);// switch对应内容
		mContentValues.put("Advice", inCase.Advice);// 意见描述

		returnValue1 = db.insert(TABLE_HSHCCL_CASE, null, mContentValues);
		// int caseID = (int) returnValue;

		int length = 0;
		if (inCase.imageList != null && (length = inCase.imageList.size()) > 0) {
			for (int i = 0; i < length; i++) {
				CaseImage caseImage = inCase.imageList.get(i);
				ContentValues mContentValues_image = new ContentValues();

				mContentValues_image.put("caseID", inCase.CaseID);
				mContentValues_image.put("wftype", inCase.wftype);

				// mContentValues_image
				// .put("processStage", caseImage.processStage);
				mContentValues_image.put("path", caseImage.path);
				returnValue2 = db.insert(TABLE_HSHCCL_IMAGE, null,
						mContentValues_image);
			}
		}
		if (inCase.recordList != null
				&& (length = inCase.recordList.size()) > 0) {
			for (int i = 0; i < length; i++) {
				CaseRecord caseRecord = inCase.recordList.get(i);
				ContentValues mContentValues_record = new ContentValues();
				mContentValues_record.put("caseID", inCase.CaseID);
				mContentValues_record.put("wftype", inCase.wftype);

				// mContentValues_record.put("processStage",
				// caseRecord.processStage);
				mContentValues_record.put("path", caseRecord.path);
				returnValue3 = db.insert(TABLE_HSHCCL_RECORD, null,
						mContentValues_record);
			}
		}

		db.close();
		if (returnValue1 == -1 || returnValue2 == -1 || returnValue3 == -1) {
			return -1;
		} else {
			return 1;
		}

	}

	/**
	 * 新增或更新一条case记录，上报问题保存至本地,成功1，失败0,默认0
	 */
	public long insertCase(Case inCase) {
		long returnValue1 = 0;
		long returnValue2 = 0;
		long returnValue3 = 0;

		// 删除CaseID相关联的属性、图片和声音
		deleteCase(inCase);
		SQLiteDatabase db = getWritableDatabase();
		ContentValues contentValues = new ContentValues();

		// mContentValues.put("CaseID", mCase.caseID);//CaseID用作自增主键
		contentValues.put("InspectorID", inCase.InspectorID);
		contentValues.put("DisposalID", inCase.disposalID);
		contentValues.put("AuditorID", inCase.AuditorID);
		contentValues.put("CaseType", inCase.CaseTypeDesc);
		contentValues.put("CaseLargeClass", inCase.CaseClassIDesc);// 注意：此处存储的是名称而非编码
		contentValues.put("CaseSmallClass", inCase.CaseClassIIDesc);
		contentValues.put("CaseSubClass", inCase.CaseClassIIIDesc);
		// mContentValues.put("wftype", mCase.wftype);// 问题上报不需要插入该字段?
		contentValues.put("ProcessStage", inCase.ProcessStageDesc);
		contentValues.put("ProcessResult", inCase.ProcessResult);
		contentValues.put("CreateTime", inCase.createTime);
		contentValues.put("PostTime", inCase.postTime);
		contentValues.put("ReceiveTime", inCase.CurrentStageStart);
		contentValues.put("Address", inCase.ReportAddress);
		contentValues.put("CaseDesc", inCase.ReportCaseDesc);
		contentValues.put("X", inCase.X);
		contentValues.put("Y", inCase.Y);
		contentValues.put("Shape", inCase.Shape);
		contentValues.put("WorkFlowID", inCase.workFlowID);
		contentValues.put("GridID", inCase.gridID);
		contentValues.put("ProblemID", inCase.ProblemNo);
		// liyl 2015-4-8
		contentValues.put("LayerId", inCase.layerId);
		contentValues.put("ThingId", inCase.thingId);
		contentValues.put("Emergency", inCase.emergency);
		contentValues.put("CaseCondition", inCase.caseCondition);
		returnValue1 = db.insert(TABLE_CASE, null, contentValues);
		// int caseID = (int) returnValue;
		int length = 0;
		if (inCase.imageList != null && (length = inCase.imageList.size()) > 0) {
			for (int i = 0; i < length; i++) {
				CaseImage caseImage = inCase.imageList.get(i);
				ContentValues contentValues_image = new ContentValues();
				contentValues_image.put("caseID", caseImage.caseID);
				// contentValues_image.put("processStage",
				// caseImage.processStage);
				contentValues_image.put("path", caseImage.path);
				returnValue2 = db
						.insert(TABLE_IMAGE, null, contentValues_image);
			}
		}
		if (inCase.recordList != null
				&& (length = inCase.recordList.size()) > 0) {
			for (int i = 0; i < length; i++) {
				CaseRecord caseRecord = inCase.recordList.get(i);
				ContentValues contentValues_record = new ContentValues();
				contentValues_record.put("caseID", caseRecord.caseID);
				// contentValues_record.put("processStage",
				// caseRecord.processStage);
				contentValues_record.put("path", caseRecord.path);
				returnValue3 = db.insert(TABLE_RECORD, null,
						contentValues_record);
			}
		}
		db.close();
		if (returnValue1 == -1 || returnValue2 == -1 || returnValue3 == -1) {
			return 0;
		} else {
			return 1;
		}

	}

	/**
	 * 根据id查询离线图层，有则返回带有update_time的临时对象，便于更新。没有直接返回null
	 */
	public Layer getLocalLayerById(String id) {

		Layer mLayer = null;
		SQLiteDatabase db = getWritableDatabase();

		String[] sectionargs = { id };

		Cursor c = db.query(TABLE_LAYER, null, "layer_id = ?", sectionargs,
				null, null, null);
		boolean hasLayer = c.moveToFirst();
		if (hasLayer) {
			mLayer = new Layer();
			mLayer.id = c.getString(c.getColumnIndex("layer_id"));
			mLayer.name = c.getString(c.getColumnIndex("name"));
			// mLayer.url = c.getString(c.getColumnIndex("url"));
			mLayer.offline_name = c.getString(c.getColumnIndex("offline_name"));
			mLayer.offline_url = c.getString(c.getColumnIndex("offline_url"));
			mLayer.offline_time = c.getLong(c.getColumnIndex("offline_time"));
			mLayer.offline_size = c.getDouble(c.getColumnIndex("offline_size"));
			mLayer.download_status = c.getInt(c.getColumnIndex("isDownloaded"));
			mLayer.hasUpdate = c.getInt(c.getColumnIndex("hasUpdate")) > 0;
		}
		return mLayer;
	}

	/**
	 * 新增或更新本地OfflineLayer记录
	 */
	public void insertOrUpdateOfflineLayer(Layer layer) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_LAYER, "layer_id=?", new String[] { layer.id });
		ContentValues mContentValues = new ContentValues();
		mContentValues.put("layer_id", layer.id);
		mContentValues.put("name", layer.name);
		// mContentValues.put("url", layer.url);
		mContentValues.put("offline_name", layer.offline_name);
		mContentValues.put("offline_url", layer.offline_url);
		mContentValues.put("offline_time", layer.offline_time);
		mContentValues.put("offline_size", layer.offline_size);
		mContentValues.put("isDownloaded", layer.download_status);
		mContentValues.put("hasUpdate", layer.hasUpdate);
		db.insert(TABLE_LAYER, null, mContentValues);
	}

	/** 删除本地保存的Layer */
	public int deleteOfflineLayerById(String layer_id) {
		SQLiteDatabase db = getWritableDatabase();
		int rows = db.delete(TABLE_LAYER, "layer_id=?",
				new String[] { layer_id });
		return rows;
	}

	/**
	 * 问题上报阶段：获得某个case的图片列表
	 */
	public ArrayList<CaseImage> getCaseImage(String selection,
			String[] selectionArgs) {
		ArrayList<CaseImage> imgList = new ArrayList<CaseImage>();
		CaseImage mCaseImage = null;
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TABLE_IMAGE, null, selection, selectionArgs, null,
				null, null);
		if (c != null && c.moveToFirst()) {
			do {
				mCaseImage = new CaseImage();
				mCaseImage.caseID = c.getString(c.getColumnIndex("caseID"));
				// mCaseImage.processStage = c.getString(c
				// .getColumnIndex("processStage"));
				mCaseImage.path = c.getString(c.getColumnIndex("path"));
				imgList.add(mCaseImage);
			} while (c.moveToNext());
			c.close();
		}
		db.close();
		return imgList;
	}

	/**
	 * 问题上报阶段：获得某个case的录音列表
	 */
	public ArrayList<CaseRecord> getCaseRecord(String selection,
			String[] selectionArgs) {

		ArrayList<CaseRecord> recordList = new ArrayList<CaseRecord>();
		CaseRecord mCaseRecord = null;

		SQLiteDatabase db = getWritableDatabase();

		Cursor c = db.query(TABLE_RECORD, null, selection, selectionArgs, null,
				null, null);
		// Log.d("MyDatabase", c.getColumnCount() + "");
		if (c != null && c.moveToFirst()) {
			do {
				mCaseRecord = new CaseRecord();
				mCaseRecord.caseID = c.getString(c.getColumnIndex("caseID"));
				// mCaseRecord.processStage = c.getString(c
				// .getColumnIndex("processStage"));
				mCaseRecord.path = c.getString(c.getColumnIndex("path"));
				recordList.add(mCaseRecord);
			} while (c.moveToNext());
			c.close();
		}
		// boolean hasValue = c.moveToFirst();
		// if (hasValue) {
		// int count = c.getCount();
		// for (int i = 0; i < count; i++) {
		// mCaseRecord = new CaseRecord();
		// mCaseRecord.caseID = c.getString(c.getColumnIndex("caseID"));
		// mCaseRecord.processStage = c.getString(c
		// .getColumnIndex("processStage"));
		// mCaseRecord.path = c.getString(c.getColumnIndex("path"));
		// recordList.add(mCaseRecord);
		// c.moveToNext();
		// }
		//
		// }

		db.close();

		return recordList;
	}

	/**
	 * 启用状态：false获得某个case在特定处理过程的图片
	 */
	// public ArrayList<CaseImage> getCaseImageOfProcessStage(String caseID,
	// String processStage) {
	// ArrayList<CaseImage> imgList = new ArrayList<CaseImage>();
	// CaseImage mCaseImage = null;
	//
	// SQLiteDatabase db = getWritableDatabase();
	//
	// String selection = "caseID = ? and processStage = ? ";
	// String[] selectionArgs = { caseID, processStage };
	//
	// Cursor c = db.query(TABLE_IMAGE, null, selection, selectionArgs, null,
	// null, null);
	// Log.d("MyDatabase", c.getColumnCount() + "");
	// boolean hasValue = c.moveToFirst();
	// if (hasValue) {
	// int count = c.getCount();
	// for (int i = 0; i < count; i++) {
	// mCaseImage = new CaseImage();
	// mCaseImage.caseID = c.getString(c.getColumnIndex("caseID"));
	// mCaseImage.processStage = c.getString(c
	// .getColumnIndex("processStage"));
	// mCaseImage.path = c.getString(c.getColumnIndex("path"));
	// imgList.add(mCaseImage);
	// c.moveToNext();
	// }
	//
	// }
	//
	// db.close();
	//
	// return imgList;
	// }

	/**
	 * 启用状态：false 获得某个case在特定处理过程的录音
	 */
	// public ArrayList<CaseRecord> getCaseRecordOfProcessStage(String caseID,
	// String processStage) {
	//
	// ArrayList<CaseRecord> recordList = new ArrayList<CaseRecord>();
	// CaseRecord mCaseRecord = null;
	//
	// SQLiteDatabase db = getWritableDatabase();
	//
	// String selection = "caseID = ? and processStage = ? ";
	// String[] selectionArgs = { caseID, processStage };
	//
	// Cursor c = db.query(TABLE_RECORD, null, selection, selectionArgs, null,
	// null, null);
	// Log.d("MyDatabase", c.getColumnCount() + "");
	// boolean hasValue = c.moveToFirst();
	// if (hasValue) {
	// int count = c.getCount();
	// for (int i = 0; i < count; i++) {
	// mCaseRecord = new CaseRecord();
	// mCaseRecord.caseID = c.getString(c.getColumnIndex("caseID"));
	// mCaseRecord.processStage = c.getString(c
	// .getColumnIndex("processStage"));
	// mCaseRecord.path = c.getString(c.getColumnIndex("path"));
	// recordList.add(mCaseRecord);
	// c.moveToNext();
	// }
	//
	// }
	//
	// db.close();
	//
	// return recordList;
	// }
}
