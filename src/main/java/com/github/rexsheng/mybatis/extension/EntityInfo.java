package com.github.rexsheng.mybatis.extension;

/**
 * @author RexSheng
 * 2020年8月27日 下午9:22:20
 */
public class EntityInfo<T> {
	
	private Class<T> entityClass;
	
	private String tableName;
	
	public EntityInfo() {
		
	}
	
	public EntityInfo(Class<T> entityClass) {
		this.entityClass=entityClass;
	}
	
	public EntityInfo(Class<T> entityClass,String tableName) {
		this.entityClass=entityClass;
		this.tableName=tableName;
	}
	
	public Class<T> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
}
