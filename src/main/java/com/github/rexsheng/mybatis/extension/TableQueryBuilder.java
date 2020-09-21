package com.github.rexsheng.mybatis.extension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.rexsheng.mybatis.core.SFunction;
import com.github.rexsheng.mybatis.util.ReflectUtil;
import com.github.rexsheng.mybatis.util.StringUtils;

/**
 * @author RexSheng 2020年8月27日 下午8:07:18
 */
public class TableQueryBuilder<T> extends EntityInfo<T>{

		
	private List<ColumnQueryBuilder<T>> selectColumns;

	private List<ConditionBuilder<T>> conditions;
	
	private List<JoinTableConditionInternal<T,?>> joinList;
	
	private List<ColumnQueryBuilder<T>> groupByColumns;
	
	private List<ConditionBuilder<T>> havingConditions;
	
	private List<ColumnQueryBuilder<T>> orderByColumns;
	
	private Integer skipSize;
	
	private Integer takeSize;
	
	public static <T> TableQueryBuilder<T> from(Class<T> clazz){
		return new TableQueryBuilder<T>(clazz);
	}
	
	public static <T> TableQueryBuilder<T> from(Class<T> clazz,String tableName){
		return new TableQueryBuilder<T>(clazz,tableName);
	}

	public TableQueryBuilder(Class<T> clazz) {
		this(clazz,null);
	}
	
