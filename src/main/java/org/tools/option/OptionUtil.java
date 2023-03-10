package cn.com.kingshine.geologic.util;

import cn.com.kingshine.geologic.config.ConstConfig;
import cn.com.kingshine.geologic.config.wight.ExplWeightConfig;
import cn.com.kingshine.geologic.config.wight.ProcWeightConfig;
import cn.com.kingshine.geologic.config.wight.SeisWeightConfig;
import cn.com.kingshine.geologic.global.GlobalType;
import cn.com.kingshine.geologic.model.entity.*;
import cn.com.kingshine.geologic.model.entity.Base.BaseLineInfo;
import cn.com.kingshine.geologic.model.entity.Base.tagWellPos;
import cn.com.kingshine.geologic.model.entity.SPS.LineCtrlCoor;
import cn.com.kingshine.geologic.model.entity.Stat.*;
import cn.com.kingshine.geologic.model.entity.external.ExternalVspProjectInfo;
import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author zhangfx
 * @date 2022/8/4
 */
public class OptionUtil {
	public static final String Separator1 = "/";
	public static final String Separator2 = "-";
	private static final String passSource = "qwertyuiopasdfghjklzxcvbnm0123456789.[]QWERTYUIOPASDFGHJKLZXCVBNM!#=+-";
	public static List<Integer> filterRole = List.of(
			GlobalType.emPower_ROLE_DOWNLOAD_CHECK,
			GlobalType.emPower_ARCHIVE_PROJECT,
			GlobalType.emPower_UNARCHIVE_PROJECT,
			GlobalType.emPower_ROLE_SECOND_CHECK,
			GlobalType.emPower_ROLE_SECOND_CHECK,
			GlobalType.emPower_ROLE_FINAL_CHECK,
			GlobalType.emPower_ROLE_BUILD_REPORT
	);
	
	@SuppressWarnings("all")
	public static Map<String, List> groupAuth(List<Authoritys> list) {
		Map<String, List> map = new HashMap<>();
		list.stream()
				.filter(item -> {
					String appdatatype = item.getAppdatatype();
					String roleid = item.getRoleid();
					boolean sysRole = String.valueOf(GlobalType.TREE_PW_SYSTEM).equals(appdatatype);
					return !(sysRole && filterRole.contains(Convert.toInt(roleid, -1)));
				})
				.forEach(item -> {
					if (map.containsKey(item.getAppdatatype())) {
						List o = map.get(item.getAppdatatype());
						HashMap<String, Object> tp = new HashMap<>();
						tp.put("title", item.getRolename());
						tp.put("roleid", item.getAppdatatype() + "-" + item.getRoleid());
						o.add(tp);
					} else {
						HashMap<String, Object> tp = new HashMap<>();
						tp.put("title", item.getRolename());
						tp.put("roleid", item.getAppdatatype() + "-" + item.getRoleid());
						var ls = new ArrayList<>();
						ls.add(tp);
						map.put(item.getAppdatatype(), ls);
					}
				});
		return map;
	}
	
	public static String getAuthTitle(int appType) {
		return switch (appType) {
			case GlobalType.TREE_PW_SYSTEM -> "用户权限";
			case GlobalType.TREE_PW_SEIS -> "采集权限";
			case GlobalType.TREE_PW_EXP -> "解释权限";
			case GlobalType.TREE_PW_WELL -> "井权限";
			case GlobalType.TREE_PW_PRO -> "处理权限";
			case GlobalType.TREE_PW_RESULT -> "成果权限";
			default -> "";
		};
	}
	
	public static <T> List<T> subList(List<T> list, GridPara paras) {
		return OptionUtil.subList(list, paras.getPageNo(), paras.getPageSize());
	}
	
	public static <T> List<T> subList(List<T> list, int pageNo, int pageSize) {
		if (CollectionUtils.isEmpty(list)) {
			return list;
		}
		int size = list.size();
		double totalPage = Math.ceil(size / (pageSize * 1.0));
		
		if (totalPage < pageNo) {
			return new ArrayList<>();
		}
		
		if (size <= pageSize) {
			return list;
		}
		
		int start = (pageNo - 1) * pageSize;
		int end = start + pageSize;
		
		if (end > size) {
			end = size;
		}
		return list.subList(start, end);
	}
	
