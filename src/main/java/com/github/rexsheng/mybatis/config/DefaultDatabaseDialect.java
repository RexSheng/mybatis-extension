package com.github.rexsheng.mybatis.config;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;

import com.github.rexsheng.mybatis.extension.QueryBuilder;
import com.github.rexsheng.mybatis.util.StringUtils;

/**
 * @author RexSheng
 * 2020年11月20日 下午10:57:24
 * @since 1.3.0
 */
public abstract class DefaultDatabaseDialect implements IDatabaseDialect{

	protected DialectProperty property;
	
	protected ParameterMapping createNewParameterMapping(MappedStatement mappedStatement,String name,Class<?> javaType) {
		ParameterMapping.Builder builder=new ParameterMapping.Builder(mappedStatement.getConfiguration(),name, javaType);
		return builder.build();
	}
	
	@Override
	public String generateCountSql(String selectSql, Object parameterObject, BoundSql boundSql, MappedStatement ms, QueryBuilder<?> queryBuilder) {		
		if(!queryBuilder.getTable().getGroupByColumns().isEmpty() 
				|| queryBuilder.getTable().getJoinList().stream().anyMatch(a->!a.getTable().getGroupByColumns().isEmpty())
				|| queryBuilder.getTable().getDistinct()
		) {
    		//此处使用外部count方法处理
			return "SELECT COUNT(*) FROM ("
    				+StringUtils.replaceSuffix(StringUtils.removeBreakingWhitespace(selectSql)," ORDER BY ","")
					+") a";
		}
		else {
			//这里会替换FROM之前语句为SELECT COUNT(*)
			String countSql=StringUtils.removeBreakingWhitespace(selectSql);
			countSql=StringUtils.replacePrefix(countSql, " FROM ", "SELECT COUNT(*) FROM ");
			countSql=StringUtils.replaceSuffix(countSql, " ORDER BY ","");
			return countSql;
		}
	}
	
	@Override
	public Boolean skipSelectIfCountZero() {
		return Boolean.TRUE;
	}
	
	@Override
	public DialectProperty getProperty() {
		return property;
	}

	protected void setProperty(DialectProperty property) {
		this.property = property;
	}
	
	@Override
	public String toString() {
		return property.toString();
	}
}