	public TableQueryBuilder(Class<T> clazz,String tableName) {
		super(clazz,tableName);
		this.selectColumns=new ArrayList<>();
		this.conditions=new ArrayList<>();
		this.joinList=new ArrayList<>();
		this.groupByColumns=new ArrayList<>();
		this.havingConditions=new ArrayList<>();
		this.orderByColumns=new ArrayList<>();
	}

	
	public TableQueryBuilder<T> selectAll() {
		Field[] fields=ReflectUtil.getDeclaredFields(super.getEntityClass());
		for(Field field:fields) {
			ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),field.getName());
			this.selectColumns.add(columnQuery);
		}
		return this;
	}
	
	public TableQueryBuilder<T> select(Predicate<Field> filter) {
		Field[] fields=ReflectUtil.getDeclaredFields(super.getEntityClass());
		Arrays.asList(fields).stream().filter(filter).forEach(field->{
			ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),field.getName());
			this.selectColumns.add(columnQuery);
		});
		return this;
	}
	
	@SafeVarargs
	public final TableQueryBuilder<T> select(SFunction<T,Object>... fields) {
		for(SFunction<T,Object> field:fields) {
			String fieldName=ReflectUtil.fnToFieldName(field);
			ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName);
			this.selectColumns.add(columnQuery);
		}
		return this;
	}
	
	public TableQueryBuilder<T> select(String... columnNames) {
		for(String columnName:columnNames) {
			ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName);
			this.selectColumns.add(columnQuery);
		}
		return this;
	}
	
	public TableQueryBuilder<T> selectAs(SFunction<T,Object> field,String aliasName) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName,aliasName);
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectAs(String columnName,String aliasName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),aliasName,columnName);
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectSum(SFunction<T,Object> field,String aliasName) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName,aliasName);
		columnQuery.setPrefix("SUM(");
		columnQuery.setSuffix(")");
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectSum(String columnName,String aliasName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),aliasName,columnName);
		columnQuery.setPrefix("SUM(");
		columnQuery.setSuffix(")");
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectCount(SFunction<T,Object> field,String aliasName) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName,aliasName);
		columnQuery.setPrefix("COUNT(");
		columnQuery.setSuffix(")");
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectCount(String columnName,String aliasName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),aliasName,columnName);
		columnQuery.setPrefix("COUNT(");
		columnQuery.setSuffix(")");
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectMax(SFunction<T,Object> field,String aliasName) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName,aliasName);
		columnQuery.setPrefix("MAX(");
		columnQuery.setSuffix(")");
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectMax(String columnName,String aliasName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),aliasName,columnName);
		columnQuery.setPrefix("MAX(");
		columnQuery.setSuffix(")");
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectMin(SFunction<T,Object> field,String aliasName) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName,aliasName);
		columnQuery.setPrefix("MIN(");
		columnQuery.setSuffix(")");
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectMin(String columnName,String aliasName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),aliasName,columnName);
		columnQuery.setPrefix("MIN(");
		columnQuery.setSuffix(")");
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> groupBy(SFunction<T,Object> field) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName);
		this.groupByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> groupBy(String columnName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName);
		this.groupByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> havingSum(Consumer<ConditionBuilder<T>> field) {
		return having("SUM",field);
	}
	
	public TableQueryBuilder<T> havingCount(Consumer<ConditionBuilder<T>> field) {
		return having("COUNT",field);
	}
	
	public TableQueryBuilder<T> havingMax(Consumer<ConditionBuilder<T>> field) {
		return having("MAX",field);
	}
	
	public TableQueryBuilder<T> havingMin(Consumer<ConditionBuilder<T>> field) {
		return having("MIN",field);
	}
	
	public TableQueryBuilder<T> havingAvg(Consumer<ConditionBuilder<T>> field) {
		return having("AVG",field);
	}
	
	/**
	 * 自定义聚合函数操作
	 * @param aggregationType 聚合类型，例如SUM,AVG,COUNT等
	 * @param field 条件构造器
	 * @return
	 */
	public TableQueryBuilder<T> having(String aggregationType,Consumer<ConditionBuilder<T>> field) {
		ConditionBuilder<T> condition=new ConditionBuilder<>(super.getEntityClass());
		field.accept(condition);
		condition.getWhereConditions().forEach(a->{
			a.getColumn().setPrefix(aggregationType+"(");
			a.getColumn().setSuffix(")");
		});
		this.havingConditions.add(condition);		
		return this;
	}
	
	public ConditionBuilder<T> where(){
		ConditionBuilder<T> condition=new ConditionBuilder<>(super.getEntityClass(),a->this.conditions.add(a));
		this.conditions.add(condition);
		return condition;
	}
	
	public ConditionBuilder<T> and(){
		ConditionBuilder<T> condition=new ConditionBuilder<>(super.getEntityClass(),a->this.conditions.add(a));
		this.conditions.add(condition);
		return condition;
	}
	
	public ConditionBuilder<T> or(){
		ConditionBuilder<T> condition=new ConditionBuilder<>(super.getEntityClass(),a->this.conditions.add(a),false);
		this.conditions.add(condition);
		return condition;
	}

	public <R> JoinConditionBuilder<T,R> leftJoin(TableQueryBuilder<R> relation) {
		JoinConditionBuilder<T,R> joinRelation=new JoinConditionBuilder<>(super.getEntityClass(),relation.getEntityClass());
		JoinTableConditionInternal<T,R> internal=new JoinTableConditionInternal<>(relation,joinRelation,"left");
		this.joinList.add(internal);
		return joinRelation;
	}
	
	public <R> JoinConditionBuilder<T,R> rightJoin(TableQueryBuilder<R> relation) {
		JoinConditionBuilder<T,R> joinRelation=new JoinConditionBuilder<>(super.getEntityClass(),relation.getEntityClass());
		JoinTableConditionInternal<T,R> internal=new JoinTableConditionInternal<>(relation,joinRelation,"right");
		this.joinList.add(internal);
		return joinRelation;
	}
	
	public <R> JoinConditionBuilder<T,R> innerJoin(TableQueryBuilder<R> relation) {
		JoinConditionBuilder<T,R> joinRelation=new JoinConditionBuilder<>(super.getEntityClass(),relation.getEntityClass());
		JoinTableConditionInternal<T,R> internal=new JoinTableConditionInternal<>(relation,joinRelation,"inner");
		this.joinList.add(internal);
		return joinRelation;
	}
	
	public <R> JoinConditionBuilder<T,R> join(TableQueryBuilder<R> relation) {
		JoinConditionBuilder<T,R> joinRelation=new JoinConditionBuilder<>(super.getEntityClass(),relation.getEntityClass());
		JoinTableConditionInternal<T,R> internal=new JoinTableConditionInternal<>(relation,joinRelation,"");
		this.joinList.add(internal);
		return joinRelation;
	}
	
	public TableQueryBuilder<T> orderBy(SFunction<T,Object> field) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName);
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderBy(String columnName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName);
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderByDesc(SFunction<T,Object> field) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName);
		columnQuery.setSuffix(" DESC");
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderByDesc(String columnName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName);
		columnQuery.setSuffix(" DESC");
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderBySum(SFunction<T,Object> field) {
		return orderByAggregation(field,"SUM");
	}
	
	public TableQueryBuilder<T> orderBySum(String columnName) {
		return orderByAggregation(columnName,"SUM");
	}
	
	public TableQueryBuilder<T> orderBySumDesc(SFunction<T,Object> field) {
		return orderByAggregationDesc(field,"SUM");
	}
	
	public TableQueryBuilder<T> orderBySumDesc(String columnName) {
		return orderByAggregationDesc(columnName,"SUM");
	}
	
	public TableQueryBuilder<T> orderByCount(SFunction<T,Object> field) {
		return orderByAggregation(field,"COUNT");
	}
	
	public TableQueryBuilder<T> orderByCount(String columnName) {
		return orderByAggregation(columnName,"COUNT");
	}
	
	public TableQueryBuilder<T> orderByCountDesc(SFunction<T,Object> field) {
		return orderByAggregationDesc(field,"COUNT");
	}
	
	public TableQueryBuilder<T> orderByCountDesc(String columnName) {
		return orderByAggregationDesc(columnName,"COUNT");
	}
	
	public TableQueryBuilder<T> orderByMax(SFunction<T,Object> field) {
		return orderByAggregation(field,"MAX");
	}
	
	public TableQueryBuilder<T> orderByMax(String columnName) {
		return orderByAggregation(columnName,"MAX");
	}
	
	public TableQueryBuilder<T> orderByMaxDesc(SFunction<T,Object> field) {
		return orderByAggregationDesc(field,"MAX");
	}
	
	public TableQueryBuilder<T> orderByMaxDesc(String columnName) {
		return orderByAggregationDesc(columnName,"MAX");
	}
	
	public TableQueryBuilder<T> orderByMin(SFunction<T,Object> field) {
		return orderByAggregation(field,"MIN");
	}
	
	public TableQueryBuilder<T> orderByMin(String columnName) {
		return orderByAggregation(columnName,"MIN");
	}
	
	public TableQueryBuilder<T> orderByMinDesc(SFunction<T,Object> field) {
		return orderByAggregationDesc(field,"MIN");
	}
	
	public TableQueryBuilder<T> orderByMinDesc(String columnName) {
		return orderByAggregationDesc(columnName,"MIN");
	}
	
	public TableQueryBuilder<T> orderByAvg(SFunction<T,Object> field) {
		return orderByAggregation(field,"AVG");
	}
	
	public TableQueryBuilder<T> orderByAvg(String columnName) {
		return orderByAggregation(columnName,"AVG");
	}
	
	public TableQueryBuilder<T> orderByAvgDesc(SFunction<T,Object> field) {
		return orderByAggregationDesc(field,"AVG");
	}
	
	public TableQueryBuilder<T> orderByAvgDesc(String columnName) {
		return orderByAggregationDesc(columnName,"AVG");
	}
	
	public TableQueryBuilder<T> orderByAggregation(SFunction<T,Object> field,String aggregationType) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName);
		columnQuery.setPrefix(aggregationType+"(");
		columnQuery.setSuffix(")");
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderByAggregation(String columnName,String aggregationType) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName);
		columnQuery.setPrefix(aggregationType+"(");
		columnQuery.setSuffix(")");
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderByAggregationDesc(SFunction<T,Object> field,String aggregationType) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName);
		columnQuery.setPrefix(aggregationType+"(");
		columnQuery.setSuffix(") DESC");
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderByAggregationDesc(String columnName,String aggregationType) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName);
		columnQuery.setPrefix(aggregationType+"(");
		columnQuery.setSuffix(") DESC");
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> page(int pageIndex,int pageSize) {
		this.skipSize=(pageIndex-1)*pageSize;
		this.takeSize=pageSize;
		return this;
	}
	
	public TableQueryBuilder<T> take(int dataSize) {
		this.takeSize=dataSize;
		return this;
	}
	
	public TableQueryBuilder<T> skip(int skipSize) {
		this.skipSize=skipSize;
		return this;
	}
	
	
	public QueryBuilder<T> build() {
		return new QueryBuilder<T>(this);
	}
	
	public <Z> QueryBuilder<Z> build(Class<Z> outputClazz) {
		return new QueryBuilder<Z>(this,outputClazz);
	}
	 
	public List<ConditionBuilder<T>> getConditions() {
		return conditions;
	}

	
	public List<ColumnQueryBuilder<T>> getSelectColumns() {
		return selectColumns;
	}

	public List<JoinTableConditionInternal<T, ?>> getJoinList() {
		return joinList;
	}

	public List<ColumnQueryBuilder<T>> getGroupByColumns() {
		return groupByColumns;
	}

	public List<ConditionBuilder<T>> getHavingConditions() {
		return havingConditions;
	}

	public Integer getSkipSize() {
		return skipSize;
	}

	public Integer getTakeSize() {
		return takeSize;
	}
	
	public List<ColumnQueryBuilder<T>> getOrderByColumns() {
		return orderByColumns;
	}

	public static class JoinTableConditionInternal<L,R>{
		
		private String joinType;
		
		private TableQueryBuilder<R> table;
		
		private JoinConditionBuilder<L,R> condtion;
		
		public JoinTableConditionInternal(TableQueryBuilder<R> table,JoinConditionBuilder<L,R> condtion,String joinType) {
			this.table=table;
			this.condtion=condtion;
			this.joinType=joinType;
		}

		public TableQueryBuilder<R> getTable() {
			return table;
		}

		public JoinConditionBuilder<L, R> getCondtion() {
			return condtion;
		}

		public String getJoinType() {
			return joinType;
		}
	}
}
