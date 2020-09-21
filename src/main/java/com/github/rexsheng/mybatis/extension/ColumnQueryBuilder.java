package com.github.rexsheng.mybatis.extension;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import com.github.rexsheng.mybatis.annotation.ColumnName;
import com.github.rexsheng.mybatis.config.BuilderConfiguration;
import com.github.rexsheng.mybatis.core.SqlReservedWords;
import com.github.rexsheng.mybatis.util.ReflectUtil;
import com.github.rexsheng.mybatis.util.StringUtils;

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
			String col=null;
			if(field!=null) {
				ColumnName columnName=field.getAnnotation(ColumnName.class);
				if(columnName!=null) {
					col=columnName.value();
				}
				else {
					col=StringUtils.camelCaseToUnderLine(getFieldName());
				}
			}
			else {
				col=StringUtils.camelCaseToUnderLine(getFieldName());
			}
			if(SqlReservedWords.containsWord(col)) {
				return configuration.getBeginDelimiter()+col+configuration.getEndDelimiter();
			}
			else {
				return col;
			}
		}
		else {
			return inputColumnName;
		}
	}

	public Field getField() {
		return field;
	}
}