	public static double termWeightSeis(StatSeisProject stat, Project project) {
		if (stat == null || project == null) return 0;
		SeisWeightConfig config = SpringBeanUtills.getBean(SeisWeightConfig.class);
		
		// 分母
		double scoresum = 0.0,
				// 分子
				member = 0;
		// 项目基本信息评分
		boolean info_w = StringUtil.anyNotBlank(project.getProGeoLoc(), project.getProjectClient(), project.getGZDY());
		int info = config.getInfo();
		scoresum += info;
		if (info_w) member += info;
		// 测线长度/施工面积
		int lenArea = config.getLenArea();
		scoresum += lenArea;
		if (NumberUtil.isPositive(stat.getLengthOrArea())) member += lenArea;
		// 满覆盖长度/满覆盖面积
		int fullLenArea = config.getFullLenArea();
		scoresum += fullLenArea;
		Double full = stat.getLengthOrAreaFull();
		if (NumberUtil.isPositive(full)) member += fullLenArea;
		// 测束线
		int sps = config.getSps();
		Integer swathNum = stat.getSwathNum();
		scoresum += sps;
		if (NumberUtil.isPositive(swathNum)) member += sps;
		// 大炮数据
		int seisRecord = config.getSeisRecord();
		scoresum += seisRecord;
		if (NumberUtil.isPositive(stat.getSeisRecordNum())) {
			if (NumberUtil.isPositive(stat.getSeisRecordShotNum()) && NumberUtil.isPositive(stat.getSpNum())) {
				double ratio = ((stat.getSeisRecordShotNum() + 0.0) / stat.getSpNum()) * 100.0;
				if (ratio > 100.0) {
					ratio = 100.0;
				}
				member += (seisRecord * ratio) / 100.0;
			} else {
				member += seisRecord;
			}
		}
		// 采集班报
		int acqReport = config.getAcqReport();
		scoresum += acqReport;
		if (NumberUtil.isPositive(stat.getSeisBb())) member += acqReport;
		// 表层调查点
		boolean haslvlru = NumberUtil.isPositive(stat.getLvlU()) || NumberUtil.isPositive(stat.getLvlR()),
				haslvlq = NumberUtil.isPositive(stat.getLvlQ()),
				haslvllith = NumberUtil.isPositive(stat.getLvlLith()),
				haslvldate = haslvlru || haslvlq || haslvllith;
		if (haslvldate) {
			if (haslvlru) {
				int lvlru = config.getLvlru();
				scoresum += lvlru;
				member += lvlru;
			} else if (haslvlq) {
				int lvlq = config.getLvlq();
				scoresum += lvlq;
				member += lvlq;
			} else {
				int lvlLith = config.getLvlLith();
				scoresum += lvlLith;
				member += lvlLith;
			}
		}
		// 表层地震记录
		if (haslvlru || haslvlq) {
			int lvlRecord = config.getLvlRecord();
			scoresum += lvlRecord;
			if (NumberUtil.isPositive(stat.getLvlRecordNum())) member += lvlRecord;
		}
		// 表层班报
		if (haslvldate) {
			int lvlReport = config.getLvlReport();
			scoresum += lvlReport;
			if (NumberUtil.isPositive(stat.getLvlbb())) member += lvlReport;
		}
		// 观测系统参数
		int obparam = config.getObparam();
		scoresum += obparam;
		if (NumberUtil.isPositive(stat.getKtseis())) member += obparam;
		// 接收参数
		int recparam = config.getRecparam();
		scoresum += recparam;
		if (NumberUtil.isPositive(stat.getDetPara())) member += recparam;
		// 接收仪器参数
		int recInstparam = config.getRecInstparam();
		scoresum += recInstparam;
		if (NumberUtil.isPositive(stat.getInsPara())) member += recInstparam;
		// 激发参数
		int shotParam = config.getShotParam();
		int spnum = Convert.toInt(stat.getShotPara(), 0) +
				Convert.toInt(stat.getVibPara(), 0) +
				Convert.toInt(stat.getAirPara(), 0);
		scoresum += shotParam;
		if (spnum > 0) member += shotParam;
		// 项目文档
		int document = config.getDocument();
		scoresum += document;
		if (NumberUtil.isPositive(stat.getDocNum())) member += document;
		// 当前项目入库率总计
		if (member + 0.5 >= scoresum) {
			member = scoresum;
		}
		
		double res = scoresum <= 0 ? 0 : member / scoresum * 100.0;
		BigDecimal decimal = new BigDecimal(res);
		decimal = decimal.setScale(2, RoundingMode.HALF_UP);
		return decimal.doubleValue();
	}
	
