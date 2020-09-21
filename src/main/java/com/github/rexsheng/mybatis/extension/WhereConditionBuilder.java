package com.github.rexsheng.mybatis.extension;

/**
 * @author RexSheng 2020年8月27日 下午8:07:18
 */
public class WhereConditionBuilder<T> extends EntityInfo<T>{

	private ColumnQueryBuilder<T> column;
	
	private String relation;
	
	private Boolean isValid;
	
	private Boolean hasValue;
	
	private Boolean listValue;
	
	private Object value;
	
	

	public WhereConditionBuilder(Class<T> clazz) {
		super(clazz);
		this.isValid=true;
		this.hasValue=false;
		this.listValue=false;
	}

	public ColumnQueryBuilder<T> getColumn() {
		return column;
	}

	public void setColumn(ColumnQueryBuilder<T> column) {
		this.column = column;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
		this.hasValue=true;
	}

	public Boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	public Boolean getHasValue() {
		return hasValue;
	}

	public Boolean getListValue() {
		return listValue;
	}

	public void setListValue(Boolean listValue) {
		this.listValue = listValue;
	}

	 
	 
}
