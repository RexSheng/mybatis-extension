package com.github.rexsheng.mybatis.config;

/**
 * @author RexSheng
 * 2020年11月20日 下午11:25:57
 * @since 1.3.0
 */
public class DialectProperty {
	
	private String dbType;
	
	private String beginDelimiter;
	
	private String endDelimiter;
	
	private int maxInLength;
	
	public DialectProperty() {
		this.maxInLength=-1;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
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

	public int getMaxInLength() {
		return maxInLength;
	}

	public void setMaxInLength(int maxInLength) {
		this.maxInLength = maxInLength;
	}

	@Override
	public String toString() {
		return "{beginDelimiter=" + beginDelimiter + ", endDelimiter=" + endDelimiter +
				", dbType=" + dbType + 
				", maxInLength=" + maxInLength + "}";
	}
}
