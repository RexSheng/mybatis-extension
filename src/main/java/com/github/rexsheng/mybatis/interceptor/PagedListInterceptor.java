package com.github.rexsheng.mybatis.interceptor;

import java.sql.Statement;
import java.util.Properties;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rexsheng.mybatis.util.ReflectUtil;

/**
 * @author RexSheng
 * 2020年8月28日 下午4:26:40
 */
@Intercepts(value = {
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = { Statement.class }) })
public class PagedListInterceptor implements Interceptor{

	private static Logger logger = LoggerFactory.getLogger(PagedListInterceptor.class);
	
	@SuppressWarnings("unused")
	private Properties properties=new Properties();
	
	@SuppressWarnings("unused")
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		
		ResultSetHandler resultSetHandler = (ResultSetHandler) invocation.getTarget();
		if(resultSetHandler==null) {
			return invocation.proceed();
		}
		final Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement)ReflectUtil.getFieldValue(resultSetHandler, "mappedStatement");
		//通过java反射获得mappedStatement属性值
//		MappedStatement ms = (MappedStatement) args[0];
		if(ms==null) {
			return invocation.proceed();
		}
		String methodName=ms.getId();
		logger.debug("methodName:{}",methodName);
		//String sql=boundSql.getSql();
		//List<ResultMap> rms = ms.getResultMaps();
		//ResultMap rm = rms != null && rms.size() > 0 ? rms.get(0) : null;
		//String type = rm != null && rm.getType() != null ? rm.getType().getName() : "";  
		//Object[] args = invocation.getArgs();
        // 获取到当前的Statement
		if(methodName.endsWith(".selectByPageBuilder")) {
			return invocation.proceed();
		}
		else {
			return invocation.proceed();
		}
	}
	
	 
	@Override
	public Object plugin(Object target) {
		//只对要拦截的对象生成代理
        if(target instanceof ResultSetHandler){
            //调用插件
            return Plugin.wrap(target, this);
        }
        return target;
	}
	
	@Override
	public void setProperties(Properties properties) {
		this.properties=properties;
	}
	

}

 