	public static double termWeightProc(StatProcessProject stat, ProcessProject project) {
		if (project == null || stat == null) {
			return 0;
		}
		var config = SpringBeanUtills.getBean(ProcWeightConfig.class);
		// 分母
		double scoresum = 0.0,
				// 分子
				member = 0;
		// 基本信息:委托单位/处理单位/处理机型/处理软件/处理评价
		int info = config.getInfo();
		boolean base = StringUtil.anyNotBlank(project.getProjectClient(), project.getProcessUnit(), project.getInstModel(), project.getProcessOS(), project.getComments());
		scoresum += info;
		if (base) member += info;
		// 三维处理边界
		
		// 关联采集项目
		int seisProject = config.getSeisProject();
		scoresum += seisProject;
		if (NumberUtil.isPositive(stat.getSeisNum())) member += seisProject;
		// 叠前道集
		if (NumberUtil.isPositive(stat.getMidNum())) {
			int cmpgather = config.getCmpgather();
			scoresum += cmpgather;
			member += cmpgather;
		}
		// 叠后成果
		int resprocsection = config.getResprocsection();
		scoresum += resprocsection;
		if (NumberUtil.isPositive(stat.getSecNum())) member += resprocsection;
		// 速度谱文件
		boolean hasvs = NumberUtil.isPositive(stat.getVsfileNum());
		boolean hasvm = NumberUtil.isPositive(stat.getVmfileNum());
		boolean hasdm = NumberUtil.isPositive(stat.getDmfileNum());
		boolean hastm = NumberUtil.isPositive(stat.getTmfileNum());
		boolean hasvelspectrm = hasvs || hasvm || hasdm || hastm;
		int velspectrum = config.getVelspectrum();
		scoresum += velspectrum;
		if (hasvelspectrm) member += velspectrum;
		// 组排SPS
		int groupsps = config.getGroupsps();
		if (NumberUtil.isPositive(stat.getGroupNum())) member += groupsps;
		// 大炮初至文件
		int bigshotfbt = config.getBigshotfbt();
		scoresum += bigshotfbt;
		if (NumberUtil.isPositive(stat.getBigshotNum())) member += bigshotfbt;
		// 浮动基准面
		if (NumberUtil.isPositive(stat.getFloatGirdFile())) {
			int floatdatum = config.getFloatdatum();
			scoresum += floatdatum;
			member += floatdatum;
		}
		// CDP坐标
		if (NumberUtil.isPositive(stat.getCdpcoorFile())) {
			int cdpcoord = config.getCdpcoord();
			scoresum += cdpcoord;
			member += cdpcoord;
		}
		// 成果图件
		int resultImage = config.getResultImage();
		scoresum += resultImage;
		if (NumberUtil.isPositive(stat.getSectionImage())) {
			member += resultImage;
		}
		// 速度数据体
		if (!hasvelspectrm && NumberUtil.isPositive(stat.getVelsegy())) member += config.getVelsegy();
		
		int document = config.getDocument();
		scoresum += document;
		if (NumberUtil.isPositive(stat.getDocNum())) member += document;
		
		// 当前项目入库率总计
		if (member + 0.5 >= scoresum) {
			member = scoresum;
		}
		double res = scoresum <= 0 ? 0 : member / scoresum * 100.0;
		BigDecimal decimal = new BigDecimal(res);
		decimal = decimal.setScale(2, RoundingMode.HALF_UP);
		return decimal.doubleValue();
	}
	
	public static double termWeightExpl(StatExplainProject stat, VexplainProject project) {
		if (stat == null || project == null) return 0;
		ExplWeightConfig config = SpringBeanUtills.getBean(ExplWeightConfig.class);
		double scoresum = 0, member = 0;
		boolean isInfo = StringUtil.anyNotBlank(project.getProjectClient(), project.getExplainUnit(), project.getExplainOS(), project.getComments());
		// 基本信息:委托单位/解释单位/解释软件/解释评价
		int info = config.getInfo();
		scoresum += info;
		if (isInfo) member += info;
		// 关联处理项目
		int procProject = config.getProcProject();
		scoresum += procProject;
		if (NumberUtil.isPositive(stat.getProNum())) member += procProject;
		// T0层位
		int t0Layer = config.getT0Layer();
		scoresum += t0Layer;
		if (NumberUtil.isPositive(stat.getT0Num())) member += t0Layer;
		// 空间断层
		int spatialfault = config.getSpatialfault();
		scoresum += spatialfault;
		if (NumberUtil.isPositive(stat.getFltFileNum())) member += spatialfault;
		// 速度场,时深转换速度场
		int velfield = config.getVelfield();
		scoresum += velfield;
		int t = Convert.toInt(stat.getVelFieldNum(), 0) + Convert.toInt(stat.getTimeFieldNum(), 0);
		if (t > 0) member += velfield;
		// 合成记录
		if (NumberUtil.isPositive(stat.getSynsecNum())) {
			int synthrecord = config.getSynthrecord();
			scoresum += synthrecord;
			member += synthrecord;
		}
		// 成果图件
		int resultImage = config.getResultImage();
		scoresum += resultImage;
		if (NumberUtil.isPositive(stat.getResultNum())) member += resultImage;
		// 特殊处理成果/正演模型/叠前反演/叠后反演/岩石物理/属性数据合并为解释成果
		int tt = Convert.toInt(stat.getExpsecNum(), 0) + Convert.toInt(stat.getForwardSec(), 0)
				+ Convert.toInt(stat.getPreStackInversion(), 0) + Convert.toInt(stat.getPostStackInversion(), 0)
				+ Convert.toInt(stat.getExpRockPhy(), 0) + Convert.toInt(stat.getExpATTData(), 0);
		int resultSection = config.getResultSection();
		scoresum += resultSection;
		if (tt > 0) member += resultSection;
		// 项目文档
		int document = config.getDocument();
		scoresum += document;
		if (NumberUtil.isPositive(stat.getDocNum())) member += document;
		// 当前项目入库率总计
		if (member + 0.5 >= scoresum) {
			member = scoresum;
		}
		double res = scoresum <= 0 ? 0 : member / scoresum * 100.0;
		BigDecimal decimal = new BigDecimal(res);
		decimal = decimal.setScale(2, RoundingMode.HALF_UP);
		return decimal.doubleValue();
	}
	
