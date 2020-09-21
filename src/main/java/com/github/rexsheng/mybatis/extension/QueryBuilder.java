package com.github.rexsheng.mybatis.extension;

import com.github.rexsheng.mybatis.config.BuilderConfiguration;

/**
 * @author RexSheng
 * 2020年8月27日 下午8:07:06
 */
public class QueryBuilder<T> {

	private Class<T> outputClazz;
	
	private TableQueryBuilder<?> table;
	
	private BuilderConfiguration buiderConfig; 
	
	public QueryBuilder(TableQueryBuilder<T> table) {
		this.table=table;
		this.outputClazz=table.getEntityClass();
	}
	
	public <S> QueryBuilder(TableQueryBuilder<S> table,Class<T> outputClazz) {
		this.table=table;
		this.outputClazz=outputClazz;
	}

	public Class<T> getOutputClazz() {
		return outputClazz;
	}

	public TableQueryBuilder<?> getTable() {
		return table;
	}
	
	public BuilderConfiguration getBuiderConfig() {
		return buiderConfig;
	}

	public void setBuiderConfig(BuilderConfiguration buiderConfig) {
		this.buiderConfig = buiderConfig;
	}
	
}
