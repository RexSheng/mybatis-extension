package com.github.rexsheng.mybatis.extension;

/**
 * @author RexSheng
 * 2020年10月13日 下午11:55:12
 */
public class TableDeleteBuilder<T> extends TableQueryBuilder<T> {
	
	public TableDeleteBuilder(Class<T> clazz) {
		super(clazz);
	}
	
	public TableDeleteBuilder(Class<T> clazz, String tableName) {
		super(clazz, tableName);
	}
	
	public static <T> TableDeleteBuilder<T> from(Class<T> clazz){
		return new TableDeleteBuilder<T>(clazz);
	}
	
	public static <T> TableDeleteBuilder<T> from(Class<T> clazz,String tableName){
		return new TableDeleteBuilder<T>(clazz,tableName);
	}	
}
