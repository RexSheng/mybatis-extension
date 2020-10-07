package com.github.rexsheng.mybatis.core;

import java.util.List;

/**
 * 分页列表
 * @author RexSheng
 * 2020年9月7日 上午12:08:10
 */
public class PagedList<T> implements IPagedList<T>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer pageIndex;
	
	private Integer pageSize;
	
	private Integer totalPageCount;
	
	private Long totalItemCount;
	
	private List<T> dataList;
	
	public PagedList() {}
	
	public PagedList(List<T> dataList,Integer pageIndex,Integer pageSize,Long totalItemCount) {
		this.dataList=dataList;
		this.pageIndex=pageIndex;
		this.pageSize=pageSize;
		this.totalPageCount=(int) (totalItemCount%pageSize>0?(totalItemCount/pageSize+1):totalItemCount/pageSize);
		this.totalItemCount=totalItemCount;
	}
	
	public PagedList(List<T> dataList,IPageInput pageInput,Long totalItemCount) {
		this(dataList,pageInput.getPageIndex(),pageInput.getPageSize(),totalItemCount);
	}
	
	public PagedList(Integer pageIndex,Integer pageSize,Long totalItemCount) {
		this(null,pageIndex,pageSize,totalItemCount);
	}
	
	public PagedList(IPageInput pageInput,Long totalItemCount) {
		this(pageInput.getPageIndex(),pageInput.getPageSize(),totalItemCount);
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
		this.totalPageCount=(int) (totalItemCount%pageSize>0?(totalItemCount/pageSize+1):totalItemCount/pageSize);
	}

	public Integer getTotalPageCount() {
		return totalPageCount;
	}

	public void setTotalPageCount(Integer totalPageCount) {
		this.totalPageCount = totalPageCount;
	}

	public List<T> getDataList() {
		return dataList;
	}

	public void setDataList(List<T> dataList) {
		this.dataList=dataList;
	}
	
	
}
