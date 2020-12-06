package com.github.rexsheng.mybatis.handler;

import org.apache.ibatis.mapping.SqlCommandType;

/**
 * @author RexSheng
 * 2020年10月16日 上午12:24:09
 * @since 1.3.1
 */
public interface ISqlHandler {

	/**
	 * convert sql
	 * @param method method
	 * @param sqlCommandType sqlCommandType
	 * @param sql sql
	 * @return converted sql
	 */
	String convert(String method,SqlCommandType sqlCommandType,String sql);
}
