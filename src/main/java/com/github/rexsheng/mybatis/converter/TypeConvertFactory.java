package com.github.rexsheng.mybatis.converter;

import java.util.Date;

/**
 * @author RexSheng
 * 2020年11月17日 上午12:59:14
 */
public class TypeConvertFactory {
	private static TypeConverterRegistry registry;
	static {
		registry=new TypeConverterRegistry();
	}

	public static void main(String[] args) {
		registry.getHandler(new Date()).convert(String.class);
		System.out.println("==========");
		registry.getHandler(9912).convert(String.class);
		System.out.println("==========>>>>>>>");
	}
}
