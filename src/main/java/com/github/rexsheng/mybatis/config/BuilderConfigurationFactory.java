package com.github.rexsheng.mybatis.config;

import com.github.rexsheng.mybatis.handler.IColumnHandler;
import com.github.rexsheng.mybatis.handler.ITableHandler;

/**
 * BuilderConfiguration构造器
 * @author RexSheng 2020年8月31日 下午10:50:28
 * @since 1.3.0
 */
public class BuilderConfigurationFactory {

	private static BuilderConfigurationFactory _instance;

	private BuilderConfigurationFactory.Builder builder;

	private static Object lockObj = new Object();

	private static BuilderConfigurationFactory getInstance() {
		if (_instance == null) {
			synchronized (lockObj) {
				if (_instance == null) {
					_instance = new BuilderConfigurationFactory();
				}
			}
		}
		return _instance;
	}

	public static Builder builder() {
		return getInstance().getBuilder();
	}

	public BuilderConfigurationFactory.Builder getBuilder() {
		if (this.builder == null) {
			this.builder = new Builder();
		}
		return this.builder;
	}

	public static class Builder {
		private IDatabaseDialect databaseDialect;
		
		private ITableHandler tableHandler;
		
		private IColumnHandler columnHandler;

		public Builder dialect(IDatabaseDialect databaseDialect) {
			this.databaseDialect = databaseDialect;
			return this;
		}

		public Builder tableHandler(ITableHandler tableHandler) {
			this.tableHandler = tableHandler;
			return this;
		}

		public Builder columnHandler(IColumnHandler columnHandler) {
			this.columnHandler = columnHandler;
			return this;
		}
		
		public BuilderConfiguration build() {
			BuilderConfiguration config=new BuilderConfiguration();
			if(databaseDialect!=null) {
				config.setDatabaseDialect(databaseDialect);
			}
			if(tableHandler!=null) {
				config.setTableHandler(tableHandler);
			}
			if(columnHandler!=null) {
				config.setColumnHandler(columnHandler);
			}
			return config;
		}
	}

}
