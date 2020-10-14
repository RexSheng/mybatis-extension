package com.github.rexsheng.mybatis.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.rexsheng.mybatis.core.SFunction;
import com.github.rexsheng.mybatis.util.ReflectUtil;
import com.github.rexsheng.mybatis.util.StringUtils;

/**
 * @author RexSheng 2020年8月27日 下午8:07:18
 */
public class ConditionBuilder<T> extends EntityInfo<T>{

	private List<WhereConditionBuilder<T>> whereConditions;
	
	private List<ConditionBuilder<T>> innerConditions;
	
	private Boolean isAnd;
	
	private Consumer<ConditionBuilder<T>> callback;
	
	public ConditionBuilder(Class<T> clazz) {
		this(clazz,null,true);
	}
	
	public ConditionBuilder(Class<T> clazz,Consumer<ConditionBuilder<T>> callback) {
		this(clazz,callback,true);
	}
	
	public ConditionBuilder(Class<T> clazz,Consumer<ConditionBuilder<T>> callback,Boolean isAnd) {
		super(clazz);
		this.isAnd=isAnd;
		this.whereConditions=new ArrayList<>();
		this.innerConditions=new ArrayList<>();
		this.callback=callback;
	}
	
	public <E> ConditionBuilder<T> and(){
		ConditionBuilder<T> condition=new ConditionBuilder<T>(super.getEntityClass(),this.callback);
		this.callback.accept(condition);
		return condition;
	}
	
	public <E> ConditionBuilder<T> or(){
		ConditionBuilder<T> condition=new ConditionBuilder<T>(super.getEntityClass(),this.callback,false);
		this.callback.accept(condition);
		return condition;
	}
	
