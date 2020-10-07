package com.github.rexsheng.mybatis.provider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rexsheng.mybatis.config.BuilderConfiguration;
import com.github.rexsheng.mybatis.extension.ColumnQueryBuilder;
import com.github.rexsheng.mybatis.extension.ConditionBuilder;
import com.github.rexsheng.mybatis.extension.QueryBuilder;
import com.github.rexsheng.mybatis.extension.TableQueryBuilder;
import com.github.rexsheng.mybatis.extension.WhereConditionBuilder;
import com.github.rexsheng.mybatis.extension.JoinConditionBuilder.JoinColumnsInternal;
import com.github.rexsheng.mybatis.extension.TableQueryBuilder.JoinTableConditionInternal;
import com.github.rexsheng.mybatis.util.ReflectUtil;

/**
 * @author RexSheng
 * 2020年8月28日 下午3:44:11
 */
public class DynamicSqlProvider {
	
	private Logger logger=LoggerFactory.getLogger(DynamicSqlProvider.class); 
	
	private Map<Class<?>,String> aliasMap=new HashMap<>();
	
	
	public <T> String selectByBuilder(QueryBuilder<T> builder) {
		return new SQL(){{
			BuilderConfiguration configuration=builder.getBuiderConfig();
			TableQueryBuilder<?> sourceTable=builder.getTable();
			Boolean singleTable=sourceTable.getJoinList().isEmpty();
			for(ColumnQueryBuilder<?> column:sourceTable.getSelectColumns()) {
				if(singleTable) {
					SELECT(column.buildSql(configuration));
				}
				else {
					SELECT(column.buildSql(configuration,getTableAlias(column.getEntityClass())));				
				}
			}
			for(ColumnQueryBuilder<?> column:sourceTable.getGroupByColumns()) {
				if(singleTable) {
					GROUP_BY(column.buildSqlNoAs(configuration));
				}
				else {
					GROUP_BY(column.buildSqlNoAs(configuration,getTableAlias(column.getEntityClass())));
				}
			}
			for(ColumnQueryBuilder<?> column:sourceTable.getOrderByColumns()) {
				if(singleTable) {
					ORDER_BY(column.buildSqlNoAs(configuration));
				}
				else {
					ORDER_BY(column.buildSqlNoAs(configuration,getTableAlias(column.getEntityClass())));
				}
			}
			if(singleTable) {
				FROM(getTableName(configuration,sourceTable));
			}
			else {
				FROM(getTableName(configuration,sourceTable)+" AS "+getTableAlias(sourceTable.getEntityClass()));
			}
			
			//子级join关联列表
			List<JoinTableConditionInternal<?, ?>> secondJoinList=new ArrayList<>();
			List<Class<?>> secondJoinClassList=new ArrayList<>();
			for(JoinTableConditionInternal<?, ?> entry:sourceTable.getJoinList()) {
				secondJoinList.addAll(entry.getTable().getJoinList());
				secondJoinClassList.add(entry.getTable().getEntityClass());
			}
			
			for(JoinTableConditionInternal<?, ?> entry:sourceTable.getJoinList()) {
				for(ColumnQueryBuilder<?> column:entry.getTable().getSelectColumns()) {
					SELECT(column.buildSql(configuration,getTableAlias(column.getEntityClass())));
				}
				for(ColumnQueryBuilder<?> column:entry.getTable().getGroupByColumns()) {
					GROUP_BY(column.buildSqlNoAs(configuration,getTableAlias(column.getEntityClass())));
				}
				for(ColumnQueryBuilder<?> column:entry.getTable().getOrderByColumns()) {
					ORDER_BY(column.buildSqlNoAs(configuration,getTableAlias(column.getEntityClass())));
				}
				List<String> onConditions=new ArrayList<>();
				Iterator<JoinTableConditionInternal<?, ?>> iterator = secondJoinList.iterator();
				while(iterator.hasNext()) {
					JoinTableConditionInternal<?, ?> secondJoinEntry=iterator.next();
					if(secondJoinEntry.getTable().getEntityClass().equals(entry.getTable().getEntityClass())) {
						//外部关联了，内部表又被关联一次，A join B on A.id=B.id Join C on A.id=C.id and C.name=B.name
						for(JoinColumnsInternal<?,?> internalOn:secondJoinEntry.getCondtion().getConditions()) {
							String leftAlias=getTableAlias(secondJoinEntry.getCondtion().getEntityClass());
							String rightAlias=getTableAlias(secondJoinEntry.getCondtion().getRightClazz());
							onConditions.add(internalOn.getLeftColumn().buildSqlNoAs(configuration,leftAlias)+internalOn.getRelation()+internalOn.getRightColumn().buildSqlNoAs(configuration,rightAlias));
						}
						iterator.remove();
					}
				}
				
				for(JoinColumnsInternal<?,?> internalOn:entry.getCondtion().getConditions()) {
					String leftAlias=getTableAlias(entry.getCondtion().getEntityClass());
					String rightAlias=getTableAlias(entry.getCondtion().getRightClazz());
					onConditions.add(internalOn.getLeftColumn().buildSqlNoAs(configuration,leftAlias)+internalOn.getRelation()+internalOn.getRightColumn().buildSqlNoAs(configuration,rightAlias));
				}
				String onConditionsStr="";
				if(!onConditions.isEmpty()) {
					onConditionsStr=" on "+String.join(" and ", onConditions);
				}
				else {
					logger.error("join关联后必须使用on条件");
					throw new RuntimeException("join关联后必须使用on条件");
				}
				
				if(entry.getJoinType().equals("")) {
					JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
					
				}
				else if(entry.getJoinType().equals("left")) {
					LEFT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("right")) {
					RIGHT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("inner")) {
					INNER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				
			}
			
			Iterator<JoinTableConditionInternal<?, ?>> iterator = secondJoinList.iterator();
			while(iterator.hasNext()) {
				JoinTableConditionInternal<?, ?> entry=iterator.next();
				for(ColumnQueryBuilder<?> column:entry.getTable().getSelectColumns()) {
					SELECT(column.buildSql(configuration,getTableAlias(column.getEntityClass())));
				}
				for(ColumnQueryBuilder<?> column:entry.getTable().getGroupByColumns()) {
					GROUP_BY(column.buildSqlNoAs(configuration,getTableAlias(column.getEntityClass())));
				}
				for(ColumnQueryBuilder<?> column:entry.getTable().getOrderByColumns()) {
					ORDER_BY(column.buildSqlNoAs(configuration,getTableAlias(column.getEntityClass())));
				}
				List<String> onConditions=new ArrayList<>();
				for(JoinColumnsInternal<?,?> internalOn:entry.getCondtion().getConditions()) {
					String leftAlias=getTableAlias(entry.getCondtion().getEntityClass());
					String rightAlias=getTableAlias(entry.getCondtion().getRightClazz());
					onConditions.add(internalOn.getLeftColumn().buildSqlNoAs(configuration,leftAlias)+internalOn.getRelation()+internalOn.getRightColumn().buildSqlNoAs(configuration,rightAlias));
				}
				String onConditionsStr="";
				if(!onConditions.isEmpty()) {
					onConditionsStr=" on "+String.join(" and ", onConditions);
				}
				else {
					logger.error("join关联后必须使用on条件");
					throw new RuntimeException("join关联后必须使用on条件");
				}
				
				if(entry.getJoinType().equals("")) {
					JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
					
				}
				else if(entry.getJoinType().equals("left")) {
					LEFT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("right")) {
					RIGHT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("inner")) {
					INNER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
			}
			
			
			//where条件
			int i=0;
			for(ConditionBuilder<?> condition:sourceTable.getConditions()) {
				int j=0;
				List<String> whereList=new ArrayList<>();
				for(WhereConditionBuilder<?> whereCondition:condition.getWhereConditions()) {
					String left=null;
					if(singleTable) {
						left=whereCondition.getColumn().buildSqlNoAs(configuration);
					}
					else {
						left=whereCondition.getColumn().buildSqlNoAs(configuration,getTableAlias(whereCondition.getColumn().getEntityClass()));
					}
					if(whereCondition.getHasValue()) {
						if(whereCondition.getListValue()) {
							List<String> listXml=new ArrayList<>();
							for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
								listXml.add("#{table.conditions["+i+"].whereConditions["+j+"].value["+listIndex+"]}");
							}
							whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");
						}
						else {
							whereList.add(left+" "+whereCondition.getRelation()+" #{table.conditions["+i+"].whereConditions["+j+"].value}");
						}
					}
					else {
						whereList.add(left+" "+whereCondition.getRelation());
					}
					j++;
				}
				if(!whereList.isEmpty()) {
					if(condition.getIsAnd()) {
						AND();
						WHERE(whereList.toArray(new String[whereList.size()]));
					}
					else {
						AND();
						WHERE(String.join(" OR ", whereList));
					}
					
				}
				i++;
			}
			
			i=0;
			for(JoinTableConditionInternal<?, ?> join:sourceTable.getJoinList()) {
				int j=0;
				for(ConditionBuilder<?> condition:join.getTable().getConditions()) {
					List<String> whereList=new ArrayList<>();
					int k=0;
					for(WhereConditionBuilder<?> whereCondition:condition.getWhereConditions()) {
						if(k==0) {
							AND();
						}
						String left=null;
						if(singleTable) {
							left=whereCondition.getColumn().buildSqlNoAs(configuration);
						}
						else {
							left=whereCondition.getColumn().buildSqlNoAs(configuration,getTableAlias(whereCondition.getColumn().getEntityClass()));
						}
						if(whereCondition.getHasValue()) {
							if(whereCondition.getListValue()) {
								List<String> listXml=new ArrayList<>();
								for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
									listXml.add("#{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value["+listIndex+"]}");
								}
								whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value}");
							}
						}
						else {
							whereList.add(left+" "+whereCondition.getRelation());
						}
						k++;
					}
					if(!whereList.isEmpty()) {
						if(condition.getIsAnd()) {
							WHERE(whereList.toArray(new String[whereList.size()]));
						}
						else {
							WHERE(String.join(" OR ", whereList));
						}
						
					}
					j++;
				}
				//三级join表中的条件
				j=0;
				for(JoinTableConditionInternal<?, ?> thirdJoin:join.getTable().getJoinList()) {
					if(secondJoinClassList.contains(thirdJoin.getTable().getEntityClass())) {
						j++;
						continue;
					}
					int jj=0;
					for(ConditionBuilder<?> condition:thirdJoin.getTable().getConditions()) {
						List<String> whereList=new ArrayList<>();
						int k=0;
						for(WhereConditionBuilder<?> whereCondition:condition.getWhereConditions()) {
							if(k==0) {
								AND();
							}
							String left=whereCondition.getColumn().buildSqlNoAs(configuration,getTableAlias(whereCondition.getColumn().getEntityClass()));
							if(whereCondition.getHasValue()) {
								if(whereCondition.getListValue()) {
									List<String> listXml=new ArrayList<>();
									for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
										listXml.add("#{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value["+listIndex+"]}");
									}
									whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");
								}
								else {
									whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value}");
								}
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation());
							}
							k++;
						}
						if(!whereList.isEmpty()) {
							if(condition.getIsAnd()) {
								WHERE(whereList.toArray(new String[whereList.size()]));
							}
							else {
								WHERE(String.join(" OR ", whereList));
							}
							
						}
						jj++;
					}
					j++;
				}
				i++;
			}
			
			//having条件
			i=0;
			for(ConditionBuilder<?> condition:sourceTable.getHavingConditions()) {
				int j=0;
				List<String> whereList=new ArrayList<>();
				for(WhereConditionBuilder<?> whereCondition:condition.getWhereConditions()) {
					String left=null;
					if(singleTable) {
						left=whereCondition.getColumn().buildSqlNoAs(configuration);
					}
					else {
						left=whereCondition.getColumn().buildSqlNoAs(configuration,getTableAlias(whereCondition.getColumn().getEntityClass()));
					}
					if(whereCondition.getHasValue()) {
						if(whereCondition.getListValue()) {
							List<String> listXml=new ArrayList<>();
							for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
								listXml.add("#{table.havingConditions["+i+"].whereConditions["+j+"].value["+listIndex+"]}");
							}
							whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");
						}
						else {
							whereList.add(left+" "+whereCondition.getRelation()+" #{table.havingConditions["+i+"].whereConditions["+j+"].value}");
						}
					}
					else {
						whereList.add(left+" "+whereCondition.getRelation());
					}
					j++;
				}
				if(!whereList.isEmpty()) {
					HAVING(whereList.toArray(new String[whereList.size()]));
				}
				i++;
			}
			
			i=0;
			for(JoinTableConditionInternal<?, ?> join:sourceTable.getJoinList()) {
				int j=0;
				for(ConditionBuilder<?> condition:join.getTable().getHavingConditions()) {
					List<String> whereList=new ArrayList<>();
					int k=0;
					for(WhereConditionBuilder<?> whereCondition:condition.getWhereConditions()) {
						if(k==0) {
							AND();
						}
						String left=null;
						if(singleTable) {
							left=whereCondition.getColumn().buildSqlNoAs(configuration);
						}
						else {
							left=whereCondition.getColumn().buildSqlNoAs(configuration,getTableAlias(whereCondition.getColumn().getEntityClass()));
						}
						if(whereCondition.getHasValue()) {
							if(whereCondition.getListValue()) {
								List<String> listXml=new ArrayList<>();
								for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
									listXml.add("#{table.joinList["+i+"].table.havingConditions["+j+"].whereConditions["+k+"].value["+listIndex+"]}");
								}
								whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.havingConditions["+j+"].whereConditions["+k+"].value}");
							}
						}
						else {
							whereList.add(left+" "+whereCondition.getRelation());
						}
						k++;
					}
					if(!whereList.isEmpty()) {
						HAVING(whereList.toArray(new String[whereList.size()]));
					}
					j++;
				}
				//三级having
				j=0;
				for(JoinTableConditionInternal<?, ?> thirdJoin:join.getTable().getJoinList()) {
					if(secondJoinClassList.contains(thirdJoin.getTable().getEntityClass())) {
						j++;
						continue;
					}
					int jj=0;
					for(ConditionBuilder<?> condition:thirdJoin.getTable().getHavingConditions()) {
						List<String> whereList=new ArrayList<>();
						int k=0;
						for(WhereConditionBuilder<?> whereCondition:condition.getWhereConditions()) {
							if(k==0) {
								AND();
							}
							String left=whereCondition.getColumn().buildSqlNoAs(configuration,getTableAlias(whereCondition.getColumn().getEntityClass()));
							if(whereCondition.getHasValue()) {
								if(whereCondition.getListValue()) {
									List<String> listXml=new ArrayList<>();
									for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
										listXml.add("#{table.joinList["+i+"].table.joinList["+j+"].table.havingConditions["+jj+"].whereConditions["+k+"].value["+listIndex+"]}");
									}
									whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");
								}
								else {
									whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.joinList["+j+"].table.havingConditions["+jj+"].whereConditions["+k+"].value}");
								}
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation());
							}
							k++;
						}
						if(!whereList.isEmpty()) {
							HAVING(whereList.toArray(new String[whereList.size()]));
						}
						jj++;
					}
					j++;
				}
				
				i++;
			}
			
			//分页
