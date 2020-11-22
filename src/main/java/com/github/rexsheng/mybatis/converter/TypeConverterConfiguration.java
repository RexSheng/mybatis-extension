package com.github.rexsheng.mybatis.converter;

/**
 * @author RexSheng
 * 2020年11月16日 下午11:41:39
 */
public abstract class TypeConverterConfiguration<T> {

	private ConverterType converterType;

	public ConverterType getConverterType() {
		return converterType;
	}

	public void setConverterType(ConverterType converterType) {
		this.converterType = converterType;
	}
	
	
}