	public <E> ConditionBuilder<T> eq(SFunction<T,E> column,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("=");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> eq(SFunction<T,E> column,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("=");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> eq(String columnName,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("=");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> eq(String columnName,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("=");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> notEq(SFunction<T,E> column,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("<>");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> notEq(SFunction<T,E> column,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("<>");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> notEq(String columnName,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("<>");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> notEq(String columnName,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("<>");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> isNull(SFunction<T,E> column){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setRelation("IS NULL");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> isNull(SFunction<T,E> column,Predicate<E> when){
		if(when.test(null)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setRelation("IS NULL");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> isNull(String columnName){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setRelation("IS NULL");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> isNull(String columnName,Predicate<E> when){
		if(when.test(null)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setRelation("IS NULL");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> isNotNull(SFunction<T,E> column){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setRelation("IS NOT NULL");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> isNotNull(SFunction<T,E> column,Predicate<E> when){
		if(when.test(null)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setRelation("IS NOT NULL");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> isNotNull(String columnName){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setRelation("IS NOT NULL");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> isNotNull(String columnName,Predicate<E> when){
		if(when.test(null)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setRelation("IS NOT NULL");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> gt(SFunction<T,E> column,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation(">");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> gt(SFunction<T,E> column,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation(">");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> gt(String columnName,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation(">");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> gt(String columnName,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation(">");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> gte(SFunction<T,E> column,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation(">=");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> gte(SFunction<T,E> column,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation(">=");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> gte(String columnName,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation(">=");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> gte(String columnName,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation(">=");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> lt(SFunction<T,E> column,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("<");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> lt(SFunction<T,E> column,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("<");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> lt(String columnName,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("<");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> lt(String columnName,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("<");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> lte(SFunction<T,E> column,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("<=");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> lte(SFunction<T,E> column,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("<=");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> lte(String columnName,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("<=");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> lte(String columnName,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("<=");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	
	public <E> ConditionBuilder<T> like(SFunction<T,E> column,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("like");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> like(SFunction<T,E> column,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("like");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> like(String columnName,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("like");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> like(String columnName,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("like");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	/**
	 * not like 
	 * @param <E> value类型
	 * @param column 列名
	 * @param value 值
	 * @return 当前条件
	 * @since 1.1.2
	 */
	public <E> ConditionBuilder<T> notLike(SFunction<T,E> column,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("not like");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	/**
	 * not like
	 * @param <E> value类型
	 * @param column 列名
	 * @param value 值
	 * @param when 预判条件
	 * @return 当前条件
	 * @since 1.1.2
	 */
	public <E> ConditionBuilder<T> notLike(SFunction<T,E> column,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("not like");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	/**
	 * not like
	 * @param <E> value类型
	 * @param columnName 列名
	 * @param value 值
	 * @return 当前条件
	 * @since 1.1.2
	 */
	public <E> ConditionBuilder<T> notLike(String columnName,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("not like");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	/**
	 * not like
	 * @param <E> value类型
	 * @param columnName 列名
	 * @param value 值
	 * @param when 预判条件
	 * @return 当前条件
	 * @since 1.1.2
	 */
	public <E> ConditionBuilder<T> notLike(String columnName,E value,Predicate<E> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setValue(value);
			condition.setRelation("not like");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> in(SFunction<T,E> column,List<E> value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setListValue(true);
		condition.setValue(value);
		condition.setRelation("in");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> in(SFunction<T,E> column,List<E> value,Predicate<List<E>> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setListValue(true);
			condition.setValue(value);
			condition.setRelation("in");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	public <E> ConditionBuilder<T> in(String columnName,List<E> value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setListValue(true);
		condition.setValue(value);
		condition.setRelation("in");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	public <E> ConditionBuilder<T> in(String columnName,List<E> value,Predicate<List<E>> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setListValue(true);
			condition.setValue(value);
			condition.setRelation("in");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	/**
	 * not in
	 * @param <E> 列表元素类型
	 * @param column 列名
	 * @param value 列表值
	 * @return 当前条件
	 * @since 1.1.2
	 */
	public <E> ConditionBuilder<T> notIn(SFunction<T,E> column,List<E> value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setListValue(true);
		condition.setValue(value);
		condition.setRelation("not in");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
		this.whereConditions.add(condition);
		return this;
	}
	
	/**
	 * not in
	 * @param <E> 列表元素类型
	 * @param column 列名
	 * @param value 列表值
	 * @param when 预判条件,传入Predicate的为当前方法的value参数
	 * @return 当前条件
	 * @since 1.1.2
	 */
	public <E> ConditionBuilder<T> notIn(SFunction<T,E> column,List<E> value,Predicate<List<E>> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setListValue(true);
			condition.setValue(value);
			condition.setRelation("not in");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(column)));
			this.whereConditions.add(condition);
		}
		return this;
	}
	
	/**
	 * not in
	 * @param <E> 列表元素类型
	 * @param columnName 列名
	 * @param value 列表值
	 * @return 当前条件
	 * @since 1.1.2
	 */
	public <E> ConditionBuilder<T> notIn(String columnName,List<E> value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
		condition.setListValue(true);
		condition.setValue(value);
		condition.setRelation("not in");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
		this.whereConditions.add(condition);
		return this;
	}
	
	/**
	 * not in
	 * @param <E> 列表元素类型
	 * @param columnName 列名
	 * @param value 列表值
	 * @param when 预判条件,传入Predicate的为当前方法的value参数
	 * @return 当前条件
	 * @since 1.1.2
	 */
	public <E> ConditionBuilder<T> notIn(String columnName,List<E> value,Predicate<List<E>> when){
		if(when.test(value)) {
			WhereConditionBuilder<T> condition=new WhereConditionBuilder<T>(super.getEntityClass());
			condition.setListValue(true);
			condition.setValue(value);
			condition.setRelation("not in");//$NON-NLS-1$
			condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),StringUtils.underlineToCamelCase(columnName),null,columnName));
			this.whereConditions.add(condition);
		}
		return this;
	}
	

	public List<WhereConditionBuilder<T>> getWhereConditions() {
		return whereConditions;
	}

	public Boolean getIsAnd() {
		return isAnd;
	}

	public void setIsAnd(Boolean isAnd) {
		this.isAnd = isAnd;
	}

	public List<ConditionBuilder<T>> getInnerConditions() {
		return innerConditions;
	}

	   
	 
}