	public static String bytesToStr(byte[] bytes) {
		String str;
		StringBuilder stringBuilder = new StringBuilder("");
		for (byte aByte : bytes) {
			int v = aByte & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		str = stringBuilder.toString();
		return str;
	}
	
	public static byte[] strToBytes(String str) {
		byte[] buf = new byte[20];
		for (int i = 0; i < str.length() / 2; i++) {
			String twstr = str.substring(i * 2, i * 2 + 2);//
			buf[i] = (byte) Integer.parseInt(twstr, 16);
		}
		return buf;
	}
	
	public static String getSqlserverDate(String Separator) {
		return OptionUtil.Separator1.equals(Separator) ?
				"CONVERT(varchar,dateadd(dd,-day(getdate())+1,getdate()),111)"
				: "CONVERT(varchar,dateadd(dd,-day(getdate())+1,getdate()),126)";
	}
	
	public static String getOracleDate(String Separator) {
		return OptionUtil.Separator1.equals(Separator) ?
				"TO_CHAR(TRUNC(SYSDATE, 'mm'),'yyyy/mm/dd')"
				: "TO_CHAR(TRUNC(SYSDATE, 'mm'),'yyyy-mm-dd')";
	}
	
	public static String getDBDate() {
		ConstConfig config = SpringBeanUtills.getBean(ConstConfig.class);
		return "0".equals(config.getDatabaseType()) ? getSqlserverDate(OptionUtil.Separator1) : getOracleDate(OptionUtil.Separator1);
	}
	
	public static String getDBDate(String Separator) {
		ConstConfig config = SpringBeanUtills.getBean(ConstConfig.class);
		return "0".equals(config.getDatabaseType()) ? getSqlserverDate(Separator) : getOracleDate(Separator);
	}
	
	/**
	 * 生成随机字符串用作密钥
	 *
	 * @param passLen 字符串长度，如果该值为0，则默认生成长度为32字符串
	 * @return 指定长度字符串
	 */
	public static String generatorPassword(final int passLen) {
		Random random = new Random(System.currentTimeMillis());
		StringBuilder password = new StringBuilder();
		int len = passLen == 0 ? 32 : passLen;
		for (int i = 0; i < len; i++) {
			int index = random.nextInt(passSource.length());
			password.append(passSource.charAt(index));
		}
		return password.toString();
	}
	
	public static String generatorPassword() {
		return generatorPassword(0);
	}
	
	public static <T> Set<T> toSet(T[] arr) {
		if (arr == null) return new HashSet<>(0);
		Set<T> res = new HashSet<>(arr.length);
		Collections.addAll(res, arr);
		return res;
	}
	
	public static <T> List<T> toList(T[] arr) {
		if (arr == null) return new ArrayList<>(0);
		List<T> res = new ArrayList<>(arr.length);
		Collections.addAll(res, arr);
		return res;
	}
	
	public static boolean and(boolean... tar) {
		if (tar == null || tar.length == 0) return false;
		boolean result = true;
		for (boolean var : tar) {
			result = (result && var);
		}
		return result;
	}
	
	public static boolean or(boolean... b) {
		if (b == null || b.length == 0) return false;
		if (b.length == 1) return b[0];
		boolean result = b[0];
		for (int i = 1; i < b.length; i++) {
			result = (result || b[i]);
		}
		return result;
	}
	
	public static long getAllPage(long count, long pageSize) {
		long page = count / pageSize;
		long rd = count % pageSize;
		return rd == 0 ? page : page + 1;
	}
	
	/**
	 * 采集项目简要信息数组
	 *
	 * @param items
	 * @return
	 */
	public static JSONArray convertSeisToBrief(List<Project> items) {
		JSONArray array = JSONUtil.createArray();
		if (items == null) return array;
		for (Project item : items) {
			JSONObject jsonBrief = JSONUtil.createObj();
			jsonBrief.set("id", item.getProjectId());
			jsonBrief.set("名称", item.getProjectName());
			jsonBrief.set("类型", GlobalType.em2D.equals(item.getProjectType()) ? "二维" : "三维");
			jsonBrief.set("年度", item.getYearNo());
			jsonBrief.set("工区", item.getWorkAreaName());
			jsonBrief.set("度带号", item.getProjectionZone());
			jsonBrief.set("施工方式", item.getGeometryTypeName());
			jsonBrief.set("基准面类型", item.getFieldDatumType());
			jsonBrief.set("基准面", item.getFieldDatum());
			jsonBrief.set("替换速度", 0 == item.getFieldReplVelocity() ? "" : item.getFieldReplVelocity());
			jsonBrief.set("满覆盖次数", item.getFoldNum());
			jsonBrief.set("处理历史", item.getProcessProjects().size());
			jsonBrief.set("测束线", item.getSwathNum());
			jsonBrief.set("文档", item.getDocNum());
			jsonBrief.set("大炮数据", item.getSeisRecordNum());
			jsonBrief.set("典型记录", item.getTipRecordNum());
			jsonBrief.set("大炮初至", item.getBigshotNum());
			array.add(jsonBrief);
		}
		return array;
	}
	
	public static JSONArray convertProcToBrief(List<SummaryProcessProject> items) {
		var jsonBriefs = JSONUtil.createArray();
		if (items == null) return jsonBriefs;
		for (int i = 0; i < items.size(); ++i) {
			var jsonBrief = JSONUtil.createObj();
			SummaryProcessProject ppb = items.get(i);
			jsonBrief.set("id", ppb.getProjectId());
			jsonBrief.set("名称", ppb.getProjectName());
			jsonBrief.set("类型", GlobalType.em2D.equals(ppb.getProjectType()) ? "二维" : "三维");
			jsonBrief.set("年度", ppb.getYearNo());
			jsonBrief.set("工区", ppb.getWorkAreaName());
			jsonBrief.set("度带号", ppb.getProjectionZone());
			jsonBrief.set("基准面", ppb.getDatumName());
			jsonBrief.set("替换速度", ppb.getVelocityName());
			jsonBrief.set("高速层顶界", ppb.getWeathBase());
			jsonBrief.set("关联采集项目", ppb.getSeisNum());
			jsonBrief.set("地震数据", ppb.getSecNum());
			jsonBrief.set("测束线", ppb.getSwathNum());
			jsonBrief.set("文档", ppb.getDocNum());
			jsonBrief.set("叠加速度谱", ppb.getVsNum());
			jsonBrief.set("偏移速度谱", ppb.getVmNum());
			jsonBrief.set("叠前深度偏速度谱", ppb.getDmNum());
			jsonBrief.set("叠前时间偏速度谱", ppb.getTmNum());
			jsonBriefs.add(i, jsonBrief);
		}
		return jsonBriefs;
	}
	
	public static JSONArray convertVspToJSONAraay(List<ExternalVspProjectInfo> items) {
		var result = JSONUtil.createArray();
		if (items == null) return result;
		for (int i = 0; i < items.size(); ++i) {
			ExternalVspProjectInfo vspProjectInfo = items.get(i);
			com.alibaba.fastjson.JSONObject jsonVspProjectInfo = new com.alibaba.fastjson.JSONObject();
			
			tagWellPos well = vspProjectInfo.getWell();
			com.alibaba.fastjson.JSONObject jsonWell = new com.alibaba.fastjson.JSONObject();
			jsonWell.put("id", well.getXh());
			jsonWell.put("井名", well.getJh());
			jsonVspProjectInfo.put("井", jsonWell);
			
			List<VspCjinfo> listVspCJInfo = vspProjectInfo.getListVspCJInfo();
			com.alibaba.fastjson.JSONArray jsonCJArray = new com.alibaba.fastjson.JSONArray();
			if (null != listVspCJInfo) {
				for (int j = 0; j < listVspCJInfo.size(); ++j) {
					VspCjinfo cjInfo = listVspCJInfo.get(j);
					com.alibaba.fastjson.JSONObject jsonCJ = new com.alibaba.fastjson.JSONObject();
					jsonCJ.put("项目名称", cjInfo.getSiteName());
					jsonCJ.put("立项单位", cjInfo.getProjectUnit());
					jsonCJ.put("度带号", well.getJkddh());
					jsonCJ.put("地质单元", cjInfo.getArea());
					jsonCJ.put("地表类型", cjInfo.getBasin());
					jsonCJ.put("项目来源", cjInfo.getProjectSource());
					jsonCJ.put("观测类型", cjInfo.getRevType());
					jsonCJ.put("激发类型", "炸药震源");
					jsonCJ.put("井号", well.getJh());
					jsonCJ.put("英文名", well.getJhen());
					jsonCJ.put("井口东坐标", null == well.getJkhzbx() ? "" : well.getJkhzbx());
					jsonCJ.put("井口北坐标", null == well.getJkzzby() ? "" : well.getJkzzby());
					jsonCJ.put("地表高程", null == well.getSurfaceEle() ? "" : well.getSurfaceEle());
					jsonCJ.put("补心高程", null == well.getBxhb() ? "" : well.getBxhb());
					jsonCJ.put("观测起始井深", null == cjInfo.getStartDepth() ? "" : cjInfo.getStartDepth());
					jsonCJ.put("观测结束井深", null == cjInfo.getEndDepth() ? "" : cjInfo.getEndDepth());
					jsonCJ.put("采样率", null == cjInfo.getSampRate() ? "" : cjInfo.getSampRate());
					jsonCJ.put("记录长度", null == cjInfo.getRecordLen() ? "" : cjInfo.getRecordLen());
					jsonCJ.put("过井测线", cjInfo.getMainLine());
					jsonCJ.put("检波器型号", cjInfo.getDetectorMode());
					jsonCJ.put("仪器型号", cjInfo.getInstModel());
					jsonCJ.put("有效物理点数", cjInfo.getQualifed());
					jsonCJ.put("施工单位", cjInfo.getUint());
					jsonCJ.put("施工队号", cjInfo.getBuilderNo());
					jsonCJ.put("起测日期", cjInfo.getStartDate());
					jsonCJ.put("结束日期", cjInfo.getEndDate());
					jsonCJ.put("备注", cjInfo.getRemark());
//					jsonCJArray.put(j, jsonCJ);
					jsonCJArray.add(j, jsonCJ);
				}
			}
			jsonVspProjectInfo.put("VSP采集成果", jsonCJArray);
			
			List<VspExplainBaseInfo> listVspProcess = vspProjectInfo.getListVspProcess();
			com.alibaba.fastjson.JSONArray jsonProcessArray = new com.alibaba.fastjson.JSONArray();
			if (null != listVspProcess) {
				for (int j = 0; j < listVspProcess.size(); ++j) {
					VspExplainBaseInfo process = listVspProcess.get(j);
					var jsonProcess = JSONUtil.createObj();
					setVspExplainInfoJson(jsonProcess, process);
					jsonProcessArray.add(j, jsonProcess);
				}
			}
			jsonVspProjectInfo.put("VSP处理成果", jsonProcessArray);
			
			List<VspExplainBaseInfo> listVspExplain = vspProjectInfo.getListVspExplain();
			com.alibaba.fastjson.JSONArray jsonExplainArray = new com.alibaba.fastjson.JSONArray();
			if (null != listVspExplain) {
				for (int j = 0; j < listVspExplain.size(); ++j) {
					VspExplainBaseInfo explain = listVspExplain.get(j);
					var jsonExplain = JSONUtil.createObj();
					setVspExplainInfoJson(jsonExplain, explain);
					jsonExplainArray.add(j, jsonExplain);
				}
			}
			jsonVspProjectInfo.put("VSP解释成果", jsonExplainArray);
			result.add(i, jsonVspProjectInfo);
		}
		return result;
	}
	
	public static JSONObject convertSwathLine(LineBaseinfo lineBaseInfo, StatLine lineStatInfo) {
		JSONObject jsonResponse = new JSONObject();
		// 测线基本信息
		JSONObject jsonLineBaseInfo = new JSONObject();
		jsonLineBaseInfo.set("测线名", lineBaseInfo.getLineName());
		jsonLineBaseInfo.set("施工方式", lineBaseInfo.getGeometryTypeName());
		setSeismBaseLineInfoJson(lineBaseInfo, jsonLineBaseInfo);
		jsonLineBaseInfo.set("测线长度(km)", TypeSupport.formatTo2(lineBaseInfo.getLineLengh()));
		jsonLineBaseInfo.set("满覆盖长度(km)", TypeSupport.formatTo2(lineBaseInfo.getFullFoldLengh()));
		jsonLineBaseInfo.set("记录长度", TypeSupport.formatTo2(lineBaseInfo.getRecordLen()));
		jsonLineBaseInfo.set("采样率", TypeSupport.formatTo2(lineBaseInfo.getSampRate()));
		jsonResponse.set("测线基本信息", jsonLineBaseInfo);
		// 测线统计信息
		JSONObject jsonLineStatInfo = new JSONObject();
		setSeismSwathLineStatInfoJson(lineStatInfo, lineBaseInfo, jsonLineStatInfo);
		jsonResponse.set("测线统计信息", jsonLineStatInfo);
		return jsonResponse;
	}
	
	public static JSONObject convertSwathLine(DswBasInfo swathBaseInfo, StatLine swathStatInfo) {
		JSONObject jsonResponse = new JSONObject();
		// 束线基本信息
		JSONObject jsonSwathBaseInfo = new JSONObject();
		jsonSwathBaseInfo.set("束线名", swathBaseInfo.getLineName());
		jsonSwathBaseInfo.set("施工方式", swathBaseInfo.getGeometryTypeName());
		setSeismBaseLineInfoJson(swathBaseInfo, jsonSwathBaseInfo);
		jsonSwathBaseInfo.set("接收线距", TypeSupport.formatTo1(swathBaseInfo.getRlinterval()));
		jsonSwathBaseInfo.set("炮线距", TypeSupport.formatTo1(swathBaseInfo.getSlinterval()));
		jsonResponse.set("束线基本信息", jsonSwathBaseInfo);
		// 束线统计信息
		JSONObject jsonSwathStatInfo = new JSONObject();
		setSeismSwathLineStatInfoJson(swathStatInfo, swathBaseInfo, jsonSwathStatInfo);
		jsonResponse.set("束线统计信息", jsonSwathStatInfo);
		return jsonResponse;
	}
	
	
	public static JSONObject convertSeisBorder(List<Border> borders, Integer borderType) {
		JSONObject jsonResponse = new JSONObject();
		JSONArray jsonBorders = new JSONArray();
		for (int i = 0; i < borders.size(); ++i) {
			Border border = borders.get(i);
			JSONObject jsonBorder = new JSONObject();
			String type = border.getEdgeType();
			String typeName = "0".equals(type) ? "施工边界" : "1".equals(type) ? "满覆盖边界" : "一次边界";
			if (-1 != borderType && borderType.equals(Integer.valueOf(type))) {
				continue;
			}
			jsonBorder.set("边界类型", typeName);
			jsonBorder.set("东坐标", border.getEastCoor());
			jsonBorder.set("北坐标", border.getNorthCoor());
			jsonBorder.set("高程", -9999.0 == border.getSurfaceEle() ? "" : border.getSurfaceEle());
			jsonBorders.add(i, jsonBorder);
		}
		jsonResponse.set("采集项目边界", jsonBorders);
		return jsonResponse;
	}
	
	public static JSONObject convertProcBorder(List<VdsurVeyParPoint> borders) {
		JSONObject jsonResponse = new JSONObject();
		JSONArray jsonBorders = new JSONArray();
		for (int i = 0; i < borders.size(); ++i) {
			VdsurVeyParPoint border = borders.get(i);
			JSONObject jsonBorder = new JSONObject();
			jsonBorder.set("东坐标", border.getEastCoor());
			jsonBorder.set("北坐标", border.getNorthCoor());
			jsonBorder.set("点索引", border.getId().getPointIndex());
			jsonBorder.set("InLine号", border.getId().getInLineNo());
			jsonBorder.set("CrossLine号", border.getId().getCroLineNo());
			jsonBorders.add(i, jsonBorder);
		}
		jsonResponse.set("处理项目边界", jsonBorders);
		return jsonResponse;
	}
	
	public static JSONObject convertSeismCtrlPoint(List<LineCtrlCoor> ctrlPoints) {
		JSONObject jsonResponse = new JSONObject();
		JSONArray jsonCtrlPoints = new JSONArray();
		for (int i = 0; i < ctrlPoints.size(); ++i) {
			LineCtrlCoor ctrlPoint = ctrlPoints.get(i);
			JSONObject jsonCtrlPoint = new JSONObject();
			jsonCtrlPoint.set("测线ID", ctrlPoint.getRline2d().getRlineId());
			jsonCtrlPoint.set("测线号", ctrlPoint.getRline2d().getRlineNo());
			jsonCtrlPoint.set("拐点桩号", ctrlPoint.getCtrlStationNo());
			jsonCtrlPoint.set("东坐标", TypeSupport.formatTo1(ctrlPoint.getEastCoor()));
			jsonCtrlPoint.set("北坐标", TypeSupport.formatTo1(ctrlPoint.getNorthCoor()));
			jsonCtrlPoint.set("高程", TypeSupport.formatTo1(ctrlPoint.getSurfaceEle()));
			jsonCtrlPoints.add(i, jsonCtrlPoint);
		}
		jsonResponse.set("采集测线拐点", jsonCtrlPoints);
		return jsonResponse;
	}
	
	/**
	 * @param request         HTTP请求
	 * @param response        HTTP响应
	 * @param srcFilePathName 服务器文件全路径文件名
	 * @throws IOException
	 * @brief HTTP响应以字节流方式传输文件
	 */
	public static void transferFileStream(HttpServletRequest request,
										  HttpServletResponse response, String srcFilePathName) throws IOException {
		response.setContentType("application/octet-stream;");
		response.setCharacterEncoding("UTF-8");
		File fileIuput = new File(srcFilePathName);
		if (fileIuput.exists()) {
			byte[] buff = new byte[1024];
			String fileName = fileIuput.getName();
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			response.setHeader("Content-Length", String.valueOf(fileIuput.length()));
			response.setCharacterEncoding("UTF-8");
			if (request.getMethod().equals("HEAD")) {    // 只获取response的HEADER信息
				return;
			}
			FileInputStream inStream = new FileInputStream(fileIuput);
			ServletOutputStream outStream = response.getOutputStream();
			int n = inStream.read(buff);
			while (n != -1) {
				outStream.write(buff, 0, n);
				n = inStream.read(buff);
			}
			System.out.println("写入流完成");
			inStream.close();
			outStream.close();
			// 删除文件
			FileUtils.deleteQuietly(fileIuput);
		} else {
			System.err.println("未找到文件" + fileIuput.getPath());
			throw new IOException("未找到文件" + fileIuput.getName());
		}
	}
	
	public static void writeFile(String filePath, HttpServletResponse response) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) return;
		response.setContentType("application/octet-stream;");
		// 避免下载文件名出现不显示汉字问题
		response.setHeader("Content-Disposition", "attachment; filename=" +
				new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
		response.setHeader("Content-Length", String.valueOf(file.length()));
		writeFile(file, response.getOutputStream());
	}
	
