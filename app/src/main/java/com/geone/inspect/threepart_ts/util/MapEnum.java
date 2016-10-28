package com.geone.inspect.threepart_ts.util;

import java.util.HashMap;
import java.util.Map;

import com.geone.inspect.threepart_ts.adapter.TabPagerAdapter;
import com.geone.inspect.threepart_ts.bean.Case;
import com.geone.inspect.threepart_ts.bean.Category;
import com.geone.inspect.threepart_ts.fragment.CaseReportFragment;
import com.geone.inspect.threepart_ts.sql.MyDatabase;

public class MapEnum {

	public MapEnum() {
		// TODO Auto-generated constructor stub
	}

	public static final Map<String, String> EMPTY_MAP = new HashMap<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1581747318773028322L;

		{
			put(TabPagerAdapter.PAGETITLES_XC[0], "无保存上报案件哦~");
			put(TabPagerAdapter.PAGETITLES_XC[1], "无待核实的案件哦~");
			put(TabPagerAdapter.PAGETITLES_XC[2], "无待核查的案件哦~");
			put(TabPagerAdapter.PAGETITLES_MANAGE[1], "无待处理的案件哦~");
			put(TabPagerAdapter.PAGETITLES_MANAGE[2], "无签收的案件哦~");
		}

	};
	public static final Map<String, String> WFTYPE_MAP = new HashMap<String, String>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4635392019418692743L;

		{
			put(TabPagerAdapter.PAGETITLES_XC[1], Case.WFTYPES[0]);// hs
			put(TabPagerAdapter.PAGETITLES_XC[2], Case.WFTYPES[1]);// hc
			put(TabPagerAdapter.PAGETITLES_MANAGE[1], Case.WFTYPES[2]);// cl
			put(TabPagerAdapter.PAGETITLES_MANAGE[2], Case.WFTYPES[2]);// 签收
		}

	};
	/** pageTitle对应的switch的off、on值 */
	public static final Map<String, String[]> SWITCH_MAP = new HashMap<String, String[]>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5199882480225337148L;

		{
			put(TabPagerAdapter.PAGETITLES_XC[1], Case.SWITCHS[0]);// hs
			put(TabPagerAdapter.PAGETITLES_XC[2], Case.SWITCHS[1]);// hc
			put(TabPagerAdapter.PAGETITLES_MANAGE[1], Case.SWITCHS[2]);// cl
			put(TabPagerAdapter.PAGETITLES_MANAGE[2], Case.SWITCHS[2]);// 签收
		}

	};
	/** pageTitle对应的意见值 */
	public static final Map<String, String> SUGGEST_MAP = new HashMap<String, String>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -495981936899372925L;

		{
			put(TabPagerAdapter.PAGETITLES_XC[1], "核实意见: ");// hs
			put(TabPagerAdapter.PAGETITLES_XC[2], "核查意见: ");// hc
			put(TabPagerAdapter.PAGETITLES_MANAGE[1], "处理意见: ");// cl
			put(TabPagerAdapter.PAGETITLES_MANAGE[2], "处理意见: ");// 签收
		}

	};
	/** pageTitle对应的requestcode */
	public static final Map<String, Integer> REQUEST_MAP = new HashMap<String, Integer>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2914788451031528867L;

		{
			put(TabPagerAdapter.PAGETITLES_XC[0],
					CaseReportFragment.REQUEST_CASE_SB);// 问题上报
			put(TabPagerAdapter.PAGETITLES_XC[1],
					CaseReportFragment.REQUEST_CASE_HS);// hs
			put(TabPagerAdapter.PAGETITLES_XC[2],
					CaseReportFragment.REQUEST_CASE_HC);// hc
			put(TabPagerAdapter.PAGETITLES_MANAGE[1],
					CaseReportFragment.REQUEST_CASE_CL);// cl
			put(TabPagerAdapter.PAGETITLES_MANAGE[2],
					CaseReportFragment.REQUEST_CASE_QS);// 签收
		}

	};
	public static final Map<String, String> CATEGORY_TABLE_MAP = new HashMap<String, String>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5851179881061470428L;

		{
			put(Category.TAG_CATEGORYS[0], MyDatabase.TABLE_CATEGORYS[0]);// 大类
			put(Category.TAG_CATEGORYS[1], MyDatabase.TABLE_CATEGORYS[1]);// 小类
			put(Category.TAG_CATEGORYS[2], MyDatabase.TABLE_CATEGORYS[2]);// 子类

		}

	};
	public static final Map<String, Category> CATEGORY_DEF_MAP = new HashMap<String, Category>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7855188981306575030L;

		{
			put(Category.TAG_CATEGORYS[0],new Category("001", "选择大类"));
			put(Category.TAG_CATEGORYS[1],new Category("0012", "选择小类"));
			put(Category.TAG_CATEGORYS[2],new Category("00001", "选择子类"));
		}

	};

	/** 根据TabPagerAdapter中的pageTitle获取对应的empty值 */
	public static String getEmpty(String key) {
		return EMPTY_MAP.get(key);
	}

	/** 根据TabPagerAdapter中的pageTitle获取对应的wftype值 */
	public static String getWftype(String key) {
		return WFTYPE_MAP.get(key);
	}

	/** 根据pageTitle获取对应的switch的off、on值 */
	public static String[] getSwitch(String key) {
		return SWITCH_MAP.get(key);
	}

	/** 根据pageTitle获取对应的意见值 */
	public static String getSuggest(String key) {
		return SUGGEST_MAP.get(key);
	}

	/** 根据pageTitle获取对应的requestcode */
	public static Integer getRequestCode(String key) {
		return REQUEST_MAP.get(key);
	}
	/** 根据categoryTag类型获取对应的数据表categoryTable */
	public static String getCategoryTable(String key) {
		return CATEGORY_TABLE_MAP.get(key);
	}
	/** 根据大小子类获取对应的默认选项*/
	public static Category getCategoryDef(String key) {
		return CATEGORY_DEF_MAP.get(key);
	}
}
