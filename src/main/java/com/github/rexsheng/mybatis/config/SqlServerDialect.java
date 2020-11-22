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
public class SqlServerDialect extends DefaultDatabaseDialect{
	
	public SqlServerDialect() {
		DialectProperty databaseProperty=new DialectProperty();
		databaseProperty.setDbType("sqlserver");
		databaseProperty.setBeginDelimiter("[");
		databaseProperty.setEndDelimiter("]");
		setProperty(databaseProperty);
	}
	
	public SqlServerDialect(DialectProperty databaseProperty) {
		setProperty(databaseProperty);
	}	

	@Override
	public String generatePaginationSql(String selectSql,List<ParameterMapping> parameterMappingList, 
			BoundSql boundSql, MappedStatement ms, QueryBuilder<?> queryBuilder) {
		String additionalSql=selectSql;
		if(queryBuilder.getTable().getPageSize()!=null) {
			if(queryBuilder.getTable().getSkipSize()!=null) {
				additionalSql+=" OFFSET ? ROWS";//$NON-NLS-1$
				parameterMappingList.add(createNewParameterMapping(ms,"table.skipSize",java.lang.Integer.class));//$NON-NLS-1$
			}
    		else {
    			additionalSql+=" OFFSET 0 ROWS";//$NON-NLS-1$
    		}
			additionalSql+=" FETCH NEXT ? ROWS ONLY";//$NON-NLS-1$
			parameterMappingList.add(createNewParameterMapping(ms,"table.pageSize",java.lang.Integer.class));//$NON-NLS-1$
		}
		return additionalSql;
	}
}
