package com.github.rexsheng.mybatis.core;

/**
 * 分页接口
 * @author RexSheng
 * 2020年9月7日 上午12:08:10
 */
public interface IPagedList<T> extends java.io.Serializable{

	Integer getPageIndex();
	
	Integer getPageSize();

	Long getTotalItemCount();

	Integer getTotalPageCount();
}
