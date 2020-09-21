package com.github.rexsheng.mybatis.converter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 对象转换类v1.0
 * 
 * @author RexSheng 2019年4月2日 下午4:45:47
 */
public class DtoConverter {
	private IConverterRule[] rules;
	
//	@SuppressWarnings("unused")
//	private Map<Class<?>,Field[]> classFieldMap=new HashMap<>();
	
	private Map<Class<?>,Map<String,Field>> classFieldMap=new HashMap<>();

	public DtoConverter(IConverterRule... rules) {
		this.setRules(rules);
	}

	public String toJson(Object source) {

		return null;
	}

	public <T> T parseJson(Object source, Class<T> clazz) throws Exception {

		return null;
	}

	public <T> List<T> parseJsonList(Object source, Class<T> clazz) throws Exception {

		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> convertList(Object source, Class<T> clazz) throws Exception {
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
		for (Object m : arr) {
			newList.add(convert(m, clazz));
		}
		return newList;
	}

	@SuppressWarnings("unchecked")
	public <T> T convertSelf(T source) throws Exception {
		if (source == null) {
			return null;
		}
		return (T) convert(source, source.getClass());
	}
	
	public <T> void setFieldValue(T t, String field,Object value) {
		Class<?> clazz=t.getClass();
		Field f=classFieldMap.computeIfAbsent(clazz, source->{
			List<Field> fieldList = new ArrayList<>();
			Class<?> tempClass = source;
			while (tempClass != null && tempClass != Object.class) {//当父类为null的时候说明到达了最上层的父类(Object类).
			      fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
			      tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
			}
			Map<String,Field> map=new HashMap<>();
			for(Field fld:fieldList) {
				fld.setAccessible(true);
				map.put(fld.getName(), fld);
			}
			return map;
		}).get(field);
		try {
			f.set(t, convert(value,f.getType()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T convert(Object source, Class<T> clazz) throws Exception {
		if (source == null) {
			return null;
		}

		if (source instanceof java.util.List || clazz.isArray()) {
			List<Object> arr = null;
			if (clazz.isArray()) {
				arr = Arrays.asList((Object[]) source);
				Object t = Array.newInstance(clazz.getComponentType(), arr.size());
				for (int i = 0; i < arr.size(); i++) {
					Array.set(t, i, convertSelf(arr.get(i)));
				}
				return (T) t;
			} else {
				arr = (List<Object>) source;
				List<Object> newList = new ArrayList<Object>();
				for (Object m : arr) {
					newList.add(convertSelf(m));
				}
				return (T) newList;
			}
		}

		if (source instanceof java.lang.Exception) {
			return (T) source;
		}
		if (clazz.getAnnotation(IgnoreConvert.class) != null) {
			return (T) source;
		}
		if (isSimpleType(clazz)) {
			if (source instanceof java.util.Map) {
				Map<Object, Object> sourceMap = (Map<Object, Object>) source;
				Iterator<Entry<Object, Object>> iter = sourceMap.entrySet().iterator();
				if (iter.hasNext()) {
					return (T) iter.next().getValue();
				}
			}
			return (T)convertObj(source,clazz);
		} else {
			switch (clazz.getTypeName()) {
			default:
				if (source instanceof java.lang.Enum) {
					if (Enum.class.isAssignableFrom(clazz)) {
						return (T) source;
					} else {
						return (T) source.toString();
					}
				} else if (source instanceof java.util.Map) {
					Map<Object, Object> sourceMap = (Map<Object, Object>) source;
					if (java.util.Map.class.isAssignableFrom(clazz)) {
						Map<Object, Object> result = new HashMap<Object, Object>();
						for (Entry<Object, Object> entry : sourceMap.entrySet()) {
							String fieldName = String.valueOf(entry.getKey());
							if (ifMatchRuler(source, fieldName, clazz, Object.class)) {
								Object value = entry.getValue();
								result.put(getPropertyNameFromRuler(source, fieldName, clazz, Object.class),
										getPropertyValueFromRuler(source, fieldName, convertSelf(value), clazz,
												Object.class));
							}
						}
						return (T) result;
					} else {
						Field[] fields = getDeclaredFields(source.getClass(), clazz);
						Object obj = createInstance(clazz);
						for (Field field : fields) {
							String fieldName = field.getName();
							for (Entry<Object, Object> entry : sourceMap.entrySet()) {
								String sourceFieldName = String.valueOf(entry.getKey());
								if (ifMatchRuler(source, fieldName, clazz, field.getType())) {
									String newFieldName = getPropertyNameFromRuler(source, sourceFieldName, clazz,
											field.getType());
									if (fieldName.equals(newFieldName)) {
										Object value = entry.getValue();
										field.setAccessible(true);
										field.set(obj, getPropertyValueFromRuler(source, fieldName, convertSelf(value),
												clazz, field.getType()));
									}
								}
							}
						}
						return (T) obj;
					}
				} else if (java.util.Map.class.isAssignableFrom(clazz)) {
					Field[] sourceFields = getDeclaredFields(source.getClass());
					Map<Object, Object> obj = new HashMap<Object, Object>();
					for (Field sourceField : sourceFields) {
						String fieldName = sourceField.getName();
						if (ifMatchRuler(source, fieldName, clazz, Object.class)) {
							String sourceFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
							String getterSourceFieldName = "get" + sourceFieldName;
							Method method = source.getClass().getMethod(getterSourceFieldName, new Class[] {});
							Object value = method.invoke(source, new Object[] {});
							obj.put(getPropertyNameFromRuler(source, fieldName, clazz, Object.class),
									getPropertyValueFromRuler(source, fieldName, convertSelf(value), clazz,
											Object.class));
						}

					}
					return (T) obj;
				} else {
					Field[] fields = getDeclaredFields(clazz);
					Object obj = createInstance(clazz);
					for (Field field : fields) {
						String fieldName = field.getName();
						for (Field sourceField : getDeclaredFields(source.getClass())) {
							if (ifMatchRuler(source, sourceField.getName(), clazz, field.getType())) {
								if (fieldName.equals(getPropertyNameFromRuler(source, sourceField.getName(), clazz,
										field.getType()))) {
									String sourceFieldName = sourceField.getName().substring(0, 1).toUpperCase()
											+ sourceField.getName().substring(1);
									String getterSourceFieldName = "get" + sourceFieldName;
									Method method = source.getClass().getMethod(getterSourceFieldName, new Class[] {});
									Object value = method.invoke(source, new Object[] {});
									field.setAccessible(true);
									field.set(obj, convertSelf(value));
								}
							}
						}

					}
					return (T) obj;
				}
			}
		}

	}

	private Boolean isSimpleType(Class<?> clazz) {
		switch (clazz.getTypeName()) {
		case "byte":
			return true;
		case "short":
			return true;
		case "int":
			return true;
		case "long":
			return true;
		case "float":
			return true;
		case "double":
			return true;
		case "char":
			return true;
		case "boolean":
			return true;
		case "java.lang.Byte":
			return true;
		case "java.lang.Short":
			return true;
		case "java.lang.Integer":
			return true;
		case "java.lang.Long":
			return true;
		case "java.lang.Float":
			return true;
		case "java.lang.Double":
			return true;
		case "java.lang.Character":
			return true;
		case "java.lang.Boolean":
			return true;
		case "java.lang.String":
			return true;
		case "java.math.BigDecimal":
			return true;
		case "java.util.Date":
			return true;
		case "java.util.UUID":
			return true;
		case "java.sql.Timestamp":
			return true;
		default:
			return false;
		}
	}
	
	private Object convertObj(Object value,Class<?> clazz) {
		if(value==null) {
			return null;
		}
		if(value.getClass().getTypeName().equals(clazz.getTypeName())) {
			return value;
		}
		switch (clazz.getTypeName()) {
		case "byte":
			return (byte)value;
		case "short":
			return (short)value;
		case "int":
			return (int)value;
		case "long":
			return (long)value;
		case "float":
			return (float)value;
		case "double":
			return (double)value;
		case "char":
			return (char)value;
		case "boolean":
			return (boolean)value;
		case "java.lang.Byte":
			return java.lang.Byte.parseByte(value.toString());
		case "java.lang.Short":
			return java.lang.Short.parseShort(value.toString());
		case "java.lang.Integer":
			return java.lang.Integer.parseInt(value.toString());
		case "java.lang.Long":
			return java.lang.Long.parseLong(value.toString());
		case "java.lang.Float":
			return java.lang.Float.parseFloat(value.toString());
		case "java.lang.Double":
			return java.lang.Double.parseDouble(value.toString());
		case "java.lang.Character":
			return java.lang.Character.valueOf((char)value);
		case "java.lang.Boolean":
			return java.lang.Boolean.parseBoolean(value.toString());
		case "java.lang.String":
			return value.toString();
		case "java.math.BigDecimal":
			return new java.math.BigDecimal(value.toString());
		case "java.util.Date":
			return (java.util.Date)value;
		case "java.util.UUID":
			return java.util.UUID.fromString(value.toString());
		case "java.sql.Timestamp":
			return java.sql.Timestamp.valueOf(value.toString());
		default:
			return clazz.cast(value);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T createInstance(Class<T> clazz) {
		T t = null;
		try {
			if (java.util.Map.class.isAssignableFrom(clazz)) {
				t = (T) new HashMap<Object, Object>();
			} else {
				t = clazz.newInstance();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;
	}

	private Field[] getDeclaredFields(Class<?> source, Class<?> target) {
		if (java.util.Map.class.isAssignableFrom(target)) {
			if (java.util.Map.class.isAssignableFrom(source)) {
				return null;
			} else {
				return getDeclaredFields(source);
			}
		} else {
			return getDeclaredFields(target);
		}
	}

	private Field[] getDeclaredFields(Class<?> target) {
		List<Field> fieldList = new ArrayList<>();
		Class<?> tempClass = target;
		while (tempClass != null && tempClass != Object.class) {//当父类为null的时候说明到达了最上层的父类(Object类).
		      fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
		      tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
		}
		return fieldList.toArray(new Field[fieldList.size()]);
//		return classFieldMap.computeIfAbsent(target, source->{
//			List<Field> fieldList = new ArrayList<>();
//			Class<?> tempClass = source;
//			while (tempClass != null && tempClass != Object.class) {//当父类为null的时候说明到达了最上层的父类(Object类).
//			      fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
//			      tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
//			}
//			return fieldList.toArray(new Field[fieldList.size()]);
//		});
		
	}

	public IConverterRule[] getRules() {
		return rules;
	}

	public Boolean ifMatchRuler(Object sourceObj, String propertyName, Class<?> targetClazz,
			Class<?> targetPropertyType) {
		for (IConverterRule rule : this.getRules()) {
			if (!rule.shouldConvert(sourceObj, propertyName, targetClazz, targetPropertyType)) {
				return false;
			}
		}
		return true;
	}

	public String getPropertyNameFromRuler(Object sourceObj, String propertyName, Class<?> targetClazz,
			Class<?> targetPropertyType) {
		String newPropertyName = propertyName;
		for (IConverterRule rule : this.getRules()) {
			newPropertyName = rule.convertPropertyName(sourceObj, newPropertyName, targetClazz, targetPropertyType);
		}
		return newPropertyName;
	}

	public Object getPropertyValueFromRuler(Object sourceObj, String propertyName, Object value, Class<?> targetClazz,
			Class<?> targetPropertyType) {
		Object newValue = convertObj(value,targetPropertyType);
		for (IConverterRule rule : this.getRules()) {
			newValue = rule.convertPropertyValue(sourceObj, propertyName, newValue, targetClazz, targetPropertyType);
		}
		return newValue;
	}

	public void setRules(IConverterRule[] rules) {
		this.rules = Arrays.stream(rules).sorted(new Comparator<IConverterRule>() {

			@Override
			public int compare(IConverterRule a, IConverterRule b) {
				// TODO Auto-generated method stub
				return a.order() - b.order();
			}
		}).toArray(IConverterRule[]::new);
	}

	public static final IConverterRule UnderLineToCamelCaseRuler = new UnderLineToToCamelCaseRule();

	public static final IConverterRule MiddleLineToCamelCaseRuler = new MiddleLineToToCamelCaseRule();

	public static final IConverterRule CamelCaseToUnderLineRuler = new CamelCaseToUnderLineRule();

	public static final IConverterRule CamelCaseToMiddleLineRuler = new CamelCaseToMiddleLineRule();

	public static class UnderLineToToCamelCaseRule implements IConverterRule {

		@Override
		public String convertPropertyName(Object sourceObj, String propertyName, Class<?> targetClazz,
				Class<?> targetPropertyType) {
			Boolean preConverted = false;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < propertyName.length(); i++) {
				char charater = propertyName.charAt(i);
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
			return sb.length() > 0 ? sb.toString() : propertyName;
		}
	}

	public static class CamelCaseToUnderLineRule implements IConverterRule {

		@Override
		public String convertPropertyName(Object sourceObj, String propertyName, Class<?> targetClazz,
				Class<?> targetPropertyType) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < propertyName.length(); i++) {
				char charater = propertyName.charAt(i);
				if (Character.isUpperCase(charater)) {
					sb.append('_');
					sb.append(Character.toLowerCase(charater));
				} else {
					sb.append(charater);
				}
			}
			return sb.toString();
		}
	}

	public static class MiddleLineToToCamelCaseRule implements IConverterRule {

		@Override
		public String convertPropertyName(Object sourceObj, String propertyName, Class<?> targetClazz,
				Class<?> targetPropertyType) {
			Boolean preConverted = false;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < propertyName.length(); i++) {
				char charater = propertyName.charAt(i);
				if (preConverted) {
					if (charater != '-') {
						sb.append(Character.toUpperCase(charater));
						preConverted = false;
					}
				} else {
					if (charater == '-') {
						preConverted = true;
					} else {
						sb.append(charater);
					}
				}
			}
			return sb.length() > 0 ? sb.toString() : propertyName;
		}
	}

	public static class CamelCaseToMiddleLineRule implements IConverterRule {

		@Override
		public String convertPropertyName(Object sourceObj, String propertyName, Class<?> targetClazz,
				Class<?> targetPropertyType) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < propertyName.length(); i++) {
				char charater = propertyName.charAt(i);
				if (Character.isUpperCase(charater)) {
					sb.append('-');
					sb.append(Character.toLowerCase(charater));
				} else {
					sb.append(charater);
				}
			}
			return sb.toString();
		}
	}

//	public static interface IConverterRule{
//		
//		default int order() {
//			return -1;
//		}
//		
//		default Boolean shouldConvert(Object sourceObj,String propertyName,Class<?> targetClazz) {
//			return true;
//		}
//		
//		default String convertPropertyName(Object sourceObj,String propertyName,Class<?> targetClazz) {
//			return propertyName;
//		}
//		
//		default Object convertPropertyValue(Object sourceObj,String propertyName,Object value,Class<?> targetClazz) {
//			return value;
//		}
//	}

}
