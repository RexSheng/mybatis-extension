package com.github.rexsheng.mybatis.converter;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class MapUtil {
 

	public static Map<String, Object> beanToMap(Object obj) throws Exception {
		if (obj == null)
			return null;
		Map<String, Object> map = new HashMap<String, Object>();

		BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor property : propertyDescriptors) {
			String key = property.getName();
			System.out.println(key);
			if (key.compareToIgnoreCase("class") == 0) {
				continue;
			}
			Method getter = property.getReadMethod();
			Object value = getter != null ? getter.invoke(obj) : null;
			map.put(key, value);
		}
		return map;
	}
	
	public static Object mapToBean(Map<String,Object> map,Object obj) throws Exception{
		BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor property : propertyDescriptors) {
			String key = property.getName();
			if (key.compareToIgnoreCase("class") == 0) {
				continue;
			}
			if(map.containsKey(key)){
				Object value=map.get(key);
				Method setter=property.getWriteMethod();
				if(setter!=null){
					setter.invoke(obj, value);
				}
			}
		}
		return obj;
	}
	
	public static Object mapToBean(Map<String,Object> map,Class<?> cls) throws Exception{
		Object obj=cls.newInstance();
		mapToBean(map,obj);
		return obj;
	}

	public static class DtoConverter {
		
		private IConverterRule[] rules;
		
		public DtoConverter(IConverterRule... rules) {
			this.setRules(rules);
		}
		
		public String toJson(Object source) {
			
			return null;
		}
		
		public <T> T parseJson(Object source,Class<T> clazz) throws Exception{
			
			return null;
		}
		
		public <T> List<T> parseJsonList(Object source,Class<T> clazz) throws Exception{
			
			return null;
		}
		
		@SuppressWarnings("unchecked")
		public <T> List<T> convertList(Object source,Class<T> clazz) throws Exception{
			if (source == null) {
				return null;
			}

			List<Object> arr = null;
			if (clazz.isArray()) {
				arr = Arrays.asList((Object[]) source);
			} else {
				arr = (List<Object>) source;
			}
			List<T> newList = new ArrayList<T>();
			for(Object m:arr) {
				newList.add(convert(m,clazz));
			}
			return newList;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T convertSelf(T source) throws Exception{
			if (source == null) {
				return null;
			}
			return (T)convert(source,source.getClass());
		}
		
		@SuppressWarnings("unchecked")
		public <T> T convert(Object source,Class<T> clazz) throws Exception{
			if (source == null) {
				return null;
			}

			if (source instanceof java.util.List || clazz.isArray()) {
				List<Object> arr = null;
				if (clazz.isArray()) {
					arr = Arrays.asList((Object[]) source);
					Object t=Array.newInstance(clazz.getComponentType(), arr.size());
					for(int i=0;i<arr.size();i++) {
						Array.set(t, i, convertSelf(arr.get(i)));
					}
					return (T)t;
				} else {
					arr = (List<Object>) source;
					List<Object> newList = new ArrayList<Object>();
					for(Object m:arr) {
						newList.add(convertSelf(m));
					}
					return (T)newList;
				}
			}

			if (source instanceof java.lang.Exception){
				return (T)source;
			}
			if(clazz.getAnnotation(IgnoreConvert.class)!=null){
				return (T)source;
			}
			
			switch (clazz.getTypeName()) {
			case "byte":
				return (T)source;
			case "short":
				return (T)source;
			case "int":
				return (T)source;
			case "long":
				return (T)source;
			case "float":
				return (T)source;
			case "double":
				return (T)source;
			case "char":
				return (T)source;
			case "boolean":
				return (T)source;
			case "java.lang.Byte":
				return (T)source;
			case "java.lang.Short":
				return (T)source;
			case "java.lang.Integer":
				return (T)source;
			case "java.lang.Long":
				return (T)source;
			case "java.lang.Float":
				return (T)source;
			case "java.lang.Double":
				return (T)source;
			case "java.lang.Character":
				return (T)source;
			case "java.lang.Boolean":
				return (T)source;
			case "java.lang.String":
				return (T)source;
			case "java.math.BigDecimal":
				return (T)source;
			case "java.util.Date":
				return (T)source;
			case "java.util.UUID":
				return (T)source;
			default:
				if(source instanceof java.lang.Enum) {
					if(Enum.class.isAssignableFrom(clazz)) {
						return (T)source;
					}
					else {
						return (T)source.toString();
					}
				}
				else if(source instanceof java.util.Map) {
					Map<Object, Object> sourceMap = (Map<Object, Object>) source;
					if(java.util.Map.class.isAssignableFrom(clazz)) {
						Map<Object, Object> result = new HashMap<Object,Object>();
						for (Entry<Object, Object> entry : sourceMap.entrySet()) {
							String fieldName = String.valueOf(entry.getKey());
							if(ifMatchRuler(source,fieldName,clazz)) {
								Object value = sourceMap.get(fieldName);
								result.put(getPropertyNameFromRuler(source,fieldName,clazz), getPropertyValueFromRuler(source,fieldName,convertSelf(value),clazz));
							}
						}
						return (T)result;
					}
					else {
						Field[] fields = getDeclaredFields(source.getClass(),clazz);
						Object obj = createInstance(clazz);
						for (Field field : fields) {
							String fieldName = field.getName();
							for (Entry<Object, Object> entry : sourceMap.entrySet()) {
								String sourceFieldName = String.valueOf(entry.getKey());
								if(ifMatchRuler(source,fieldName,clazz)) {
									String newFieldName = getPropertyNameFromRuler(source,sourceFieldName,clazz);
									if(fieldName.equals(newFieldName)) {
										Object value = sourceMap.get(fieldName);
										field.setAccessible(true);
										field.set(obj, getPropertyValueFromRuler(source,fieldName,convertSelf(value),clazz));
									}
								}
							}
						}
						return (T)obj;
					}
				}
				else if(java.util.Map.class.isAssignableFrom(clazz)) {
					Field[] sourceFields = getDeclaredFields(source.getClass());
					Map<Object,Object> obj = new HashMap<Object,Object>();
					for (Field sourceField : sourceFields) {
						String fieldName = sourceField.getName();
						if(ifMatchRuler(source,fieldName,clazz)) {
							String sourceFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
							String getterSourceFieldName = "get" + sourceFieldName;
							Method method = source.getClass().getMethod(getterSourceFieldName, new Class[] {});
							Object value = method.invoke(source, new Object[] {});
							obj.put(getPropertyNameFromRuler(source,fieldName,clazz),getPropertyValueFromRuler(source,fieldName,convertSelf(value),clazz));
						}
						
					}
					return (T)obj;
				}
				else {
					Field[] fields = getDeclaredFields(clazz);
					Object obj = createInstance(clazz);
					for (Field field : fields) {
						String fieldName = field.getName();
						for (Field sourceField : getDeclaredFields(source.getClass())) {
							if(ifMatchRuler(source,sourceField.getName(),clazz)) {
								if(fieldName.equals(getPropertyNameFromRuler(source,sourceField.getName(),clazz))) {
									String sourceFieldName = sourceField.getName().substring(0, 1).toUpperCase() + sourceField.getName().substring(1);
									String getterSourceFieldName = "get" + sourceFieldName;
									Method method = source.getClass().getMethod(getterSourceFieldName, new Class[] {});
									Object value = method.invoke(source, new Object[] {});
									field.setAccessible(true);
									field.set(obj, convertSelf(value));
								}
							}
						}
						
					}
					return (T)obj;
				}
				
			}
		}

		@SuppressWarnings("unchecked")
		private <T> T createInstance(Class<T> clazz) {
			T t=null;
			try {
				if(java.util.Map.class.isAssignableFrom(clazz)) {
					t= (T)new HashMap<Object,Object>();
				}
				else{
					t= clazz.newInstance();
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return t;
		}
		
		private  Field[] getDeclaredFields(Class<?> source,Class<?> target) {
			if(java.util.Map.class.isAssignableFrom(target)) {
				if(java.util.Map.class.isAssignableFrom(source)) {
					return null;
				}
				else {
					return getDeclaredFields(source);
				}
			}
			else {
				return getDeclaredFields(target);
			}
		}
		
		private  Field[] getDeclaredFields(Class<?> target) {
			return target.getDeclaredFields();
		}

		public IConverterRule[] getRules() {
			return rules;
		}

		public Boolean ifMatchRuler(Object sourceObj, String propertyName, Class<?> targetClazz) {
			for(IConverterRule rule : this.getRules()) {
				if(!rule.shouldConvert(sourceObj, propertyName, targetClazz)) {
					return false;
				}
			}
			return true;
		}
		
		public String getPropertyNameFromRuler(Object sourceObj, String propertyName, Class<?> targetClazz) {
			String newPropertyName=propertyName;
			for(IConverterRule rule : this.getRules()) {
				newPropertyName=rule.convertPropertyName(sourceObj, newPropertyName, targetClazz);
			}
			return newPropertyName;
		}
		
		public Object getPropertyValueFromRuler(Object sourceObj, String propertyName, Object value, Class<?> targetClazz) {
			Object newValue=value;
			for(IConverterRule rule : this.getRules()) {
				newValue=rule.convertPropertyValue(sourceObj, propertyName, newValue, targetClazz);
			}
			return newValue;
		}


		public void setRules(IConverterRule[] rules) {
			this.rules = Arrays.stream(rules).sorted(new Comparator<IConverterRule>() {

				@Override
				public int compare(IConverterRule a, IConverterRule b) {
					// TODO Auto-generated method stub
					return a.order()-b.order();
				}
			}).toArray(IConverterRule[]::new);
		}

	}
	
	public static final IConverterRule UnderLineToCamelCaseRuler=new UnderLineToToCamelCaseRule();
	
	public static final IConverterRule MiddleLineToCamelCaseRuler=new MiddleLineToToCamelCaseRule();
	
	public static final IConverterRule CamelCaseToUnderLineRuler=new CamelCaseToUnderLineRule();
	
	public static final IConverterRule CamelCaseToMiddleLineRuler=new CamelCaseToMiddleLineRule();

	
	public static class UnderLineToToCamelCaseRule implements IConverterRule{

		@Override
		public String convertPropertyName(Object sourceObj, String propertyName, Class<?> targetClazz) {
			Boolean preConverted=false;
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<propertyName.length();i++) {
				char charater=propertyName.charAt(i);
				if(preConverted) {
					if(charater!='_') {
						sb.append(Character.toUpperCase(charater));
						preConverted=false;
					}
				}
				else {
					if(charater=='_') {
						preConverted=true;
					}
					else {
						sb.append(charater);
					}
				}
			}
			return sb.length()>0?sb.toString():propertyName;
		}
	}
	
	public static class CamelCaseToUnderLineRule implements IConverterRule{

		@Override
		public String convertPropertyName(Object sourceObj, String propertyName, Class<?> targetClazz) {
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<propertyName.length();i++) {
				char charater=propertyName.charAt(i);
				if(Character.isUpperCase(charater)) {
					sb.append('_');
					sb.append(Character.toLowerCase(charater));
				}
				else {
					sb.append(charater);
				}
			}
			return sb.toString();
		}
	}
	
	public static class MiddleLineToToCamelCaseRule implements IConverterRule{

		@Override
		public String convertPropertyName(Object sourceObj, String propertyName, Class<?> targetClazz) {
			Boolean preConverted=false;
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<propertyName.length();i++) {
				char charater=propertyName.charAt(i);
				if(preConverted) {
					if(charater!='-') {
						sb.append(Character.toUpperCase(charater));
						preConverted=false;
					}
				}
				else {
					if(charater=='-') {
						preConverted=true;
					}
					else {
						sb.append(charater);
					}
				}
			}
			return sb.length()>0?sb.toString():propertyName;
		}
	}
	
	public static class CamelCaseToMiddleLineRule implements IConverterRule{

		@Override
		public String convertPropertyName(Object sourceObj, String propertyName, Class<?> targetClazz) {
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<propertyName.length();i++) {
				char charater=propertyName.charAt(i);
				if(Character.isUpperCase(charater)) {
					sb.append('-');
					sb.append(Character.toLowerCase(charater));
				}
				else {
					sb.append(charater);
				}
			}
			return sb.toString();
		}
	}

	public static interface IConverterRule{
		
		default int order() {
			return -1;
		}
		
		default Boolean shouldConvert(Object sourceObj,String propertyName,Class<?> targetClazz) {
			return true;
		}
		
		default String convertPropertyName(Object sourceObj,String propertyName,Class<?> targetClazz) {
			return propertyName;
		}
		
		default Object convertPropertyValue(Object sourceObj,String propertyName,Object value,Class<?> targetClazz) {
			return value;
		}
	}
}
