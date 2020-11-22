package com.github.rexsheng.mybatis.converter.type;

import com.github.rexsheng.mybatis.converter.ConverterType;
import com.github.rexsheng.mybatis.converter.TypeConverterConfiguration;
import com.github.rexsheng.mybatis.converter.TypeConverterHandler;
import com.github.rexsheng.mybatis.converter.TypeConverterReference;

/**
 * @author RexSheng
 * 2020年11月16日 下午11:40:03
 */
public abstract class BaseTypeConverterHandler<T> extends TypeConverterReference<T> implements TypeConverterHandler<T> {

	protected T value;
	
	protected TypeConverterConfiguration<T> configuration;
	
	protected ConverterType converterType;
		
	@Override
	public void config(TypeConverterConfiguration<T> configuration) {
		// TODO Auto-generated method stub
		this.configuration=configuration;
	}
	
	
	
	@Override
	public TypeConverterHandler<T> setValue(T value,ConverterType converterType) {
		this.value=value;
		this.converterType=converterType;
		return this;
	}
	
	public T getValue() {
		return value;
	}


	@SuppressWarnings("unchecked")
	public <U> U convert(Class<U> clazz) {
		return (U)value;
	}

	@Override
	public T convert() {
		// TODO Auto-generated method stub
		return value;
	}
	
	@Override
	public String toString() {
		if(value==null) {
			return null;
		}
		return value.toString();
	}
}
