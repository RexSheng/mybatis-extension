package com.github.rexsheng.mybatis.core;

import java.util.List;
import java.util.Map;

public class Tuple extends java.util.HashMap<String, Object> implements java.util.Map<String, Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<Class<?>,List<Object>> values;
	
	private Map<String,Map<Class<?>,List<Object>>> aggValues;
	 
	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<T> clazz){
		if(values==null) {
			return null;
		}
		List<Object> valueList=values.get(clazz);
		return valueList==null?null:(T)valueList.get(0);
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<T> clazz){
		if(values==null) {
			return null;
		}
		List<Object> valueList=values.get(clazz);
		return valueList==null?null:(List<T>)valueList;
	}
	
	public <T> T getSum(Class<T> clazz){
		return getAggregation(clazz,"sum");
	}
	
	public <T> T getAvg(Class<T> clazz){
		return getAggregation(clazz,"avg");
	}
	
	public <T> T getCount(Class<T> clazz){
		return getAggregation(clazz,"count");
	}
	
	public <T> T getMax(Class<T> clazz){
		return getAggregation(clazz,"max");
	}
	
	public <T> T getMin(Class<T> clazz){
		return getAggregation(clazz,"min");
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAggregation(Class<T> clazz,String aggregation){
		if(aggValues==null) {
			return null;
		}
		Map<Class<?>,List<Object>> map=aggValues.get(aggregation);
		if(map==null) {
			return null;
		}
		List<Object> valueList=map.get(clazz);
		return valueList==null?null:(T)valueList.get(0);
	} 


	public Map<Class<?>, List<Object>> getValues() {
		return values;
	}


	public void setValues(Map<Class<?>, List<Object>> values) {
		this.values = values;
	}


	public Map<String,Map<Class<?>,List<Object>>> getAggValues() {
		return aggValues;
	}


	public void setAggValues(Map<String,Map<Class<?>,List<Object>>> aggValues) {
		this.aggValues = aggValues;
	}
 
}
