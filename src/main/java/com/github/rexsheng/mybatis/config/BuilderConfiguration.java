package com.github.rexsheng.mybatis.config;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.github.rexsheng.mybatis.annotation.ColumnName;
import com.github.rexsheng.mybatis.annotation.TableName;
import com.github.rexsheng.mybatis.core.SqlReservedWords;
import com.github.rexsheng.mybatis.extension.ColumnQueryBuilder;
import com.github.rexsheng.mybatis.util.StringUtils;

/**
 * 全局配置
 * @author RexSheng 2020年8月31日 下午10:50:28
 */
public class BuilderConfiguration {

	private String beginDelimiter;

	private String endDelimiter;
	
	private String dbType;
	
	private Function<Class<?>,String> tableNameHandler;
	
	private Function<ColumnQueryBuilder<?>,String> columnNameHandler;
	
	private final List<String> DB_TYPE_LIST= Arrays.asList("mysql","oracle","sqlserver");

	public BuilderConfiguration() {
		this.beginDelimiter="`";
		this.endDelimiter="`";
		this.dbType="mysql";
		this.tableNameHandler=(clazz)->{
			TableName tableName=clazz.getAnnotation(TableName.class);
			if(tableName!=null) {
				return tableName.value();
			}
			else {
				return StringUtils.capitalToUnderLine(clazz.getSimpleName());
			}
		};
		this.columnNameHandler=(query)->{
			String col=null;
			if(query.getField()!=null) {
				ColumnName columnName=query.getField().getAnnotation(ColumnName.class);
				if(columnName!=null) {
					col=columnName.value();
				}
				else {
					col=StringUtils.camelCaseToUnderLine(query.getFieldName());
				}
			}
			else {
				col=StringUtils.camelCaseToUnderLine(query.getFieldName());
			}
			if(SqlReservedWords.containsWord(col)) {
				return getBeginDelimiter()+col+getEndDelimiter();
			}
			else {
				return col;
			}
		};
	}

	public String getBeginDelimiter() {
		return beginDelimiter;
	}

	public void setBeginDelimiter(String beginDelimiter) {
		this.beginDelimiter = beginDelimiter;
	}

	public String getEndDelimiter() {
		return endDelimiter;
	}

	public void setEndDelimiter(String endDelimiter) {
		this.endDelimiter = endDelimiter;
	}
	
	public Function<Class<?>,String> getTableNameHandler() {
		return tableNameHandler;
	}

	public void setTableNameHandler(Function<Class<?>,String> tableNameHandler) {
		this.tableNameHandler = tableNameHandler;
	}
	
	public Function<ColumnQueryBuilder<?>, String> getColumnNameHandler() {
		return columnNameHandler;
	}
	
	public void setColumnNameHandler(Function<ColumnQueryBuilder<?>, String> columnNameHandler) {
		this.columnNameHandler = columnNameHandler;
	}

	public String getDbType() {
		return dbType;
	}

	/**
	 * 设置数据库类型，默认mysql,目前支持"mysql","oracle","sqlserver"三种
	 * @param dbType 数据库类型
	 * @since 1.1.0
	 */
	public void setDbType(String dbType) {
		if(dbType==null || !DB_TYPE_LIST.contains(dbType.toLowerCase())) {
			throw new RuntimeException("无效的数据库类型，请指定为"+String.join("、", DB_TYPE_LIST)+"中的一种");
		}
		this.dbType = dbType;
	}
	
	@Override
	public String toString() {
		return "{beginDelimiter=" + beginDelimiter + ", endDelimiter=" + endDelimiter + ", dbType=" + dbType + "}";
	}
	
	

}