	public static void writeFile(String filePath, OutputStream outputStream) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) throw new FileNotFoundException("不存在文件： %s，请确保该文件存在。 ".formatted(filePath));
		writeFile(file, outputStream);
	}
	
	public static void writeFile(File file, OutputStream outputStream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		BufferedOutputStream bos = new BufferedOutputStream(outputStream);
		try (bis;bos) {
			byte[] bytes = new byte[1024 * 4];
			int len = -1;
			while ((len = bis.read(bytes)) != -1) {
				bos.write(bytes, 0, len);
			}
			bos.flush();
		}
	}
	
	/**
	 * @param bli     测束线信息基类对象
	 * @param jsonObj 测束线基类信息JSON对象
	 * @brief 设置采集项目测束线基类信息JSON对象(用于获取测束线信息)
	 */
	private static void setSeismBaseLineInfoJson(BaseLineInfo bli, JSONObject jsonObj) {
		jsonObj.set("投影带号", bli.getProjectionZone());
		jsonObj.set("覆盖次数", bli.getFold());
		jsonObj.set("仪器道数", bli.getInstruChannel());
		String sourceType = bli.getSourceType();
		jsonObj.set("震源类型", GlobalType.getShotSourceTypeName(sourceType));
		jsonObj.set("仪器型号", bli.getInstModel());
		jsonObj.set("观测系统类型", bli.getObType());
		jsonObj.set("最小炮检距", TypeSupport.formatTo1(bli.getMinOffset()));
		jsonObj.set("最大炮检距", TypeSupport.formatTo1(bli.getMaxOffset()));
		jsonObj.set("道距", TypeSupport.formatTo1(bli.getRpinterval()));
		jsonObj.set("炮点距", TypeSupport.formatTo1(bli.getSpinterval()));
		String fieldDatum = "0".equals(bli.getFieldDatumType()) ? "浮动" : "水平";
		fieldDatum += "_" + bli.getFieldDatum();
		jsonObj.set("基准面", fieldDatum);
		jsonObj.set("替换速度", bli.getFieldReplVelocity());
		jsonObj.set("野外处理系统", bli.getOpSytem());
		jsonObj.set("检波器型号", bli.getrInstModel());
		jsonObj.set("阶段", bli.getStage());
		String contractorName = (null == bli.getPartyInfo() || null == bli.getPartyInfo().getContractor()) ? "" : bli.getPartyInfo().getContractor().getContractorName();
		String partyName = (null == bli.getPartyInfo()) ? "" : bli.getPartyInfo().getPartyName();
		jsonObj.set("施工单位", (null == contractorName ? "" : contractorName) + (null == partyName ? "" : partyName));
	}
	
	/**
	 * @param statLine     测束线统计信息
	 * @param baseLineInfo 测束线基本信息
	 * @param jsonObj      JSON对象
	 * @brief 设置采集项目测束线统计信息JSON对象
	 */
	private static void setSeismSwathLineStatInfoJson(StatLine statLine, BaseLineInfo baseLineInfo, JSONObject jsonObj) {
		jsonObj.set("炮点", statLine.getSpNum());
		jsonObj.set("井炮", statLine.getSpENum());
		jsonObj.set("可控震源", statLine.getSpVNum());
		jsonObj.set("其它震源", statLine.getSpONum());
		jsonObj.set("废炮", statLine.getSpKlNum());
		jsonObj.set("检波点", statLine.getRpNum());
		jsonObj.set("表层调查点", statLine.getLvlnum());
		jsonObj.set("小折射", statLine.getLvlR());
		jsonObj.set("微测井", statLine.getLvlU());
		jsonObj.set("大折射", statLine.getLvlE());
		jsonObj.set("水坑", statLine.getLvlW());
		jsonObj.set("插入点", statLine.getLvlInsert());
		jsonObj.set("其它表层点", statLine.getLvlO());
		jsonObj.set("测线交点", statLine.getCrossNum());
		jsonObj.set("校正量闭合交点", statLine.getClosedCrossNum());
		jsonObj.set("校正量不闭合交点", statLine.getUnClosedCrossNum());
		jsonObj.set("表层地震记录", statLine.getLvlRecordNum());
		jsonObj.set("大炮初至", statLine.getBigshotNum());
		jsonObj.set("大炮数据", statLine.getSeisRecordNum());
		jsonObj.set("典型记录", statLine.getTipRecordNum());
		jsonObj.set("地表地质图件", statLine.getGeoNum());
		jsonObj.set("生产记录", baseLineInfo.getRecord());
		jsonObj.set("一级品", baseLineInfo.getFirstRecord());
		jsonObj.set("二级品", baseLineInfo.getSubRecord());
	}
	
	
	private static void setVspExplainInfoJson(JSONObject jsonObj, VspExplainBaseInfo vspProcExpInfo) {
		jsonObj.set("项目名称", vspProcExpInfo.getProjectName());
		jsonObj.set("年度", null == vspProcExpInfo.getYearNo() ? "" : vspProcExpInfo.getYearNo());
		jsonObj.set("承包商", vspProcExpInfo.getContractor());
		jsonObj.set("软件名称", vspProcExpInfo.getSoftName());
		jsonObj.set("处理单位", vspProcExpInfo.getProcessUnit());
		jsonObj.set("处理员", vspProcExpInfo.getProcessor());
		jsonObj.set("处理流程说明", vspProcExpInfo.getProcessFlow());
		jsonObj.set("开始时间", vspProcExpInfo.getStartDate());
		jsonObj.set("完成时间", vspProcExpInfo.getEndDate());
		jsonObj.set("测线名", vspProcExpInfo.getLineName());
		jsonObj.set("炮数", null == vspProcExpInfo.getShotNum() ? "" : vspProcExpInfo.getShotNum());
		jsonObj.set("道距", null == vspProcExpInfo.getTraceLen() ? "" : vspProcExpInfo.getTraceLen());
		jsonObj.set("记录长度", null == vspProcExpInfo.getRecordLen() ? "" : vspProcExpInfo.getRecordLen());
		jsonObj.set("采样间隔", null == vspProcExpInfo.getTraceInt() ? "" : vspProcExpInfo.getTraceInt());
		jsonObj.set("显示方向", vspProcExpInfo.getShowDir());
		jsonObj.set("基准面", null == vspProcExpInfo.getDatumPlane() ? "" : vspProcExpInfo.getDatumPlane());
		jsonObj.set("低降速层厚度", null == vspProcExpInfo.getLowThick() ? "" : vspProcExpInfo.getLowThick());
		jsonObj.set("低降速层速度", null == vspProcExpInfo.getLowVi() ? "" : vspProcExpInfo.getLowVi());
		jsonObj.set("填充速度", null == vspProcExpInfo.getReplaceV() ? "" : vspProcExpInfo.getReplaceV());
		jsonObj.set("双程基准面校正量", null == vspProcExpInfo.getDdatumStatics() ? "" : vspProcExpInfo.getDdatumStatics());
		jsonObj.set("双程校正量", null == vspProcExpInfo.getDstatics() ? "" : vspProcExpInfo.getDstatics());
		jsonObj.set("备注", vspProcExpInfo.getComment());
	}
}






















