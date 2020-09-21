package com.github.rexsheng.mybatis.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RexSheng
 * 2020年9月7日 上午12:08:10
 */
public class PagedList<T> extends ArrayList<T>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer pageIndex;
	
	private Integer pageSize;
	
	private Integer totalPageCount;
	
	private Long totalItemCount;
	
	public PagedList() {}
	
	public PagedList(List<T> dataList,Integer pageIndex,Integer pageSize,Long totalItemCount) {
		this.addAll(dataList);
		this.pageIndex=pageIndex;
		this.pageSize=pageSize;
		this.totalPageCount=(int) (totalItemCount%pageSize>0?(totalItemCount/pageSize+1):totalItemCount/pageSize);
		this.totalItemCount=totalItemCount;
	}
	
	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Long getTotalItemCount() {
		return totalItemCount;
	}

	public void setTotalItemCount(Long totalItemCount) {
		this.totalItemCount = totalItemCount;
	}

	public Integer getTotalPageCount() {
		return totalPageCount;
	}

	public void setTotalPageCount(Integer totalPageCount) {
		this.totalPageCount = totalPageCount;
	}
	
	
}
