package com.github.rexsheng.mybatis.extension;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import com.github.rexsheng.mybatis.config.BuilderConfiguration;
import com.github.rexsheng.mybatis.util.ReflectUtil;

/**
 * @author RexSheng 2020年8月27日 下午8:07:18
 */
public class ColumnQueryBuilder<T> extends EntityInfo<T>{
	
	private String inputColumnName;
	
	private String columnName;
	
	private String fieldName;
	
	private Field field;
		
	private String prefix;
	
	private String suffix;
	
	private Boolean supportAlias;
	
	public ColumnQueryBuilder(Class<T> clazz,String fieldName) {
		this(clazz,fieldName,null);
	}
	
	public ColumnQueryBuilder(Class<T> clazz,String fieldName,String columnName) {
		this(clazz,fieldName,columnName,null);
	}
	
	public ColumnQueryBuilder(Class<T> clazz,String fieldName,String columnName,String inputColumnName) {
		super(clazz);
		this.fieldName=fieldName;
		this.columnName=columnName;
		this.inputColumnName=inputColumnName;
		this.prefix="";
		this.suffix="";
		if("*".equals(fieldName) || Pattern.compile("[0-9]*").matcher(fieldName).matches()) {
			this.supportAlias=false;
		}
		else {
			this.supportAlias=true;
			this.field=ReflectUtil.getClassField(clazz, fieldName);
		}
	}
	
	/**
	 * 用于批量新增时
	 * @param clazz 实体类型
	 * @param field 字段类型
	 */
	public ColumnQueryBuilder(Class<T> clazz,Field field) {
		super(clazz);
		this.fieldName=field.getName();
		this.field=field;
	}
	 

	public String getColumnName() {
		if(columnName==null) {
			return getFieldName();
		}
		return columnName;
	}

	public String getFieldName() {
		return fieldName;
	}
	
	public String buildSql(BuilderConfiguration configuration) {
		return this.prefix+getActualColumnName(configuration)+this.suffix+" AS "+this.getColumnName();
	}
	
	public String buildSql(BuilderConfiguration configuration,String tableAlias) {
		return this.prefix+(supportAlias?(tableAlias+"."):"")+getActualColumnName(configuration)+this.suffix+" AS "+this.getColumnName();
	}
	
	public String buildSqlNoAs(BuilderConfiguration configuration) {
		return this.prefix+getActualColumnName(configuration)+this.suffix;
	}
	
	public String buildSqlNoAs(BuilderConfiguration configuration,String tableAlias) {
		return this.prefix+(supportAlias?(tableAlias+"."):"")+getActualColumnName(configuration)+this.suffix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public void setSupportAlias(Boolean supportAlias) {
		this.supportAlias = supportAlias;
	}

	public String getInputColumnName() {
		return inputColumnName;
	}
	
	private String getActualColumnName(BuilderConfiguration configuration) {
		if(inputColumnName==null) {
			return configuration.getColumnNameHandler().apply(this);
		}
		else {
			return inputColumnName;
		}
	}

	public Field getField() {
		return field;
	}
}
