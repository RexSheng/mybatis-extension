package com.github.rexsheng.mybatis.config;

import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;

import com.github.rexsheng.mybatis.extension.QueryBuilder;

/**
 * @author RexSheng 2020年11月20日 下午10:57:24
 * @since 1.3.0
 */
public class MySqlDialect extends DefaultDatabaseDialect {

	public MySqlDialect() {
		DialectProperty databaseProperty = new DialectProperty();
		databaseProperty.setDbType("mysql");
		databaseProperty.setBeginDelimiter("`");
		databaseProperty.setEndDelimiter("`");
		setProperty(databaseProperty);
	}

	public MySqlDialect(DialectProperty databaseProperty) {
		setProperty(databaseProperty);
	}

	@Override
	public String generatePaginationSql(String selectSql, List<ParameterMapping> parameterMappingList,
			BoundSql boundSql, MappedStatement ms, QueryBuilder<?> queryBuilder) {
		String additionalSql = selectSql;
		if (queryBuilder.getTable().getPageSize() != null) {
			additionalSql += " LIMIT ?";//$NON-NLS-1$
			parameterMappingList.add(createNewParameterMapping(ms, "table.pageSize", java.lang.Integer.class));//$NON-NLS-1$
		}
		if (queryBuilder.getTable().getSkipSize() != null) {
			additionalSql += " OFFSET ?";//$NON-NLS-1$
			parameterMappingList.add(createNewParameterMapping(ms, "table.skipSize", java.lang.Integer.class));//$NON-NLS-1$
		}
		return additionalSql;
	}
}
