package com.github.rexsheng.mybatis.extension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.rexsheng.mybatis.config.IDatabaseDialect;
import com.github.rexsheng.mybatis.core.IPageInput;
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
	
	private Integer pageIndex;
	
	private Integer pageSize;
	
	private long totalItemCount;
	
	private Boolean calculateTotalCount;
	
	private Boolean distinct;
	
	private Boolean temporarySkipSelectIfCountZero;
			
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
		this.totalItemCount=0L;
		this.calculateTotalCount=false;
		this.distinct=false;
	}

	/**
	 * distinct
	 * @return Boolean
	 * @since 1.2.0
	 */
	public TableQueryBuilder<T> distinct() {
		this.distinct = true;
		return this;
	}
	
	public TableQueryBuilder<T> selectAll() {
		List<Field> fields=ReflectUtil.getDeclaredFields(super.getEntityClass());
		for(Field field:fields) {
			ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),field.getName());
			this.selectColumns.add(columnQuery);
		}
		return this;
	}
	
	/**
	 * 查询满足条件的字段
	 * same as {@link TableQueryBuilder#selectField(Predicate)}
	 * @param filter 满足的条件
	 * @return 当前条件
	 */
	public TableQueryBuilder<T> select(Predicate<Field> filter) {
		return selectField(filter);
	}
	
	/**
	 * 查询满足条件的字段
	 * @param filter 满足的条件
	 * @return 当前条件
	 * @since 1.2.2
	 */
	public TableQueryBuilder<T> selectField(Predicate<Field> filter) {
		List<Field> fields=ReflectUtil.getDeclaredFields(super.getEntityClass());
		fields.stream().filter(filter).forEach(field->{
			ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),field.getName());
			this.selectColumns.add(columnQuery);
		});
		return this;
	}
	
	/**
	 * 查询不满足条件的字段
	 * @param filter 要排除的字段满足的条件
	 * @return 当前条件
	 * @since 1.2.2
	 */
	public TableQueryBuilder<T> selectExcept(Predicate<Field> filter) {
		List<Field> fields=ReflectUtil.getDeclaredFields(super.getEntityClass());
		fields.stream().filter(a->!filter.test(a)).forEach(field->{
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
	
	public TableQueryBuilder<T> selectAs(SFunction<T,Object> field,String aliasName) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName,aliasName);
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectAs(String columnName,String aliasName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),null,aliasName,columnName);
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectSum(SFunction<T,Object> field,String aliasName) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName,aliasName);
		columnQuery.setPrefix("SUM(");//$NON-NLS-1$
		columnQuery.setSuffix(")");//$NON-NLS-1$
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectSum(String columnName,String aliasName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),null,aliasName,columnName);
		columnQuery.setPrefix("SUM(");//$NON-NLS-1$
		columnQuery.setSuffix(")");//$NON-NLS-1$
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectCount(SFunction<T,Object> field,String aliasName) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName,aliasName);
		columnQuery.setPrefix("COUNT(");//$NON-NLS-1$
		columnQuery.setSuffix(")");//$NON-NLS-1$
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectCount(String columnName,String aliasName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),null,aliasName,columnName);
		columnQuery.setPrefix("COUNT(");//$NON-NLS-1$
		columnQuery.setSuffix(")");//$NON-NLS-1$
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectMax(SFunction<T,Object> field,String aliasName) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName,aliasName);
		columnQuery.setPrefix("MAX(");//$NON-NLS-1$
		columnQuery.setSuffix(")");//$NON-NLS-1$
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectMax(String columnName,String aliasName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),null,aliasName,columnName);
		columnQuery.setPrefix("MAX(");//$NON-NLS-1$
		columnQuery.setSuffix(")");//$NON-NLS-1$
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectMin(SFunction<T,Object> field,String aliasName) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName,aliasName);
		columnQuery.setPrefix("MIN(");//$NON-NLS-1$
		columnQuery.setSuffix(")");//$NON-NLS-1$
		this.selectColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> selectMin(String columnName,String aliasName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),null,aliasName,columnName);
		columnQuery.setPrefix("MIN(");//$NON-NLS-1$
		columnQuery.setSuffix(")");//$NON-NLS-1$
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
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),null,null,columnName);
		this.groupByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> havingSum(Consumer<ConditionBuilder<T>> field) {
		return having("SUM",field);//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> havingCount(Consumer<ConditionBuilder<T>> field) {
		return having("COUNT",field);//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> havingMax(Consumer<ConditionBuilder<T>> field) {
		return having("MAX",field);//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> havingMin(Consumer<ConditionBuilder<T>> field) {
		return having("MIN",field);//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> havingAvg(Consumer<ConditionBuilder<T>> field) {
		return having("AVG",field);//$NON-NLS-1$
	}
	
	/**
	 * 自定义聚合函数操作
	 * @param aggregationType 聚合类型，例如SUM,AVG,COUNT等
	 * @param field 条件构造器
	 * @return 当前条件
	 */
	public TableQueryBuilder<T> having(String aggregationType,Consumer<ConditionBuilder<T>> field) {
		ConditionBuilder<T> condition=new ConditionBuilder<>(super.getEntityClass());
		field.accept(condition);
		condition.getWhereConditions().forEach(a->{
			a.getColumn().setPrefix(aggregationType+"(");//$NON-NLS-1$
			a.getColumn().setSuffix(")");//$NON-NLS-1$
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
		JoinTableConditionInternal<T,R> internal=new JoinTableConditionInternal<>(relation,joinRelation,"left");//$NON-NLS-1$
		this.joinList.add(internal);
		return joinRelation;
	}
	
	public <R> JoinConditionBuilder<T,R> rightJoin(TableQueryBuilder<R> relation) {
		JoinConditionBuilder<T,R> joinRelation=new JoinConditionBuilder<>(super.getEntityClass(),relation.getEntityClass());
		JoinTableConditionInternal<T,R> internal=new JoinTableConditionInternal<>(relation,joinRelation,"right");//$NON-NLS-1$
		this.joinList.add(internal);
		return joinRelation;
	}
	
	public <R> JoinConditionBuilder<T,R> innerJoin(TableQueryBuilder<R> relation) {
		JoinConditionBuilder<T,R> joinRelation=new JoinConditionBuilder<>(super.getEntityClass(),relation.getEntityClass());
		JoinTableConditionInternal<T,R> internal=new JoinTableConditionInternal<>(relation,joinRelation,"inner");//$NON-NLS-1$
		this.joinList.add(internal);
		return joinRelation;
	}
	
	public <R> JoinConditionBuilder<T,R> join(TableQueryBuilder<R> relation) {
		JoinConditionBuilder<T,R> joinRelation=new JoinConditionBuilder<>(super.getEntityClass(),relation.getEntityClass());
		JoinTableConditionInternal<T,R> internal=new JoinTableConditionInternal<>(relation,joinRelation,"");//$NON-NLS-1$
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
		columnQuery.setSuffix(" DESC");//$NON-NLS-1$
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderByDesc(String columnName) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName);
		columnQuery.setSuffix(" DESC");//$NON-NLS-1$
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderBySum(SFunction<T,Object> field) {
		return orderByAggregation(field,"SUM");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderBySum(String columnName) {
		return orderByAggregation(columnName,"SUM");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderBySumDesc(SFunction<T,Object> field) {
		return orderByAggregationDesc(field,"SUM");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderBySumDesc(String columnName) {
		return orderByAggregationDesc(columnName,"SUM");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByCount(SFunction<T,Object> field) {
		return orderByAggregation(field,"COUNT");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByCount(String columnName) {
		return orderByAggregation(columnName,"COUNT");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByCountDesc(SFunction<T,Object> field) {
		return orderByAggregationDesc(field,"COUNT");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByCountDesc(String columnName) {
		return orderByAggregationDesc(columnName,"COUNT");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByMax(SFunction<T,Object> field) {
		return orderByAggregation(field,"MAX");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByMax(String columnName) {
		return orderByAggregation(columnName,"MAX");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByMaxDesc(SFunction<T,Object> field) {
		return orderByAggregationDesc(field,"MAX");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByMaxDesc(String columnName) {
		return orderByAggregationDesc(columnName,"MAX");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByMin(SFunction<T,Object> field) {
		return orderByAggregation(field,"MIN");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByMin(String columnName) {
		return orderByAggregation(columnName,"MIN");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByMinDesc(SFunction<T,Object> field) {
		return orderByAggregationDesc(field,"MIN");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByMinDesc(String columnName) {
		return orderByAggregationDesc(columnName,"MIN");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByAvg(SFunction<T,Object> field) {
		return orderByAggregation(field,"AVG");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByAvg(String columnName) {
		return orderByAggregation(columnName,"AVG");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByAvgDesc(SFunction<T,Object> field) {
		return orderByAggregationDesc(field,"AVG");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByAvgDesc(String columnName) {
		return orderByAggregationDesc(columnName,"AVG");//$NON-NLS-1$
	}
	
	public TableQueryBuilder<T> orderByAggregation(SFunction<T,Object> field,String aggregationType) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName);
		columnQuery.setPrefix(aggregationType+"(");//$NON-NLS-1$
		columnQuery.setSuffix(")");//$NON-NLS-1$
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderByAggregation(String columnName,String aggregationType) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName);
		columnQuery.setPrefix(aggregationType+"(");//$NON-NLS-1$
		columnQuery.setSuffix(")");//$NON-NLS-1$
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderByAggregationDesc(SFunction<T,Object> field,String aggregationType) {
		String fieldName=ReflectUtil.fnToFieldName(field);
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),fieldName);
		columnQuery.setPrefix(aggregationType+"(");//$NON-NLS-1$
		columnQuery.setSuffix(") DESC");//$NON-NLS-1$
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> orderByAggregationDesc(String columnName,String aggregationType) {
		ColumnQueryBuilder<T> columnQuery=new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName);
		columnQuery.setPrefix(aggregationType+"(");//$NON-NLS-1$
		columnQuery.setSuffix(") DESC");//$NON-NLS-1$
		this.orderByColumns.add(columnQuery);
		return this;
	}
	
	public TableQueryBuilder<T> page(IPageInput page) {
		this.pageIndex=page.getPageIndex();
		this.pageSize=page.getPageSize();
		return this;
	}
	
	public TableQueryBuilder<T> page(int pageIndex,int pageSize) {
		this.pageIndex=pageIndex;
		this.pageSize=pageSize;
		return this;
	}
	
	public TableQueryBuilder<T> take(int pageSize) {
		this.pageSize=pageSize;
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
		return getStartIndex();
	}
	
	/**
	 * 获取开始序号
	 * @return 开始序号
	 * @since 1.2.0
	 */
	public Integer getStartIndex() {
		if(skipSize!=null) {
			return skipSize;
		}
		else if(pageIndex!=null && pageIndex>0 && pageSize!=null) {
			return (pageIndex-1)*pageSize;
		}
		else {
			return null;
		}
	}
	
	/**
	 * 获取截止序号
	 * @return 截止序号
	 * @since 1.2.0
	 */
	public Integer getEndIndex() {
		if(pageSize!=null) {
			if(pageIndex!=null && pageIndex>0) {
				return pageIndex*pageSize;
			}
			else {
				return pageSize;
			}
		}
		return null;
	}

	public Integer getPageSize() {
		return pageSize;
	}
	
	public Integer getPageIndex() {
		return pageIndex;
	}
	
	public List<ColumnQueryBuilder<T>> getOrderByColumns() {
		return orderByColumns;
	}

	public long getTotalItemCount() {
		return totalItemCount;
	}

	public void setTotalItemCount(long totalItemCount) {
		this.totalItemCount = totalItemCount;
	}

	/**
	 * 是否在执行列表查询中同时计算总条数
	 * @see TableQueryBuilder#totalCountEnabled()
	 * @return true是，false否
	 */
	public Boolean getTotalCountEnabled() {
		return calculateTotalCount;
	}

	/**
	 * 允许在查询过程中同时计算影响的总行数
	 * 用于分页计算总条数，执行查询后调用方法{@link TableQueryBuilder#getTotalItemCount()}来获取总条数
	 * <p>
	 * 代码示例：
	 * <pre>
	 * int pageIndex=1; //要查询的页码
	 * int pageSize=10; //页大小
	 * TableQueryBuilder&lt;AuthUser&gt; query=TableQueryBuilder.from(AuthUser.class);
	 * query.selectAll().page(pageIndex, pageSize)
	 * .totalCountEnabled() //注意必须调用此方法，否则下方query.getTotalItemCount()无法获取总条数
	 * .where().gt(AuthUser::getUserId, 0);
	 * List&lt;AuthUser&gt; userList=dao.selectByBuilder(query.build());
	 * PagedList&lt;AuthUser&gt; pagedList=new PagedList&lt;&gt;(userList,pageIndex,pageSize,query.getTotalItemCount());
	 * logger.info(objectMapper.writeValueAsString(pagedList));
	 * </pre>
	 * @return 当前条件
	 */
	public TableQueryBuilder<T> totalCountEnabled() {
		this.calculateTotalCount = true;
		return this;
	}
	
	/**
	 * 允许在查询过程中同时计算影响的总行数
	 * @param skipSelectIfCountZero 是否在总行数为0时，不继续执行原有查询，忽略全局配置{@link IDatabaseDialect#skipSelectIfCountZero()} 
	 * true停止执行查询，false继续执行查询
	 * @return 当前条件
	 * @since 1.3.0
	 */
	public TableQueryBuilder<T> totalCountEnabled(Boolean skipSelectIfCountZero) {
		this.calculateTotalCount = true;
		this.temporarySkipSelectIfCountZero=skipSelectIfCountZero;
		return this;
	}
	
	/**
	 * 不查询总条数
	 * @return 当前条件
	 * @since 1.5.0
	 */
	public TableQueryBuilder<T> totalCountDisabled() {
		this.calculateTotalCount = false;
		return this;
	}

	public Boolean getDistinct() {
		return distinct;
	}

	public Boolean getTemporarySkipSelectIfCountZero() {
		return temporarySkipSelectIfCountZero;
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
