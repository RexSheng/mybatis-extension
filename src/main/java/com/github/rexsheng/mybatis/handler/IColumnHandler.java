package com.github.rexsheng.mybatis.handler;

import com.github.rexsheng.mybatis.config.BuilderConfiguration;
import com.github.rexsheng.mybatis.extension.ColumnQueryBuilder;

/**
 * @author RexSheng
 * 2020年10月16日 上午12:24:09
 * @since 1.2.0
 */
public interface IColumnHandler {

	/**
	 * 获取列名
	 * @param columnBuilder 构造条件
	 * @param configuration 配置
	 * @return 列名
	 */
	String getName(ColumnQueryBuilder<?> columnBuilder,BuilderConfiguration configuration);
	
	/**
	 * 是否主键
	 * @param columnBuilder 构造条件
	 * @param configuration 配置
	 * @return 是否主键
	 * @since 1.4.0
	 */
	Boolean isPrimaryKey(ColumnQueryBuilder<?> columnBuilder,BuilderConfiguration configuration);
	
}
