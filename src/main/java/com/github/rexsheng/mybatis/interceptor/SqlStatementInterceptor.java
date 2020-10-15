package com.github.rexsheng.mybatis.interceptor;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rexsheng.mybatis.util.SQLFormatterUtil;

@Intercepts(value = {
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }) })
public class SqlStatementInterceptor implements Interceptor{

	private static Logger logger = LoggerFactory.getLogger(SqlStatementInterceptor.class);
	
	private Properties properties=new Properties();
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String sqlId = mappedStatement.getId();
        BoundSql boundSql=mappedStatement.getBoundSql(invocation.getArgs()[1]);
//        logger.info("===>sql:"+SQLFormatterUtil.formatSql(boundSql.getSql()));
        logger.debug("===>sql:"+SQLFormatterUtil.formatSql(showSql(mappedStatement.getConfiguration(),boundSql)));
        long start = System.currentTimeMillis();
        Object returnValue = invocation.proceed();
        long end = System.currentTimeMillis();
        logger.debug((properties.getProperty("prefix")!=null?properties.getProperty("prefix"):formatSqlId(sqlId))+":"+(end - start)+"ms");
        return returnValue;
        //修改sql
//        ReflectUtil.setFieldValue(boundSql, "sql", sql);
	}
	
	private String formatSqlId(String sqlId) {
		String[] arr=sqlId.split("\\.");
		
		if(arr.length<=2) {
			return sqlId;
		}
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<arr.length;i++) {
			String str=arr[i];
			if(i<arr.length-2) {
				sb.append(str.charAt(0));
			}
			else {
				sb.append(str);
			}
			if(i<arr.length-1) {
				sb.append(".");
			}
		}
		return sb.toString();
	}
	
	public static String showSql(Configuration configuration, BoundSql boundSql) {
		Object parameterObject = boundSql.getParameterObject();
		List<ParameterMapping> parameterMappings = boundSql
				.getParameterMappings();
		String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
		if (parameterMappings!=null && parameterMappings.size()>0 && parameterObject != null) {
			TypeHandlerRegistry typeHandlerRegistry = configuration
					.getTypeHandlerRegistry();
			if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
				sql = sql.replaceFirst("\\?",
						Matcher.quoteReplacement(getParameterValue(parameterObject)));
 
			} else {
				MetaObject metaObject = configuration
						.newMetaObject(parameterObject);
				for (ParameterMapping parameterMapping : parameterMappings) {
					String propertyName = parameterMapping.getProperty();
					if (metaObject.hasGetter(propertyName)) {
						Object obj = metaObject.getValue(propertyName);
						sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
					} else if (boundSql.hasAdditionalParameter(propertyName)) {
						Object obj = boundSql
								.getAdditionalParameter(propertyName);
						sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
					}else{sql=sql.replaceFirst("\\?","缺失");}//打印出缺失，提醒该参数缺失并防止错位
				}
			}
		}
		return sql;
	}
	
	private static String getParameterValue(Object obj) {
		String value = null;
		if (obj instanceof String) {
			value = "'" + obj.toString() + "'";
		} else if (obj instanceof Date) {
			value = "'" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(obj) + "'";
		} else {
			if (obj != null) {
				value = obj.toString();
			} else {
				value = "";
			}
 
		}
		return value;
	}
	
	@Override
	public Object plugin(Object target) {
		// TODO Auto-generated method stub
		return Plugin.wrap(target, this);
	}
	
	@Override
	public void setProperties(Properties properties) {
		this.properties=properties;
	}
	

}
