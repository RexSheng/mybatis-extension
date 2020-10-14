package com.github.rexsheng.mybatis.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import com.github.rexsheng.mybatis.extension.QueryBuilder;
import com.github.rexsheng.mybatis.provider.DynamicSqlProvider;

/**
 * Mybatis扩展接口
 * @author RexSheng
 * 2020年8月28日 下午3:45:11
 */
public interface DynamicMapper {

	/**
	 * 根据构造条件查询数据
	 * @param <T> 返回数据列表类型
	 * @param builder 构造条件
	 * @return 列表数据
	 */
	@SelectProvider(type = DynamicSqlProvider.class,method = "selectByBuilder")
	<T> List<T> selectByBuilder(QueryBuilder<T> builder);
	
	/**
	 * 根据传入的sql及返回类型执行查询
	 * @param <T> 返回数据列表类型
	 * @param sql 要执行查询的sql
	 * @param clazz 返回数据列表类型
	 * @return 列表数据
	 */
	@SelectProvider(type = DynamicSqlProvider.class,method = "selectBySql")
	<T> List<T> selectBySql(String sql,Class<T> clazz);
	
	/**
	 * 根据传入的sql查询总条数
	 * @param sql 计算总条数的sql
	 * @return sql结果
	 * @since 1.1.2
	 */
	@SelectProvider(type = DynamicSqlProvider.class,method = "selectBySql")
	long countBySql(String sql);

	/**
	 * 根据传入的sql、参数及返回类型执行查询
	 * <p>
	 *  代码示例：
	 * <pre>
	 * Map&lt;String,Object&gt; paramMap=new HashMap&lt;&gt;();
	 * 	paramMap.put("limit", 3);
	 * 	paramMap.put("userId", 10);
	 * 	paramMap.put("create_time", new Date());
	 * 	paramMap.put("userName", "%姓名%");
	 * 	paramMap.put("orderByClause", " create_time desc ");
	 * 	paramMap.put("userIdList", Arrays.asList(13,14,15));
	 * 	List&lt;TUser&gt; userList = dao.selectBySqlWithParams("Select user_id,user_name as userName,create_time as createTime from t_user "+
	 * 	" where user_id&gt;=#{userId} and user_id not in #{userIdList} and create_time&lt; #{create_time} order by ${orderByClause} limit #{limit}",paramMap,TUser.class);
	 * </pre>
	 * @param <T> 返回数据列表类型
	 * @param sql 要执行查询的sql
	 * @param params 参数
	 * @param clazz 返回数据列表类型
	 * @return 列表数据
	 * @since 1.1.1
	 */
	@SelectProvider(type = DynamicSqlProvider.class,method = "selectBySqlWithParams")
	<T> List<T> selectBySqlWithParams(@Param("sql") String sql,@Param("params") Map<String,Object> params,@Param("clazz") Class<T> clazz);
	
	/**
	 * 根据传入的sql、参数查询总条数
	 * @param sql 计算总条数的sql
	 * @param params 参数
	 * @return sql结果
	 * @since 1.1.2
	 */
	@SelectProvider(type = DynamicSqlProvider.class,method = "selectBySqlWithParams")
	long countBySqlWithParams(@Param("sql") String sql,@Param("params") Map<String,Object> params);
	
	/**
	 * 根据传入的sql返回map结果
	 * @param sql 要执行查询的sql
	 * @return 列表数据
	 */
	@SelectProvider(type = DynamicSqlProvider.class,method = "selectBySql")
	@ResultType(Map.class)
	List<Map<String,Object>> selectByMap(String sql);
	
	/**
	 * 根据传入的sql、参数返回map结果
	 *  <p>
	 *  代码示例：
	 * <pre>
	 *  Map&lt;String,Object&gt; paramMap=new HashMap&lt;&gt;();
	 * 	paramMap.put("limit", 3);
	 * 	paramMap.put("userId", 10);
	 * 	paramMap.put("create_time", new Date());
	 * 	paramMap.put("orderByClause", " create_time desc ");
	 * 	paramMap.put("userIdList", Arrays.asList(13,14,15));
	 * 	List&lt;Map&lt;String,Object&gt;&gt; userList = dao.selectByMapWithParams("Select user_id,user_name as userName,create_time as createTime from t_user "+
	 * 	" where user_id&gt;=#{userId} and user_id not in #{userIdList} and create_time&lt; #{create_time} order by ${orderByClause} limit #{limit}",paramMap);
	 * </pre>
	 * @param sql 要执行查询的sql
	 * @param params 参数
	 * @return 列表数据
	 * @since 1.1.1
	 */
	@SelectProvider(type = DynamicSqlProvider.class,method = "selectBySqlWithParams")
	@ResultType(Map.class)
	List<Map<String,Object>> selectByMapWithParams(@Param("sql") String sql,@Param("params") Map<String,Object> params);

	/**
	 * 批量新增
	 * @param <T> 数据类型
	 * @param list 要新增的数据列表
	 * @return 影响的行数
	 * @since 1.0.1
	 */
	@InsertProvider(type = DynamicSqlProvider.class,method = "insertBatch")
	<T> int insertBatch(@Param("list") List<T> list);
	
	/**
	 * 根据构造条件批量更新数据
	 * @param <T> 数据类型
	 * @param builder 构造条件
	 * @return 影响的行数
	 * @since 1.1.2
	 */
	@UpdateProvider(type = DynamicSqlProvider.class,method = "updateByBuilder")
	<T> int updateByBuilder(QueryBuilder<T> builder);
	
	/**
	 * 根据构造条件批量删除数据
	 * @param <T> 数据类型
	 * @param builder 构造条件
	 * @return 影响的行数
	 * @since 1.1.2
	 */
	@DeleteProvider(type = DynamicSqlProvider.class,method = "deleteByBuilder")
	<T> int deleteByBuilder(QueryBuilder<T> builder);
}
