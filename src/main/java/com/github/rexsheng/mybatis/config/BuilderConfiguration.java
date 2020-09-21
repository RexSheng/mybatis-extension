package com.github.rexsheng.mybatis.config;

import java.util.function.Function;

import com.github.rexsheng.mybatis.annotation.TableName;
import com.github.rexsheng.mybatis.util.StringUtils;

/**
 * @author RexSheng 2020年8月31日 下午10:50:28
 */
public class BuilderConfiguration {

	private String beginDelimiter;

	private String endDelimiter;

	private Function<Class<?>,String> tableNameHandler;

	public BuilderConfiguration() {
		this.beginDelimiter="";
		this.endDelimiter="";
		this.tableNameHandler=(clazz)->{
			TableName tableName=clazz.getAnnotation(TableName.class);
			if(tableName!=null) {
				return tableName.value();
			}
			else {
				return StringUtils.capitalToUnderLine(clazz.getSimpleName());
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
	
	@Override
	public String toString() {
		return "{beginDelimiter=" + beginDelimiter + ", endDelimiter=" + endDelimiter + "}";
	}

}
