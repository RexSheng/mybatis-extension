package com.github.rexsheng.mybatis.handler;

import com.github.rexsheng.mybatis.config.BuilderConfiguration;

/**
 * @author RexSheng
 * 2020年10月16日 上午12:24:09
 * @since 1.2.0
 */
public interface ITableHandler {

	/**
	 * 获取表名
	 * @param clazz 表实体类型
	 * @param configuration 配置
	 * @return 表名
	 */
	String getName(Class<?> clazz,BuilderConfiguration configuration);
	
}
