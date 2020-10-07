package com.github.rexsheng.mybatis.core;

/**
 * @author RexSheng
 * 2020年6月21日 下午2:02:24
 */
public interface IPageInput {
	
	/**
	 *  页码，从1开始
	 * @return 要查询的页码
	 */
	default Integer getPageIndex() {
		return 1;
	}

	/**
	 *  页大小
	 * @return 每页的数据量
	 */
	Integer getPageSize();
}
