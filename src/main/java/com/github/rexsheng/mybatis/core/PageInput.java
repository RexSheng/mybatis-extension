package com.github.rexsheng.mybatis.core;

/**
 * @author RexSheng
 * 2020年6月21日 下午2:02:24
 */
public class PageInput implements IPageInput{

	protected Integer pageIndex;
	
	protected Integer pageSize;
	
	public PageInput(){}
	
	public PageInput(Integer pageIndex,Integer pageSize){
		this.pageIndex=pageIndex;
		this.pageSize=pageSize;
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
}
