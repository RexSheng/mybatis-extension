package com.github.rexsheng.mybatis.config;

import java.util.Arrays;
import java.util.List;

import com.github.rexsheng.mybatis.handler.DefaultColumnHandler;
import com.github.rexsheng.mybatis.handler.DefaultTableHandler;
import com.github.rexsheng.mybatis.handler.IColumnHandler;
import com.github.rexsheng.mybatis.handler.ITableHandler;

/**
 * 全局配置
 * @author RexSheng 2020年8月31日 下午10:50:28
 */
public class BuilderConfiguration {

	private String beginDelimiter;

	private String endDelimiter;
	
	private String dbType;
	
	private int maxInLength;
	
	private ITableHandler tableHandler;
	
	private IColumnHandler columnHandler;
	
	private final List<String> DB_TYPE_LIST= Arrays.asList("mysql","oracle","sqlserver");//$NON-NLS-1$

	public BuilderConfiguration() {
		this.beginDelimiter="";//$NON-NLS-1$
		this.endDelimiter="";//$NON-NLS-1$
		this.dbType="mysql";//$NON-NLS-1$
		this.maxInLength=-1;
		this.tableHandler=new DefaultTableHandler();
		this.columnHandler=new DefaultColumnHandler();
	}

	public String getBeginDelimiter() {
		return beginDelimiter;
	}

	public void setBeginDelimiter(String beginDelimiter) {
		this.beginDelimiter = beginDelimiter;
	}

	public String getEndDelimiter() {
		return endDelimiter;
	}

	public void setEndDelimiter(String endDelimiter) {
		this.endDelimiter = endDelimiter;
	}

	public ITableHandler getTableHandler() {
		return tableHandler;
	}

	public void setTableHandler(ITableHandler tableHandler) {
		this.tableHandler = tableHandler;
	}

	public IColumnHandler getColumnHandler() {
		return columnHandler;
	}

	public void setColumnHandler(IColumnHandler columnHandler) {
		this.columnHandler = columnHandler;
	}

	public String getDbType() {
		return dbType;
	}
	
	public int getMaxInLength() {
		return maxInLength;
	}

	public void setMaxInLength(int maxInLength) {
		this.maxInLength = maxInLength;
	}

	/**
	 * 设置数据库类型，默认mysql,目前支持"mysql","oracle","sqlserver"三种
	 * @param dbType 数据库类型
	 * @since 1.1.0
	 */
	public void setDbType(String dbType) {
		if(dbType==null || !DB_TYPE_LIST.contains(dbType.toLowerCase())) {
			throw new RuntimeException("无效的数据库类型，请指定为"+String.join("、", DB_TYPE_LIST)+"中的一种");//$NON-NLS-1$
		}
		this.dbType = dbType;
	}
	
	@Override
	public String toString() {
		return "{beginDelimiter=" + beginDelimiter + ", endDelimiter=" + endDelimiter + ", dbType=" + dbType + "}";
	}
	
	

}
