package com.github.rexsheng.mybatis.extension;

import java.util.ArrayList;
import java.util.List;

import com.github.rexsheng.mybatis.core.SFunction;
import com.github.rexsheng.mybatis.util.ReflectUtil;

/**
 * @author RexSheng 2020年8月27日 下午8:07:18
 */
public class JoinConditionBuilder<L,R> extends EntityInfo<L>{
	
	private List<JoinColumnsInternal<L,R>> conditions;
	
	private List<JoinColumnsInternal<L,R>> conditionsForUpdate;
	
	private Class<R> rightClazz;

	public JoinConditionBuilder(Class<L> leftClazz,Class<R> rightClazz) {
		super(leftClazz);
		this.rightClazz=rightClazz;
		this.conditions=new ArrayList<>();
		this.conditionsForUpdate=new ArrayList<>();
	}

	public JoinConditionBuilder<L,R> on(SFunction<L,Object> leftColumn,SFunction<R,Object> rightColumn){
		ColumnQueryBuilder<L> left=new ColumnQueryBuilder<L>(super.getEntityClass(),ReflectUtil.fnToFieldName(leftColumn));
		ColumnQueryBuilder<R> right=new ColumnQueryBuilder<R>(rightClazz,ReflectUtil.fnToFieldName(rightColumn));
		JoinColumnsInternal<L,R> ref=new JoinColumnsInternal<>(left,right);
		this.conditions.add(ref);
		return this;
	}
	
	/**
	 * update语句中设置左列值=右列值
	 * @param leftColumn 左表列
	 * @param rightColumn 右表列
	 * @return 当前条件
	 * @since 1.1.2
	 */
	public JoinConditionBuilder<L,R> setColumnValue(SFunction<L,Object> leftColumn,SFunction<R,Object> rightColumn){
		ColumnQueryBuilder<L> left=new ColumnQueryBuilder<L>(super.getEntityClass(),ReflectUtil.fnToFieldName(leftColumn));
		ColumnQueryBuilder<R> right=new ColumnQueryBuilder<R>(rightClazz,ReflectUtil.fnToFieldName(rightColumn));
		JoinColumnsInternal<L,R> ref=new JoinColumnsInternal<>(left,right);
		this.conditionsForUpdate.add(ref);
		return this;
	}
	
	public List<JoinColumnsInternal<L, R>> getConditions() {
		return conditions;
	}
	
	public Class<R> getRightClazz() {
		return rightClazz;
	}
	
	public List<JoinColumnsInternal<L,R>> getConditionsForUpdate() {
		return conditionsForUpdate;
	}

	public static class JoinColumnsInternal<L,R>{
		private ColumnQueryBuilder<L> leftColumn;

		private ColumnQueryBuilder<R> rightColumn;
		
		private String relation;
		
		public JoinColumnsInternal(ColumnQueryBuilder<L> leftColumn,ColumnQueryBuilder<R> rightColumn) {
			this.leftColumn=leftColumn;
			this.rightColumn=rightColumn;
			this.relation="=";
		}

		public ColumnQueryBuilder<L> getLeftColumn() {
			return leftColumn;
		}

		 

		public ColumnQueryBuilder<R> getRightColumn() {
			return rightColumn;
		}

		public String getRelation() {
			return relation;
		}

		 
		
	}
	 
}
