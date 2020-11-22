package com.github.rexsheng.mybatis.config;

import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;

import com.github.rexsheng.mybatis.extension.QueryBuilder;

/**
 * @author RexSheng
 * 2020年11月20日 下午10:57:24
 * @since 1.3.0
 */
public interface IDatabaseDialect {

	DialectProperty getProperty();
	
	String generateCountSql(String selectSql, Object parameterObject, BoundSql boundSql, MappedStatement ms, QueryBuilder<?> queryBuilder);
	
	String generatePaginationSql(String selectSql, List<ParameterMapping> parameterMappingList, BoundSql boundSql, MappedStatement ms, QueryBuilder<?> queryBuilder);
	
	/**
	 * 分页查询时，当总条数count为0时，跳过执行原有select查询，默认true：不继续执行，利于提高性能
	 * @return 是否跳过原有查询
	 */
	Boolean skipSelectIfCountZero();
}
