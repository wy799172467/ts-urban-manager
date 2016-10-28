package com.geone.inspect.threepart_ts.bean;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONObject;

/**
 * 说明： 1.该类定义字段可能与服务器端返回的字段不符，但是与db中列名应严格保持一致
 */
public class Case implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3289258651008619963L;
	/** 部件，事件TYPE_DESCS = { "部件", "事件" } */
	public static final String[] TYPE_DESCS = { "部件", "事件" };
	/** 工作流类型，用于区分工作阶段WFTYPES={"hs","hc","cl"} */
	public static final String[] WFTYPES = { "hs", "hc", "cl" };
	/** 开关取值0：关，1：开SWITCHS={{"无问题","有问题"},{"通过","不通过"},{"结案","临时结案"}} */
	public static final String[][] SWITCHS = { { "无问题", "有问题" },
			{ "通过", "不通过" }, { "结案", "临时结案" } };
	/**
	 * 案件审批状态APPROVE_STATUS = { "未读", "已读", "延时申请中", "延时已通过", "延时未通过", "拒签未通过",
	 * "缓办已通过", "缓办未通过", "缓办申请中" }; 返回码：11缓办已通过,12缓办未通过,13缓办申请中
	 */
	public static final String[] APPROVE_STATUS = { "未读", "已读", "延时申请中",
			"延时已通过", "延时未通过", "拒签未通过", "缓办已通过", "缓办未通过", "缓办申请中" };
	/**
	 * 问题上报状态 REPORT_STATUS = { "已保存", "已上报", "不受理", "立案不立案" }
	 */
	public static final String[] REPORT_STATUS = { "已保存", "已上报", "不受理", "立案不立案" };
	/**
	 * 问题上报状态 对应码：REPORT_STATUS_CODE = { "0", "1", "2", "3" }
	 * 问题上报状态REPORT_STATUS = { "已保存", "已上报", "不受理", "不立案" }
	 */
	public static final String[] REPORT_STATUS_CODE = { "0", "1", "2", "3" };

	// 1.待核实待核查待处理公共字段（29个）----
	public String ProblemNo;
	public String Reporter;
	public String ReporterTel;
	public String ProblemOrigin;
	public String ReportWay;
	public String CaseType;
	public String CaseID;
	public String CaseClassI;// 名称对应的编码
	public String CaseClassII;
	public String CaseClassIII;
	public String ReportCaseDesc;
	public String ReportAddress;
	public String ReportTime;
	/** ProcessStageDesc对应编码 */
	public String CurrentStage;
	public String ProcessResult;// ProcessResult1对应编码, 问题上报模块：只存在本地
	public String InspectorID;// 注意此处与服务器返回字段名不同
	public String Shape;// 注意此处与服务器返回字段名不同
	public double X;
	public double Y;
	public String CurrentStageStart;
	public String CaseTypeDesc;
	public String CaseClassIDesc;// 名称
	public String CaseClassIIDesc;
	public String CaseClassIIIDesc;
	/** 处理阶段名称：核实核查处理 */
	public String ProcessStageDesc;
	/** 工作流类型 */
	public String wftype;
	/** 处理结果名称 */
	public String ProcessResult1;// 问题上报模块：未启用
	public String CmdID;
	/** 0:未读 1:已读 2：延时申请中 3：延时已通过 4：延时未通过 5:拒签未通过 */
	public String IsRead;
	/**1:督办*/
	public String SupervisionStatus;

	// ----待核实待核查待处理公共字段（29个）
	// 2.待处理专用----
	/** 是否可以临时结案，1可以0不可以 */
	public String IsCanLSJA;
	public String sendTo;
	// ----待处理专用
	// 3.问题上报专用----
	/** 上报类型码：0正常上报、1随手处置 */
	public String emergency;
	/** 立案条件 */
	public String caseCondition;
	public String gridID;
	public String workFlowID;
	public String disposalID;
	public String AuditorID;
	public String createTime;
	public String postTime;
	public String status;
	// 为配合pc端查找部件位置新增字段---
	/** i查询图层的id号，不要与服务器端返回的layerids（可见图层）混淆 */
	public String layerId;
	/** 所选部件的id */
	public String thingId;// 核实核查处理字段也有用到
	// ----问题上报专用

	// 4.其他----
	public ArrayList<CaseImage> imageList;
	public ArrayList<CaseRecord> recordList;
	/** dealComment，switch内容 */
	public String DealComment;
	/** advice，意见详细描述 */
	public String Advice;

	// 5.临时变量不存数据库
	/** tag本地是够存在记录 */
	public boolean isLocalExist;
	/** 案件是否受理,默认1受理， 0则不受理 */
	public String isAccept = "1";
	// 6.查看延时、查看拒签
	/** 申请日期 */
	public String applyDate;
	/** 申请天数 */
	public String applyAsk;
	/** 申请理由 */
	public String applyDesc;
	/** 审批人员 */
	public String approvePersonName;
	/** 审批日期 */
	public String approveDate;
	/** 审批意见 */
	public String approveAdvice;
	/** 审批状态 */
	public String isPassDesc;
	/** 批准天数 */
	public String approveDays;// 对应Responsetime

	public String toJSON() {
		String jsonString = "";
		JSONObject mJsonObject = new JSONObject();

		return jsonString;
	}

}
