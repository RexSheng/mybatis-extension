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
import com.github.rexsheng.mybatis.extension.TableDeleteBuilder;
import com.github.rexsheng.mybatis.extension.TableQueryBuilder;
import com.github.rexsheng.mybatis.extension.WhereConditionBuilder;
import com.github.rexsheng.mybatis.extension.JoinConditionBuilder.JoinColumnsInternal;
import com.github.rexsheng.mybatis.extension.TableQueryBuilder.JoinTableConditionInternal;
import com.github.rexsheng.mybatis.extension.TableUpdateBuilder;
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
			Boolean doSelect=false;
			for(ColumnQueryBuilder<?> column:sourceTable.getSelectColumns()) {
				if(singleTable) {
					if(sourceTable.getDistinct()) {
						SELECT_DISTINCT(column.buildSql(configuration));
					}
					else {
						SELECT(column.buildSql(configuration));
					}
				}
				else {
					if(sourceTable.getDistinct()) {
						SELECT_DISTINCT(column.buildSql(configuration,getTableAlias(column.getEntityClass())));
					}
					else {
						SELECT(column.buildSql(configuration,getTableAlias(column.getEntityClass())));
					}
				}
				doSelect=true;
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
					if(sourceTable.getDistinct()) {
						SELECT_DISTINCT(column.buildSql(configuration,getTableAlias(column.getEntityClass())));
					}
					else {
						SELECT(column.buildSql(configuration,getTableAlias(column.getEntityClass())));
					}
					doSelect=true;
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
					onConditionsStr=" on "+String.join(" and ", onConditions);//$NON-NLS-1$
				}
				else {
					logger.error("join关联后必须使用on条件");
					throw new RuntimeException("join关联后必须使用on条件");
				}
				
				if(entry.getJoinType().equals("")) {
					JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
					
				}
				else if(entry.getJoinType().equals("left")) {//$NON-NLS-1$
					LEFT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("right")) {//$NON-NLS-1$
					RIGHT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("inner")) {//$NON-NLS-1$
					INNER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				
			}
			
			Iterator<JoinTableConditionInternal<?, ?>> iterator = secondJoinList.iterator();
			while(iterator.hasNext()) {
				JoinTableConditionInternal<?, ?> entry=iterator.next();
				for(ColumnQueryBuilder<?> column:entry.getTable().getSelectColumns()) {
					if(sourceTable.getDistinct()) {
						SELECT_DISTINCT(column.buildSql(configuration,getTableAlias(column.getEntityClass())));
					}
					else {
						SELECT(column.buildSql(configuration,getTableAlias(column.getEntityClass())));
					}
					doSelect=true;
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
					onConditionsStr=" on "+String.join(" and ", onConditions);//$NON-NLS-1$
				}
				else {
					logger.error("join关联后必须使用on条件");
					throw new RuntimeException("join关联后必须使用on条件");
				}
				
				if(entry.getJoinType().equals("")) {
					JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
					
				}
				else if(entry.getJoinType().equals("left")) {//$NON-NLS-1$
					LEFT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("right")) {//$NON-NLS-1$
					RIGHT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("inner")) {//$NON-NLS-1$
					INNER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);//$NON-NLS-1$
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
							int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
							if(maxInLength>0) {
								int length=((List<?>)whereCondition.getValue()).size();
								int batchCount=0;
								int yushu = length % maxInLength;
						        if (yushu == 0) {
						        	batchCount = length / maxInLength;
						        } else {
						        	batchCount = (length / maxInLength) + 1;
						        }
						        StringBuilder sb=new StringBuilder();
						        int startIndex=0;
						        int endIndex=configuration.getDatabaseDialect().getProperty().getMaxInLength();
						        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
						        	List<String> listXml=new ArrayList<>();
						        	
						        	if(sizeIndex==batchCount-1) {
						        		endIndex=length;
						        	}
						        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
										listXml.add("#{table.conditions["+i+"].whereConditions["+j+"].value["+listIndex+"]}");//$NON-NLS-1$
									}
						        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
						        	sb.append(" OR ");//$NON-NLS-1$
						        	
						        	startIndex=endIndex;
						        	endIndex+=maxInLength;
						        }
						        if(sb.length()>0) {
						        	sb.delete(sb.length()-4,sb.length());
						        }
						        if(batchCount>1) {
						        	sb.insert(0, "(");
						        	sb.append(")");
						        }
						        whereList.add(sb.toString());
							}
							else {
								List<String> listXml=new ArrayList<>();
								for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
									listXml.add("#{table.conditions["+i+"].whereConditions["+j+"].value["+listIndex+"]}");//$NON-NLS-1$
								}
								whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
							}						
						}
						else {
							whereList.add(left+" "+whereCondition.getRelation()+" #{table.conditions["+i+"].whereConditions["+j+"].value}");//$NON-NLS-1$
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
						WHERE(String.join(" OR ", whereList));//$NON-NLS-1$
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
								int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
								if(maxInLength>0) {
									int length=((List<?>)whereCondition.getValue()).size();
									int batchCount=0;
									int yushu = length % maxInLength;
							        if (yushu == 0) {
							        	batchCount = length / maxInLength;
							        } else {
							        	batchCount = (length / maxInLength) + 1;
							        }
							        StringBuilder sb=new StringBuilder();
							        int startIndex=0;
							        int endIndex=maxInLength;
							        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
							        	List<String> listXml=new ArrayList<>();
							        	
							        	if(sizeIndex==batchCount-1) {
							        		endIndex=length;
							        	}
							        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
											listXml.add("#{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
										}
							        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
							        	sb.append(" OR ");//$NON-NLS-1$
							        	
							        	startIndex=endIndex;
							        	endIndex+=maxInLength;
							        }
							        if(sb.length()>0) {
							        	sb.delete(sb.length()-4,sb.length());
							        }
							        if(batchCount>1) {
							        	sb.insert(0, "(");
							        	sb.append(")");
							        }
							        whereList.add(sb.toString());
								}
								else {
									List<String> listXml=new ArrayList<>();
									for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
										listXml.add("#{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
									}
									whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
								}
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value}");//$NON-NLS-1$
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
							WHERE(String.join(" OR ", whereList));//$NON-NLS-1$
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
									int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
									if(maxInLength>0) {
										int length=((List<?>)whereCondition.getValue()).size();
										int batchCount=0;
										int yushu = length % maxInLength;
								        if (yushu == 0) {
								        	batchCount = length / maxInLength;
								        } else {
								        	batchCount = (length / maxInLength) + 1;
								        }
								        StringBuilder sb=new StringBuilder();
								        int startIndex=0;
								        int endIndex=maxInLength;
								        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
								        	List<String> listXml=new ArrayList<>();
								        	
								        	if(sizeIndex==batchCount-1) {
								        		endIndex=length;
								        	}
								        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
								        		listXml.add("#{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
											}
								        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
								        	sb.append(" OR ");//$NON-NLS-1$
								        	
								        	startIndex=endIndex;
								        	endIndex+=maxInLength;
								        }
								        if(sb.length()>0) {
								        	sb.delete(sb.length()-4,sb.length());
								        }
								        if(batchCount>1) {
								        	sb.insert(0, "(");
								        	sb.append(")");
								        }
								        whereList.add(sb.toString());
									}
									else {
										List<String> listXml=new ArrayList<>();
										for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
											listXml.add("#{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
										}
										whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
									}
								}
								else {
									whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value}");//$NON-NLS-1$
								}
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation());//$NON-NLS-1$
							}
							k++;
						}
						if(!whereList.isEmpty()) {
							if(condition.getIsAnd()) {
								WHERE(whereList.toArray(new String[whereList.size()]));
							}
							else {
								WHERE(String.join(" OR ", whereList));//$NON-NLS-1$
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
							int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
							if(maxInLength>0) {
								int length=((List<?>)whereCondition.getValue()).size();
								int batchCount=0;
								int yushu = length % maxInLength;
						        if (yushu == 0) {
						        	batchCount = length / maxInLength;
						        } else {
						        	batchCount = (length / maxInLength) + 1;
						        }
						        StringBuilder sb=new StringBuilder();
						        int startIndex=0;
						        int endIndex=maxInLength;
						        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
						        	List<String> listXml=new ArrayList<>();
						        	
						        	if(sizeIndex==batchCount-1) {
						        		endIndex=length;
						        	}
						        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
						        		listXml.add("#{table.havingConditions["+i+"].whereConditions["+j+"].value["+listIndex+"]}");//$NON-NLS-1$
									}
						        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
						        	sb.append(" OR ");//$NON-NLS-1$
						        	
						        	startIndex=endIndex;
						        	endIndex+=maxInLength;
						        }
						        if(sb.length()>0) {
						        	sb.delete(sb.length()-4,sb.length());
						        }
						        if(batchCount>1) {
						        	sb.insert(0, "(");
						        	sb.append(")");
						        }
						        whereList.add(sb.toString());
							}
							else {
								List<String> listXml=new ArrayList<>();
								for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
									listXml.add("#{table.havingConditions["+i+"].whereConditions["+j+"].value["+listIndex+"]}");//$NON-NLS-1$
								}
								whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
							}
						}
						else {
							whereList.add(left+" "+whereCondition.getRelation()+" #{table.havingConditions["+i+"].whereConditions["+j+"].value}");//$NON-NLS-1$
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
								int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
								if(maxInLength>0) {
									int length=((List<?>)whereCondition.getValue()).size();
									int batchCount=0;
									int yushu = length % maxInLength;
							        if (yushu == 0) {
							        	batchCount = length / maxInLength;
							        } else {
							        	batchCount = (length / maxInLength) + 1;
							        }
							        StringBuilder sb=new StringBuilder();
							        int startIndex=0;
							        int endIndex=maxInLength;
							        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
							        	List<String> listXml=new ArrayList<>();
							        	
							        	if(sizeIndex==batchCount-1) {
							        		endIndex=length;
							        	}
							        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
							        		listXml.add("#{table.joinList["+i+"].table.havingConditions["+j+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
										}
							        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
							        	sb.append(" OR ");//$NON-NLS-1$
							        	
							        	startIndex=endIndex;
							        	endIndex+=maxInLength;
							        }
							        if(sb.length()>0) {
							        	sb.delete(sb.length()-4,sb.length());
							        }
							        if(batchCount>1) {
							        	sb.insert(0, "(");
							        	sb.append(")");
							        }
							        whereList.add(sb.toString());
								}
								else {
									List<String> listXml=new ArrayList<>();
									for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
										listXml.add("#{table.joinList["+i+"].table.havingConditions["+j+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
									}
									whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
								}
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.havingConditions["+j+"].whereConditions["+k+"].value}");//$NON-NLS-1$
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
									int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
									if(maxInLength>0) {
										int length=((List<?>)whereCondition.getValue()).size();
										int batchCount=0;
										int yushu = length % maxInLength;
								        if (yushu == 0) {
								        	batchCount = length / maxInLength;
								        } else {
								        	batchCount = (length / maxInLength) + 1;
								        }
								        StringBuilder sb=new StringBuilder();
								        int startIndex=0;
								        int endIndex=maxInLength;
								        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
								        	List<String> listXml=new ArrayList<>();
								        	
								        	if(sizeIndex==batchCount-1) {
								        		endIndex=length;
								        	}
								        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
								        		listXml.add("#{table.joinList["+i+"].table.joinList["+j+"].table.havingConditions["+jj+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
											}
								        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
								        	sb.append(" OR ");//$NON-NLS-1$
								        	
								        	startIndex=endIndex;
								        	endIndex+=maxInLength;
								        }
								        if(sb.length()>0) {
								        	sb.delete(sb.length()-4,sb.length());
								        }
								        if(batchCount>1) {
								        	sb.insert(0, "(");
								        	sb.append(")");
								        }
								        whereList.add(sb.toString());
									}
									else {
										List<String> listXml=new ArrayList<>();
										for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
											listXml.add("#{table.joinList["+i+"].table.joinList["+j+"].table.havingConditions["+jj+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
										}
										whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
									}
								}
								else {
									whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.joinList["+j+"].table.havingConditions["+jj+"].whereConditions["+k+"].value}");//$NON-NLS-1$
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
			if(!doSelect) {
				throw new RuntimeException("select必须指定列");
			}
            
        }}.toString();
	}
		
	public String selectBySql(String sql) {
		return sql;
	}
	
	@SuppressWarnings("unchecked")
	public String selectBySqlWithParams(Map<String,Object> input) {
		String sql=String.valueOf(input.get("sql"));
		if(sql!=null) {
			Map<String,Object> paramMap=(Map<String,Object>)input.get("params");//$NON-NLS-1$
			if(paramMap!=null) {
				Pattern pattern=Pattern.compile("[\\$#]\\{(\\s)*\\w+(\\s)*(,(\\s)*jdbcType(\\s)*=(\\s)*\\w+(\\s)*)?}");//$NON-NLS-1$
				Matcher matcher=pattern.matcher(sql);
				while(matcher.find()) {
					String variable=matcher.group().substring(2, matcher.group().length()-1);
					if(variable.indexOf(",")>-1) {
						variable=variable.substring(0, variable.indexOf(",")).trim();
					}
					if(logger.isDebugEnabled()) {
						logger.debug("variable find:{},start:{},end:{},property:{}",matcher.group(),matcher.start(),matcher.end(),variable);
					}
					
					Object value=paramMap.get(variable);
					if(matcher.group().startsWith("#")){//$NON-NLS-1$
						if(value==null) {
							throw new NullPointerException("参数值"+variable+"不能为空");//$NON-NLS-1$
						}
						if(value instanceof Iterable<?>) {
							Iterable<?> iter=(Iterable<?>)value;
							Iterator<?> iterator=iter.iterator();
							List<String> list=new ArrayList<>();
							while(iterator.hasNext()) {
								iterator.next();
								list.add(" ? ");//$NON-NLS-1$
							}
							sql=sql.replace(matcher.group(), "("+String.join(",", list)+")");//$NON-NLS-1$
						}
						else {
							sql=sql.replace(matcher.group(), " ? ");//$NON-NLS-1$
						}
					}
					else if(matcher.group().startsWith("$")){//$NON-NLS-1$
						sql=sql.replace(matcher.group(), value==null?"":String.valueOf(value));
					}
				}
			}
		}
		return sql;
	}
	
	public <T> String insertBatch(Map<String,Object> data) {
		if(data!=null) {
			List<?> dataList=(List<?>) data.get("list");//$NON-NLS-1$
			if(dataList.size()>0) {
				return new SQL() {{
					List<String> fieldNames=new ArrayList<>();
					BuilderConfiguration config=(BuilderConfiguration)data.get("config");//$NON-NLS-1$
					for(int i=0;i<dataList.size();i++) {
						if(i==0) {
							Object element=dataList.get(i);
							Class<?> clazz=element.getClass();
							String tableName=config.getTableHandler().getName(element.getClass(),config);
							INSERT_INTO(tableName);
							List<Field> fields=ReflectUtil.getDeclaredFields(clazz);
							for(Field field:fields) {
								ColumnQueryBuilder<?> colBuilder=new ColumnQueryBuilder<>(clazz,field);
								String col=config.getColumnHandler().getName(colBuilder,config);
								INTO_COLUMNS(col);
								fieldNames.add(field.getName());
							}
						}
						for(String fieldName:fieldNames) {
							INTO_VALUES("#{list["+i+"]."+fieldName+"}");//$NON-NLS-1$
						}
						ADD_ROW();
					}
					
				}}.toString();
			}
		}
		return null;
	}
	
	public <T> String updateByBuilder(QueryBuilder<T> builder) {
		return new SQL(){{
			BuilderConfiguration configuration=builder.getBuiderConfig();
			TableUpdateBuilder<?> sourceTable=(TableUpdateBuilder<?>) builder.getTable();
			if(sourceTable.getUpdateColumns().isEmpty()) {
				logger.error("update必须指定set列");
				throw new RuntimeException("update必须指定set列");
			}
			Boolean singleTable=sourceTable.getJoinList().isEmpty();
			if(singleTable) {
				UPDATE(getTableName(configuration,sourceTable));
			}
			else {
				UPDATE(getTableName(configuration,sourceTable)+" AS "+getTableAlias(sourceTable.getEntityClass()));
			}
			
			//set常量值
			int i=0;
			List<String> columnList=new ArrayList<>();
			for(WhereConditionBuilder<?> condition:sourceTable.getUpdateColumns()) {
				String left=null;
				if(singleTable) {
					left=condition.getColumn().buildSqlNoAs(configuration);
				}
				else {
					left=condition.getColumn().buildSqlNoAs(configuration,getTableAlias(condition.getColumn().getEntityClass()));
				}
				if(condition.getHasValue()) {
					//listvalue情况不会发生
					columnList.add(left+" "+condition.getRelation()+" #{table.updateColumns["+i+"].value}");//$NON-NLS-1$
				}
				else {
					columnList.add(left+" "+condition.getRelation());
				}
				i++;
			}
			if(!columnList.isEmpty()) {
				SET(columnList.toArray(new String[columnList.size()]));
			}
			
			//子级join关联列表
			List<JoinTableConditionInternal<?, ?>> secondJoinList=new ArrayList<>();
			List<Class<?>> secondJoinClassList=new ArrayList<>();
			for(JoinTableConditionInternal<?, ?> entry:sourceTable.getJoinList()) {
				secondJoinList.addAll(entry.getTable().getJoinList());
				secondJoinClassList.add(entry.getTable().getEntityClass());
			}
			
			for(JoinTableConditionInternal<?, ?> entry:sourceTable.getJoinList()) {
				for(ColumnQueryBuilder<?> column:entry.getTable().getOrderByColumns()) {
					ORDER_BY(column.buildSqlNoAs(configuration,getTableAlias(column.getEntityClass())));
				}
				List<String> onConditions=new ArrayList<>();
				List<String> setConditions=new ArrayList<>();
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
						for(JoinColumnsInternal<?,?> internalOn:secondJoinEntry.getCondtion().getConditionsForUpdate()) {
							String leftAlias=getTableAlias(secondJoinEntry.getCondtion().getEntityClass());
							String rightAlias=getTableAlias(secondJoinEntry.getCondtion().getRightClazz());
							setConditions.add(internalOn.getLeftColumn().buildSqlNoAs(configuration,leftAlias)+internalOn.getRelation()+internalOn.getRightColumn().buildSqlNoAs(configuration,rightAlias));
						}
						iterator.remove();
					}
				}
				
				for(JoinColumnsInternal<?,?> internalOn:entry.getCondtion().getConditions()) {
					String leftAlias=getTableAlias(entry.getCondtion().getEntityClass());
					String rightAlias=getTableAlias(entry.getCondtion().getRightClazz());
					onConditions.add(internalOn.getLeftColumn().buildSqlNoAs(configuration,leftAlias)+internalOn.getRelation()+internalOn.getRightColumn().buildSqlNoAs(configuration,rightAlias));
				}
				for(JoinColumnsInternal<?,?> internalOn:entry.getCondtion().getConditionsForUpdate()) {
					String leftAlias=getTableAlias(entry.getCondtion().getEntityClass());
					String rightAlias=getTableAlias(entry.getCondtion().getRightClazz());
					setConditions.add(internalOn.getLeftColumn().buildSqlNoAs(configuration,leftAlias)+internalOn.getRelation()+internalOn.getRightColumn().buildSqlNoAs(configuration,rightAlias));
				}
				if(!setConditions.isEmpty()) {
					SET(setConditions.toArray(new String[setConditions.size()]));
				}
				String onConditionsStr="";
				if(!onConditions.isEmpty()) {
					onConditionsStr=" on "+String.join(" and ", onConditions);//$NON-NLS-1$
				}
				else {
					logger.error("join关联后必须使用on条件");
					throw new RuntimeException("join关联后必须使用on条件");
				}
				
				if(entry.getJoinType().equals("")) {
					JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
					
				}
				else if(entry.getJoinType().equals("left")) {//$NON-NLS-1$
					LEFT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("right")) {//$NON-NLS-1$
					RIGHT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("inner")) {//$NON-NLS-1$
					INNER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				
			}
			
			Iterator<JoinTableConditionInternal<?, ?>> iterator = secondJoinList.iterator();
			while(iterator.hasNext()) {
				JoinTableConditionInternal<?, ?> entry=iterator.next();
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
					onConditionsStr=" on "+String.join(" and ", onConditions);//$NON-NLS-1$
				}
				else {
					logger.error("join关联后必须使用on条件");
					throw new RuntimeException("join关联后必须使用on条件");
				}
				
				if(entry.getJoinType().equals("")) {
					JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
					
				}
				else if(entry.getJoinType().equals("left")) {//$NON-NLS-1$
					LEFT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("right")) {//$NON-NLS-1$
					RIGHT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("inner")) {//$NON-NLS-1$
					INNER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);//$NON-NLS-1$
				}
			}
			
			//where条件
			i=0;
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
							int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
							if(maxInLength>0) {
								int length=((List<?>)whereCondition.getValue()).size();
								int batchCount=0;
								int yushu = length % maxInLength;
						        if (yushu == 0) {
						        	batchCount = length / maxInLength;
						        } else {
						        	batchCount = (length / maxInLength) + 1;
						        }
						        StringBuilder sb=new StringBuilder();
						        int startIndex=0;
						        int endIndex=maxInLength;
						        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
						        	List<String> listXml=new ArrayList<>();
						        	
						        	if(sizeIndex==batchCount-1) {
						        		endIndex=length;
						        	}
						        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
										listXml.add("#{table.conditions["+i+"].whereConditions["+j+"].value["+listIndex+"]}");//$NON-NLS-1$
									}
						        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
						        	sb.append(" OR ");//$NON-NLS-1$
						        	
						        	startIndex=endIndex;
						        	endIndex+=maxInLength;
						        }
						        if(sb.length()>0) {
						        	sb.delete(sb.length()-4,sb.length());
						        }
						        if(batchCount>1) {
						        	sb.insert(0, "(");
						        	sb.append(")");
						        }
						        whereList.add(sb.toString());
							}
							else {
								List<String> listXml=new ArrayList<>();
								for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
									listXml.add("#{table.conditions["+i+"].whereConditions["+j+"].value["+listIndex+"]}");//$NON-NLS-1$
								}
								whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
							}
						}
						else {
							whereList.add(left+" "+whereCondition.getRelation()+" #{table.conditions["+i+"].whereConditions["+j+"].value}");//$NON-NLS-1$
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
						WHERE(String.join(" OR ", whereList));//$NON-NLS-1$
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
								int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
								if(maxInLength>0) {
									int length=((List<?>)whereCondition.getValue()).size();
									int batchCount=0;
									int yushu = length % maxInLength;
							        if (yushu == 0) {
							        	batchCount = length / maxInLength;
							        } else {
							        	batchCount = (length / maxInLength) + 1;
							        }
							        StringBuilder sb=new StringBuilder();
							        int startIndex=0;
							        int endIndex=maxInLength;
							        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
							        	List<String> listXml=new ArrayList<>();
							        	
							        	if(sizeIndex==batchCount-1) {
							        		endIndex=length;
							        	}
							        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
							        		listXml.add("#{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
										}
							        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
							        	sb.append(" OR ");//$NON-NLS-1$
							        	
							        	startIndex=endIndex;
							        	endIndex+=maxInLength;
							        }
							        if(sb.length()>0) {
							        	sb.delete(sb.length()-4,sb.length());
							        }
							        if(batchCount>1) {
							        	sb.insert(0, "(");
							        	sb.append(")");
							        }
							        whereList.add(sb.toString());
								}
								else {
									List<String> listXml=new ArrayList<>();
									for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
										listXml.add("#{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
									}
									whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
								}
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value}");//$NON-NLS-1$
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
							WHERE(String.join(" OR ", whereList));//$NON-NLS-1$
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
									int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
									if(maxInLength>0) {
										int length=((List<?>)whereCondition.getValue()).size();
										int batchCount=0;
										int yushu = length % maxInLength;
								        if (yushu == 0) {
								        	batchCount = length / maxInLength;
								        } else {
								        	batchCount = (length / maxInLength) + 1;
								        }
								        StringBuilder sb=new StringBuilder();
								        int startIndex=0;
								        int endIndex=maxInLength;
								        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
								        	List<String> listXml=new ArrayList<>();
								        	
								        	if(sizeIndex==batchCount-1) {
								        		endIndex=length;
								        	}
								        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
								        		listXml.add("#{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
											}
								        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
								        	sb.append(" OR ");//$NON-NLS-1$
								        	
								        	startIndex=endIndex;
								        	endIndex+=maxInLength;
								        }
								        if(sb.length()>0) {
								        	sb.delete(sb.length()-4,sb.length());
								        }
								        if(batchCount>1) {
								        	sb.insert(0, "(");
								        	sb.append(")");
								        }
								        whereList.add(sb.toString());
									}
									else {
										List<String> listXml=new ArrayList<>();
										for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
											listXml.add("#{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
										}
										whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
									}
								}
								else {
									whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value}");//$NON-NLS-1$
								}
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation());//$NON-NLS-1$
							}
							k++;
						}
						if(!whereList.isEmpty()) {
							if(condition.getIsAnd()) {
								WHERE(whereList.toArray(new String[whereList.size()]));
							}
							else {
								WHERE(String.join(" OR ", whereList));//$NON-NLS-1$
							}
							
						}
						jj++;
					}
					j++;
				}
				i++;
			}
            
        }}.toString();
	}
	
	
	
	public <T> String deleteByBuilder(QueryBuilder<T> builder) {
		BuilderConfiguration configuration=builder.getBuiderConfig();
		TableDeleteBuilder<?> sourceTable=(TableDeleteBuilder<?>) builder.getTable();
		
		Boolean singleTable=sourceTable.getJoinList().isEmpty();
		
		String sql=new SQL(){{
			SELECT("1");
			if(singleTable) {
//				DELETE_FROM
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
					onConditionsStr=" on "+String.join(" and ", onConditions);//$NON-NLS-1$
				}
				else {
					logger.error("join关联后必须使用on条件");
					throw new RuntimeException("join关联后必须使用on条件");
				}
				
				if(entry.getJoinType().equals("")) {
					JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("left")) {//$NON-NLS-1$
					LEFT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("right")) {//$NON-NLS-1$
					RIGHT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("inner")) {//$NON-NLS-1$
					INNER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				
			}
			
			Iterator<JoinTableConditionInternal<?, ?>> iterator = secondJoinList.iterator();
			while(iterator.hasNext()) {
				JoinTableConditionInternal<?, ?> entry=iterator.next();
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
					onConditionsStr=" on "+String.join(" and ", onConditions);//$NON-NLS-1$
				}
				else {
					logger.error("join关联后必须使用on条件");
					throw new RuntimeException("join关联后必须使用on条件");
				}
				
				if(entry.getJoinType().equals("")) {
					JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
					
				}
				else if(entry.getJoinType().equals("left")) {//$NON-NLS-1$
					LEFT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("right")) {//$NON-NLS-1$
					RIGHT_OUTER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);
				}
				else if(entry.getJoinType().equals("inner")) {//$NON-NLS-1$
					INNER_JOIN(getTableName(configuration,entry.getTable()) +" AS "+getTableAlias(entry.getTable().getEntityClass())+onConditionsStr);//$NON-NLS-1$
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
							int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
							if(maxInLength>0) {
								int length=((List<?>)whereCondition.getValue()).size();
								int batchCount=0;
								int yushu = length % maxInLength;
						        if (yushu == 0) {
						        	batchCount = length / maxInLength;
						        } else {
						        	batchCount = (length / maxInLength) + 1;
						        }
						        StringBuilder sb=new StringBuilder();
						        int startIndex=0;
						        int endIndex=maxInLength;
						        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
						        	List<String> listXml=new ArrayList<>();
						        	
						        	if(sizeIndex==batchCount-1) {
						        		endIndex=length;
						        	}
						        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
										listXml.add("#{table.conditions["+i+"].whereConditions["+j+"].value["+listIndex+"]}");//$NON-NLS-1$
									}
						        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
						        	sb.append(" OR ");//$NON-NLS-1$
						        	
						        	startIndex=endIndex;
						        	endIndex+=maxInLength;
						        }
						        if(sb.length()>0) {
						        	sb.delete(sb.length()-4,sb.length());
						        }
						        if(batchCount>1) {
						        	sb.insert(0, "(");
						        	sb.append(")");
						        }
						        whereList.add(sb.toString());
							}
							else {
								List<String> listXml=new ArrayList<>();
								for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
									listXml.add("#{table.conditions["+i+"].whereConditions["+j+"].value["+listIndex+"]}");//$NON-NLS-1$
								}
								whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
							}
						}
						else {
							whereList.add(left+" "+whereCondition.getRelation()+" #{table.conditions["+i+"].whereConditions["+j+"].value}");//$NON-NLS-1$
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
						WHERE(String.join(" OR ", whereList));//$NON-NLS-1$
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
								int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
								if(maxInLength>0) {
									int length=((List<?>)whereCondition.getValue()).size();
									int batchCount=0;
									int yushu = length % maxInLength;
							        if (yushu == 0) {
							        	batchCount = length / maxInLength;
							        } else {
							        	batchCount = (length / maxInLength) + 1;
							        }
							        StringBuilder sb=new StringBuilder();
							        int startIndex=0;
							        int endIndex=maxInLength;
							        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
							        	List<String> listXml=new ArrayList<>();
							        	
							        	if(sizeIndex==batchCount-1) {
							        		endIndex=length;
							        	}
							        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
							        		listXml.add("#{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
										}
							        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
							        	sb.append(" OR ");//$NON-NLS-1$
							        	
							        	startIndex=endIndex;
							        	endIndex+=maxInLength;
							        }
							        if(sb.length()>0) {
							        	sb.delete(sb.length()-4,sb.length());
							        }
							        if(batchCount>1) {
							        	sb.insert(0, "(");
							        	sb.append(")");
							        }
							        whereList.add(sb.toString());
								}
								else {
									List<String> listXml=new ArrayList<>();
									for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
										listXml.add("#{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
									}
									whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
								}
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.conditions["+j+"].whereConditions["+k+"].value}");//$NON-NLS-1$
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
							WHERE(String.join(" OR ", whereList));//$NON-NLS-1$
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
									int maxInLength=configuration.getDatabaseDialect().getProperty().getMaxInLength();
									if(maxInLength>0) {
										int length=((List<?>)whereCondition.getValue()).size();
										int batchCount=0;
										int yushu = length % maxInLength;
								        if (yushu == 0) {
								        	batchCount = length / maxInLength;
								        } else {
								        	batchCount = (length / maxInLength) + 1;
								        }
								        StringBuilder sb=new StringBuilder();
								        int startIndex=0;
								        int endIndex=maxInLength;
								        for (int sizeIndex = 0; sizeIndex < batchCount; sizeIndex++) {
								        	List<String> listXml=new ArrayList<>();
								        	
								        	if(sizeIndex==batchCount-1) {
								        		endIndex=length;
								        	}
								        	for(int listIndex=startIndex;listIndex<endIndex;listIndex++) {
								        		listXml.add("#{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
											}
								        	sb.append(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
								        	sb.append(" OR ");//$NON-NLS-1$
								        	
								        	startIndex=endIndex;
								        	endIndex+=maxInLength;
								        }
								        if(sb.length()>0) {
								        	sb.delete(sb.length()-4,sb.length());
								        }
								        if(batchCount>1) {
								        	sb.insert(0, "(");
								        	sb.append(")");
								        }
								        whereList.add(sb.toString());
									}
									else {
										List<String> listXml=new ArrayList<>();
										for(int listIndex=0;listIndex<((List<?>)whereCondition.getValue()).size();listIndex++) {
											listXml.add("#{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value["+listIndex+"]}");//$NON-NLS-1$
										}
										whereList.add(left+" "+whereCondition.getRelation()+" ("+String.join(",", listXml)+")");//$NON-NLS-1$
									}
								}
								else {
									whereList.add(left+" "+whereCondition.getRelation()+" #{table.joinList["+i+"].table.joinList["+j+"].table.conditions["+jj+"].whereConditions["+k+"].value}");//$NON-NLS-1$
								}
							}
							else {
								whereList.add(left+" "+whereCondition.getRelation());//$NON-NLS-1$
							}
							k++;
						}
						if(!whereList.isEmpty()) {
							if(condition.getIsAnd()) {
								WHERE(whereList.toArray(new String[whereList.size()]));
							}
							else {
								WHERE(String.join(" OR ", whereList));//$NON-NLS-1$
							}
							
						}
						jj++;
					}
					j++;
				}
				i++;
			}
        }}.toString().substring(8);
        return "DELETE "+(singleTable?"":getTableAlias(sourceTable.getEntityClass()))+sql;
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
			return configuration.getTableHandler().getName(builder.getEntityClass(),configuration);
		}
	}
	
}
