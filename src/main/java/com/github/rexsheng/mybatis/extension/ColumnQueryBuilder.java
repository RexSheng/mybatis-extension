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
	
	private String aliasName;
	
	private String fieldName;
	
	private Field field;
		
	private String prefix;
	
	private String suffix;
	
	private Boolean supportAlias;
	
	public ColumnQueryBuilder(Class<T> clazz,String fieldName) {
		this(clazz,fieldName,null);
	}
	
	public ColumnQueryBuilder(Class<T> clazz,String fieldName,String aliasName) {
		this(clazz,fieldName,aliasName,null);
	}
	
	public ColumnQueryBuilder(Class<T> clazz,String fieldName,String aliasName,String inputColumnName) {
		super(clazz);
		this.fieldName=fieldName;
		this.aliasName=aliasName;
		this.inputColumnName=inputColumnName;
		this.prefix="";
		this.suffix="";
		if(fieldName!=null) {
			if("*".equals(fieldName) || Pattern.compile("[0-9]*").matcher(fieldName).matches()) {//$NON-NLS-1$
				this.supportAlias=false;
			}
			else {
				this.supportAlias=true;
				this.field=ReflectUtil.getClassField(clazz, fieldName);
			}
		}
		else {
			if(inputColumnName!=null && ("*".equals(inputColumnName) || Pattern.compile("[0-9]*").matcher(inputColumnName).matches())) {//$NON-NLS-1$
				this.supportAlias=false;
			}
			else {
				this.supportAlias=true;
			}
			
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
	 

	public String getAliasName() {
		return aliasName;
	}

	public String getFieldName() {
		return fieldName;
	}
	
	public String buildSql(BuilderConfiguration configuration) {
		String actualName=this.prefix+getActualColumnName(configuration)+this.suffix;
		if(aliasName==null) {
			return actualName;
		}
		else {
			if(actualName.equals(aliasName)) {
				return actualName;
			}
			else {
				return actualName+" AS "+aliasName;//$NON-NLS-1$
			}
//			String fieldName=getFieldName();
//			if(SqlReservedWords.containsWord(fieldName)) {
//				return configuration.getDatabaseDialect().getProperty().getBeginDelimiter()+fieldName+configuration.getDatabaseDialect().getProperty().getEndDelimiter();
//			}
//			else {
//				return fieldName;
//			}
		}	
	}
	
	public String buildSql(BuilderConfiguration configuration,String tableAlias) {
		String actualName=getActualColumnName(configuration);
		String aliasName=getAliasName();
		if(aliasName==null) {
			return this.prefix+(supportAlias?(tableAlias+"."):"")+actualName+this.suffix;//$NON-NLS-1$
		}
		else {
			if((this.prefix+actualName+this.suffix).equals(aliasName)) {
				return this.prefix+(supportAlias?(tableAlias+"."):"")+actualName+this.suffix;//$NON-NLS-1$
			}
			else {
				return this.prefix+(supportAlias?(tableAlias+"."):"")+actualName+this.suffix+" AS "+aliasName;//$NON-NLS-1$
			}
		}	
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
			return configuration.getColumnHandler().getName(this, configuration);
		}
		else {
			return inputColumnName;
		}
	}
	
	public String getPropertyName() {
		if(aliasName!=null) {
			return aliasName;
		}
		return fieldName;
	}
	
	public String getColumnName(BuilderConfiguration configuration) {
		String columnName=aliasName!=null?aliasName:configuration.getColumnHandler().getName(this, configuration);
		if(configuration.getDatabaseDialect().getProperty().getBeginDelimiter()!=null) {
			columnName=columnName.replace(configuration.getDatabaseDialect().getProperty().getBeginDelimiter(), "");
		}
		if(configuration.getDatabaseDialect().getProperty().getEndDelimiter()!=null) {
			columnName=columnName.replace(configuration.getDatabaseDialect().getProperty().getEndDelimiter(), "");
		}
		return columnName;
	}

	public Field getField() {
		return field;
	}
}
