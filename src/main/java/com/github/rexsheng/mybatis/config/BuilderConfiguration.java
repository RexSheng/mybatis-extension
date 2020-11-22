package com.github.rexsheng.mybatis.config;

import com.github.rexsheng.mybatis.handler.DefaultColumnHandler;
import com.github.rexsheng.mybatis.handler.DefaultTableHandler;
import com.github.rexsheng.mybatis.handler.IColumnHandler;
import com.github.rexsheng.mybatis.handler.ITableHandler;

/**
 * 全局配置
 * @author RexSheng 2020年8月31日 下午10:50:28
 */
public class BuilderConfiguration {
	
	private IDatabaseDialect databaseDialect;
	
	private ITableHandler tableHandler;
	
	private IColumnHandler columnHandler;
		
	public BuilderConfiguration() {
		this(new MySqlDialect());
	}
	
	public BuilderConfiguration(IDatabaseDialect databaseDialect) {
		this.databaseDialect=databaseDialect;
		this.tableHandler=new DefaultTableHandler();
		this.columnHandler=new DefaultColumnHandler();
	}

	public ITableHandler getTableHandler() {
		return tableHandler;
	}

	public BuilderConfiguration setTableHandler(ITableHandler tableHandler) {
		this.tableHandler = tableHandler;
		return this;
	}

	public IColumnHandler getColumnHandler() {
		return columnHandler;
	}
	
	public void setColumnHandler(IColumnHandler columnHandler) {
		this.columnHandler = columnHandler;
	}
	
	public IDatabaseDialect getDatabaseDialect() {
		return databaseDialect;
	}

	public void setDatabaseDialect(IDatabaseDialect databaseDialect) {
		this.databaseDialect = databaseDialect;
	}

	@Override
	public String toString() {
		return databaseDialect.toString();
	}

}
