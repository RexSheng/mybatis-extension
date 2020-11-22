package com.github.rexsheng.mybatis.converter;

/**
 * @author RexSheng
 * 2020年11月16日 下午11:39:14
 */
public interface TypeConverterHandler<T> {
	
	void config(TypeConverterConfiguration<T> configuration);
	
	TypeConverterHandler<T> setValue(T value,ConverterType converterType);
		
	<U> U convert(Class<U> clazz);
	
	T convert();
	
	String toString();
}
