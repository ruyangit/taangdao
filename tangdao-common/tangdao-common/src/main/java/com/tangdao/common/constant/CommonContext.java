/**
 *
 */
package com.tangdao.common.constant;

/**
 * <p>
 * TODO 描述
 * </p>
 *
 * @author ruyang@gmail.com
 * @since 2020年6月12日
 */
public class CommonContext {

	public enum UserSource {

		WEB("网页注册"),

		DEVELOPER("开发者平台");

		private String name;

		UserSource(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}