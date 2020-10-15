package com.github.rexsheng.mybatis.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.github.rexsheng.mybatis.core.SFunction;
import com.github.rexsheng.mybatis.util.ReflectUtil;

/**
 * @author RexSheng
 * 2020年10月13日 下午11:55:12
 */
public class TableUpdateBuilder<T> extends TableQueryBuilder<T> {
	
	private List<WhereConditionBuilder<T>> updateColumns;

	public TableUpdateBuilder(Class<T> clazz) {
		super(clazz);
		this.updateColumns=new ArrayList<>();
	}
	
	public TableUpdateBuilder(Class<T> clazz, String tableName) {
		super(clazz, tableName);
		this.updateColumns=new ArrayList<>();
	}
	
	public static <T> TableUpdateBuilder<T> from(Class<T> clazz){
		return new TableUpdateBuilder<T>(clazz);
	}
	
	public static <T> TableUpdateBuilder<T> from(Class<T> clazz,String tableName){
		return new TableUpdateBuilder<T>(clazz,tableName);
	}	
	
	public <E> TableUpdateBuilder<T> setValue(SFunction<T,E> field,E value){
		WhereConditionBuilder<T> condition=new WhereConditionBuilder<>(super.getEntityClass());
		condition.setValue(value);
		condition.setRelation("=");//$NON-NLS-1$
		condition.setColumn(new ColumnQueryBuilder<T>(super.getEntityClass(),ReflectUtil.fnToFieldName(field)));
		this.updateColumns.add(condition);
		return this;
	}
	
	public <E> TableUpdateBuilder<T> setValue(SFunction<T,E> field,E value,Predicate<E> when){
		if(when.test(value)) {
			setValue(field,value);
		}
		return this;
	}
	
	public <E> TableUpdateBuilder<T> setValueNull(SFunction<T,E> field){
		return setValue(field,null);
	}
	
	public List<WhereConditionBuilder<T>> getUpdateColumns() {
		return updateColumns;
	}

}
