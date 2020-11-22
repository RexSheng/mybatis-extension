package com.github.rexsheng.mybatis.converter;

/**
 * @author RexSheng
 * 2020年11月17日 下午8:55:42
 */
public enum ConverterType {

	TOSTRING(0),CUSTOM(99);
	
	public final Integer code;
	
	ConverterType(Integer code){
		this.code=code;		
	} 
}
