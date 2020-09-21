package com.github.rexsheng.mybatis.converter;

public interface IConverterRule {
	default int order() {
		return -1;
	}
	
	default Boolean shouldConvert(Object sourceObj,String propertyName,Class<?> targetClazz, Class<?> targetPropertyType) {
		return true;
	}
	
	default String convertPropertyName(Object sourceObj,String propertyName, Class<?> targetClazz, Class<?> targetPropertyType) {
		return propertyName;
	}
	
	default Object convertPropertyValue(Object sourceObj,String propertyName, Object value,Class<?> targetClazz, Class<?> targetPropertyType) {
		return value;
	}
}
