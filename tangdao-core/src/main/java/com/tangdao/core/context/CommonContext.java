/**
 *
 */
package com.tangdao.core.context;

import java.util.ArrayList;
import java.util.List;

import com.tangdao.core.utils.PatternUtil;

import cn.hutool.core.util.StrUtil;

/**
 * <p>
 * TODO 描述
 * </p>
 *
 * @author ruyang@gmail.com
 * @since 2020年6月12日
 */
public class CommonContext {

	public enum PlatformType {

		UNDEFINED(0, "未定义"),

		SEND_MESSAGE_SERVICE(1, "短信服务");

		private int code;
		private String name;

		PlatformType(int code, String name) {
			this.code = code;
			this.name = name;
		}

		public int getCode() {
			return code;
		}

		public String getName() {
			return name;
		}

		public static PlatformType parse(int code) {
			for (PlatformType pt : PlatformType.values()) {
				if (pt.getCode() == code) {
					return pt;
				}
			}
			return null;
		}

		public static List<Integer> allCodes() {
			List<Integer> all = new ArrayList<>();
			for (PlatformType pt : PlatformType.values()) {
				if (pt == PlatformType.UNDEFINED) {
					continue;
				}
				all.add(pt.getCode());
			}
			return all;
		}
	}

	public enum AppType {

		WEB(1, "工作平台"),

		DEVELOPER(2, "开发者平台"),

		BOSS(3, "运营支撑系统");

		private int code;
		private String name;

		AppType(int code, String name) {
			this.code = code;
			this.name = name;
		}

		public int getCode() {
			return code;
		}

		public String getName() {
			return name;
		}

		public static AppType parse(int code) {
			for (AppType at : AppType.values()) {
				if (at.getCode() == code) {
					{
						return at;
					}
				}
			}
			return AppType.WEB;
		}
	}

	public enum CMCP {

		UNRECOGNIZED(0, "无法识别", null),

		CHINA_MOBILE(1, "移动",
				"^((134|135|136|137|138|139|150|151|152|157|158|159|182|183|184|187|188|178|147|198)[0-9]{8}|1705[0-9]{7})$"),

		CHINA_TELECOM(2, "电信", "^((133|149|153|180|181|189||173|177|199|191)[0-9]{8}|(1700|1701)[0-9]{7})$"),

		CHINA_UNICOM(3, "联通",
				"^((130|131|132|155|156|185|186|175|176|145|166|171)[0-9]{8}|(1709|1707|1708|1704)[0-9]{7})$"),

		GLOBAL(4, "全网", "^(13[0-9]|15[012356789]|166|17[05678]|18[0-9]|14[579]|19[89])[0-9]{8}$");

		CMCP(int code, String title, String localRegex) {
			this.code = code;
			this.title = title;
			this.localRegex = localRegex;
		}

		private int code;
		private String title;
		private String localRegex;

		public int getCode() {
			return code;
		}

		public String getTitle() {
			return title;
		}

		public String getLocalRegex() {
			return localRegex;
		}

		public static CMCP getByCode(int code) {
			for (CMCP cmcp : CMCP.values()) {
				if (cmcp.getCode() == code) {
					return cmcp;
				}
			}
			return CMCP.UNRECOGNIZED;
		}

		/**
		 * TODO 获取手机号码归属运营商
		 *
		 * @param mobileNumber
		 * @return
		 */
		public static CMCP local(String mobileNumber) {
			if (StrUtil.isEmpty(mobileNumber)) {
				return CMCP.UNRECOGNIZED;
			}

			for (CMCP cmcp : CMCP.values()) {
				if (PatternUtil.isRight(cmcp.getLocalRegex(), mobileNumber)) {
					return cmcp;
				}
			}
			return CMCP.UNRECOGNIZED;
		}

		/**
		 * TODO 是否为有效的手机号码
		 *
		 * @param mobileNumber
		 * @return
		 */
		public static boolean isAvaiableMobile(String mobileNumber) {
			return StrUtil.isNotBlank(mobileNumber) && PatternUtil.isRight(CMCP.GLOBAL.getLocalRegex(), mobileNumber);
		}

	}
}