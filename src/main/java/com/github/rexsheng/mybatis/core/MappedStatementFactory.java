package com.github.rexsheng.mybatis.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author RexSheng
 * 2020年8月30日 下午11:34:32
 */
public class MappedStatementFactory {
	
	private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<ResultMapping>(0);

	/**
	 * 根据现有的 ms 创建一个新的，使用新的返回值类型
	 * @param ms MappedStatement
	 * @param resultType 新的类型
	 * @return 根据现有的 ms 创建一个新的
	 */
	public static MappedStatement changeMappedStatementResultType(MappedStatement ms, Class<?> resultType) {
        return changeMappedStatementResultType(ms,ms.getSqlSource(),resultType);
    }
	
	public static MappedStatement changeMappedStatementResultType(MappedStatement ms, Class<?> resultType,Boolean changeId) {
        return changeMappedStatementResultType(ms,ms.getSqlSource(),resultType,changeId);
    }
	
	public static MappedStatement changeMappedStatementResultType(MappedStatement ms,SqlSource sqlSource, Class<?> resultType) {
        return changeMappedStatementResultType(ms, sqlSource,resultType,true);
    }
	
	public static MappedStatement changeMappedStatementResultType(MappedStatement ms,SqlSource sqlSource, Class<?> resultType,Boolean changeId) {
        //下面是新建的过程，考虑效率和复用对象的情况下，这里最后生成的ms可以缓存起来，下次根据 ms.getId() + "_" + getShortName(resultType) 直接返回 ms,省去反复创建的过程
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + (changeId?("[" + getShortName(resultType) +"]"):""), sqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
//        自定义resultMapping
//        List<ResultMapping> resultMappingList=new ArrayList<>();
//        resultMappingList.add(new ResultMapping.Builder(ms.getConfiguration(), "pageIndex","page_index",java.lang.Integer.class).build());
//        resultMappingList.add(new ResultMapping.Builder(ms.getConfiguration(), "pageSize","page_size",java.lang.Integer.class).build());
//        resultMappingList.add(new ResultMapping.Builder(ms.getConfiguration(), "totalItemCount","total_item_count",java.lang.Long.class).build());
//        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), resultType, resultMappingList).build();
        //count查询返回值int
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), resultType, EMPTY_RESULTMAPPING).build();
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }
	
	private static String getShortName(Class<?> clazz){
        String className = clazz.getCanonicalName();
        return className.substring(className.lastIndexOf(".") + 1);
    }
}