//			if(sourceTable.getPageSize()!=null) {
//				LIMIT("#{table.pageSize}");
//			}
//			if(sourceTable.getSkipSize()!=null) {
//				OFFSET("#{table.skipSize}");
//			}
            
        }}.toString();
	}
		
	public String selectBySql(String sql) {
		return sql;
	}
	
	@SuppressWarnings("unchecked")
	public String selectBySqlWithParams(Map<String,Object> input) {
		String sql=String.valueOf(input.get("sql"));
		if(sql!=null) {
			Map<String,Object> paramMap=(Map<String,Object>)input.get("params");
			if(paramMap!=null) {
				Pattern pattern=Pattern.compile("[\\$#]\\{\\w+\\}");
				Matcher matcher=pattern.matcher(sql);
				while(matcher.find()) {
					String variable=matcher.group().substring(2, matcher.group().length()-1);
					logger.debug("sql find:{},start:{},end:{},variable:{}",matcher.group(),matcher.start(),matcher.end(),variable);
					Object value=paramMap.get(variable);
					if(matcher.group().startsWith("#")){
						if(value==null) {
							throw new NullPointerException("参数值"+variable+"不能为空");
						}
						if(value instanceof Iterable<?>) {
							Iterable<?> iter=(Iterable<?>)value;
							Iterator<?> iterator=iter.iterator();
							List<String> list=new ArrayList<>();
							while(iterator.hasNext()) {
								iterator.next();
								list.add(" ? ");
							}
							sql=sql.replace(matcher.group(), "("+String.join(",", list)+")");
						}
						else {
							sql=sql.replace(matcher.group(), " ? ");
						}
					}
					else if(matcher.group().startsWith("$")){
						sql=sql.replace(matcher.group(), value==null?"":String.valueOf(value));
					}
				}
			}
		}
		return sql;
	}
	
	public <T> String insertBatch(Map<String,Object> data) {
		if(data!=null) {
			List<?> dataList=(List<?>) data.get("list");
			if(dataList.size()>0) {
				return new SQL() {{
					List<String> fieldNames=new ArrayList<>();
					BuilderConfiguration config=(BuilderConfiguration)data.get("config");
					for(int i=0;i<dataList.size();i++) {
						if(i==0) {
							Object element=dataList.get(i);
							Class<?> clazz=element.getClass();
							String tableName=config.getTableNameHandler().apply(element.getClass());
							INSERT_INTO(tableName);
							Field[] fields=ReflectUtil.getDeclaredFields(clazz);
							for(Field field:fields) {
								ColumnQueryBuilder<?> colBuilder=new ColumnQueryBuilder<>(clazz,field);
								String col=config.getColumnNameHandler().apply(colBuilder);
								INTO_COLUMNS(col);
								fieldNames.add(field.getName());
							}
						}
						for(String fieldName:fieldNames) {
							INTO_VALUES("#{list["+i+"]."+fieldName+"}");
						}
						ADD_ROW();
					}
					
				}}.toString();
			}
		}
		return null;
	}
	
	private String getTableAlias(Class<?> clazz) {
		return aliasMap.compute(clazz, (a,b)->{
			if(b==null) {
				char character=(char)(97+aliasMap.size());
				return String.valueOf(character);
			}
			else {
				return b;
			}
		});
	}
	
	private String getTableName(BuilderConfiguration configuration,TableQueryBuilder<?> builder) {
		String tableName=builder.getTableName();
		if(tableName!=null) {
			return tableName;
		}
		else {
			return configuration.getTableNameHandler().apply(builder.getEntityClass());
		}
	}
	
}
