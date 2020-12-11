package com.github.rexsheng.mybatis.factory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rexsheng.mybatis.extension.ColumnQueryBuilder;
import com.github.rexsheng.mybatis.extension.QueryBuilder;
import com.github.rexsheng.mybatis.extension.TableQueryBuilder.JoinTableConditionInternal;
import com.github.rexsheng.mybatis.util.ReflectUtil;

/**
 * @author RexSheng
 * 2020年12月7日 上午12:23:04
 */
public class ResultMappingFactory {
	
	private static Logger logger=LoggerFactory.getLogger(ResultMappingFactory.class);
	
	public static List<ResultMapping> getColumnMapping(MappedStatement ms,QueryBuilder<?> queryBuilder){
		List<ResultMapping> resultMappingList=new ArrayList<>();
		List<ColumnQueryBuilder<?>> columns=new ArrayList<>();
		for(ColumnQueryBuilder<?> column:queryBuilder.getTable().getSelectColumns()) {
			columns.add(column);			
		}
		for(JoinTableConditionInternal<?, ?> join:queryBuilder.getTable().getJoinList()) {
			for(ColumnQueryBuilder<?> column:join.getTable().getSelectColumns()) {
				columns.add(column);			
			}
			for(JoinTableConditionInternal<?, ?> join2:join.getTable().getJoinList()) {
				for(ColumnQueryBuilder<?> column:join2.getTable().getSelectColumns()) {
					columns.add(column);			
				}			
			}
		}
		
		List<String> outputFieldNameList = new ArrayList<>();
		for(Field field:ReflectUtil.getDeclaredFields(queryBuilder.getOutputClazz())) {
			outputFieldNameList.add(field.getName());
		}		
		for(ColumnQueryBuilder<?> column:columns) {
			try {
				String propertyName=column.getPropertyName();
				String columnName=column.getColumnName(queryBuilder.getBuiderConfig());

				Field field = ReflectUtil.getClassField(column.getEntityClass(),propertyName);
				if(field==null) {
					continue;
				}
				//检查返回值类中是否有sql中的字段, DefaultResultSetHandler#applyPropertyMappings==》if (property == null) {continue;}
				if(!outputFieldNameList.contains(propertyName)) {
					propertyName=null;
				}
				
				ResultMapping.Builder builder=new ResultMapping.Builder(ms.getConfiguration(), propertyName,columnName,field.getType());
//				builder.jdbcType(queryBuilder.getBuiderConfig().getDatabaseDialect().getDefaultJdbcType(field.getType()));
				Boolean isPk=queryBuilder.getBuiderConfig().getColumnHandler().isPrimaryKey(column, queryBuilder.getBuiderConfig());
				if(isPk) {
					builder.flags(Arrays.asList(ResultFlag.ID));
				}
				
				ResultMapping resultMapping=builder.build();
				if(logger.isDebugEnabled()) {
					logger.debug(resultMapping.toString());
				}
				resultMappingList.add(resultMapping);
			} catch (Exception e) {
				e.printStackTrace();
				//continue
			}
			
		}
		return resultMappingList;
	}
}
