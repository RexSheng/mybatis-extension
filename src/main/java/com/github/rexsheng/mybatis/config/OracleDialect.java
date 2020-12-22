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
public class OracleDialect extends DefaultDatabaseDialect{
	
	public OracleDialect() {
		DialectProperty databaseProperty=new DialectProperty();
		databaseProperty.setDbType("oracle");
		databaseProperty.setBeginDelimiter("\"");
		databaseProperty.setEndDelimiter("\"");
		setProperty(databaseProperty);
	}
	
	public OracleDialect(DialectProperty databaseProperty) {
		setProperty(databaseProperty);
	}

	@Override
	public String generatePaginationSql(String selectSql,List<ParameterMapping> parameterMappingList, 
			BoundSql boundSql, MappedStatement ms, QueryBuilder<?> queryBuilder) {
		String additionalSql=selectSql;
		if(queryBuilder.getTable().getEndIndex()!=null) {
			additionalSql="SELECT tt.*,ROWNUM AS extension_rowno FROM ("+additionalSql+") tt WHERE ROWNUM<= ? ";//$NON-NLS-1$
			parameterMappingList.add(createNewParameterMapping(ms,"table.endIndex",java.lang.Integer.class));//$NON-NLS-1$							
		}
		if(queryBuilder.getTable().getStartIndex()!=null) {
			additionalSql="SELECT * FROM ("+additionalSql+") t WHERE t.extension_rowno> ?";//$NON-NLS-1$
			parameterMappingList.add(createNewParameterMapping(ms,"table.startIndex",java.lang.Integer.class));//$NON-NLS-1$
		}
		return additionalSql;
	}
}
