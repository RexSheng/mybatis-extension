package com.github.rexsheng.mybatis.util;

import java.util.StringTokenizer;

/**
 * @author RexSheng 2020年8月12日 下午10:59:38
 */
public class StringUtils {

	/**
	 * 驼峰转下划线
	 * 
	 * @param source 字符串
	 * @return 转换后的字符
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
	 * 
	 * @param source 字符串
	 * @return 字符串
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
	 * 
	 * @param source 字符串
	 * @return 字符串
	 */
	public static String capitalToUnderLine(String source) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < source.length(); i++) {
			char charater = source.charAt(i);
			if (i == 0) {
				sb.append(Character.toLowerCase(charater));
			} else if (Character.isUpperCase(charater)) {
				sb.append('_');
				sb.append(Character.toLowerCase(charater));
			} else {
				sb.append(charater);
			}
		}
		return sb.toString();
	}

	public static boolean hasValue(String s) {
		return s != null && s.length() > 0;
	}

	public static String removeBreakingWhitespace(String original) {
		StringTokenizer whitespaceStripper = new StringTokenizer(original);
		StringBuilder builder = new StringBuilder();
		while (whitespaceStripper.hasMoreTokens()) {
			builder.append(whitespaceStripper.nextToken());
			builder.append(" ");
		}
		return builder.toString();
	}
	
	public static String replacePrefix(String source,String stopMark,String replaceStr) {
		int index=source.toUpperCase().indexOf(stopMark);
		if(index>-1) {
			return replaceStr+source.substring(index+stopMark.length());
		}
		return source;
	}
	
	public static String replaceSuffix(String source,String stopMark,String replaceStr) {
		int index=source.toUpperCase().lastIndexOf(stopMark);
		if(index>-1) {
			return replaceStr+source.substring(0,index);
		}
		return source;
	}
	
	public static String replaceFirst(String source,String stopMark,String replaceStr) {
		int index=source.toUpperCase().indexOf(stopMark);
		if(index>-1) {
			return source.substring(0,index)+replaceStr+source.substring(index+stopMark.length());
		}
		return source;
	}
	
	public static void main(String[] args) {
		String sql="Select * from Table a where a.id>0 order by create_time desc";
		System.out.println(replacePrefix(sql," FROM ","SELECT COUNT(*) FROM "));
		System.out.println(replaceSuffix(sql," ORDER BY ",""));
		System.out.println(replacePrefix(sql,"SELECT ","SELECT ROWNUM as _rownum,"));
		System.out.println(replaceFirst(sql," WHERE "," WHERE ROWNUM>1 AND "));
	}
}
