package com.github.rexsheng.mybatis.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.SelectProvider;

import com.github.rexsheng.mybatis.extension.QueryBuilder;
import com.github.rexsheng.mybatis.provider.DynamicSqlProvider;

/**
 * Mybatis扩展接口
 * @author RexSheng
 * 2020年8月28日 下午3:45:11
 */
public interface DynamicMapper {

	@SelectProvider(type = DynamicSqlProvider.class,method = "selectByBuilder")
	<T> List<T> selectByBuilder(QueryBuilder<T> builder);
	
	@SelectProvider(type = DynamicSqlProvider.class,method = "selectBySql")
	<T> List<T> selectBySql(String sql,Class<T> clazz);
	
	@SelectProvider(type = DynamicSqlProvider.class,method = "selectBySql")
	@ResultType(Map.class)
	List<Map<String,Object>> selectByMap(String sql);
	
//	@SelectProvider(type = DynamicSqlProvider.class,method = "selectByBuilder")
//	<T> PagedList<T> selectByPage(QueryBuilder<T> builder);
//	
//	@InsertProvider(type = DynamicSqlProvider.class,method = "insertBatch")
//	<T> int insertBatch(@Param("list") List<T> list);
}
