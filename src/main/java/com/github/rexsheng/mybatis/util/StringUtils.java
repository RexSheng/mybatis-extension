package com.github.rexsheng.mybatis.util;

/**
 * @author RexSheng
 * 2020年8月12日 下午10:59:38
 */
public class StringUtils {
	
	/**
	 * 驼峰转下划线
	 * @param source
	 * @return
	 */
	public static String camelCaseToUnderLine(String source) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < source.length(); i++) {
			char charater = source.charAt(i);
			if (Character.isUpperCase(charater)) {
				sb.append('_');
				sb.append(Character.toLowerCase(charater));
			} else {
				sb.append(charater);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 驼峰转下划线
	 * @param source
	 * @return
	 */
	public static String underlineToCamelCase(String source) {
		Boolean preConverted = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < source.length(); i++) {
			char charater = source.charAt(i);
			if (preConverted) {
				if (charater != '_') {
					sb.append(Character.toUpperCase(charater));
					preConverted = false;
				}
			} else {
				if (charater == '_') {
					preConverted = true;
				} else {
					sb.append(charater);
				}
			}
		}
		return sb.length() > 0 ? sb.toString() : source;
	}
	
	/**
	 * 首字母小写，大写转下划线
	 * @param source
	 * @return
	 */
	public static String capitalToUnderLine(String source) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < source.length(); i++) {
			char charater = source.charAt(i);
			if(i==0) {
				sb.append(Character.toLowerCase(charater));
			}
			else if (Character.isUpperCase(charater)) {
				sb.append('_');
				sb.append(Character.toLowerCase(charater));
			} else {
				sb.append(charater);
			}
		}
		return sb.toString();
	}
	
}
