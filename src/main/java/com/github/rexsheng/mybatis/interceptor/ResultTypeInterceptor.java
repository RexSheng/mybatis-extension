package com.github.rexsheng.mybatis.interceptor;

import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rexsheng.mybatis.config.BuilderConfiguration;
import com.github.rexsheng.mybatis.core.MappedStatementFactory;
import com.github.rexsheng.mybatis.provider.DynamicSqlProvider;

/**
 * @author RexSheng
 * 2020年8月28日 下午4:26:40
 */
@Intercepts(@Signature(
        type = Executor.class,
        method = "query",
        args = {
            MappedStatement.class, 
            Object.class, 
            RowBounds.class, 
            ResultHandler.class
        }
    )
)
public class ResultTypeInterceptor implements Interceptor{

	private Logger logger=LoggerFactory.getLogger(DynamicSqlProvider.class); 
	
	private BuilderConfiguration builderConfig=new BuilderConfiguration();
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		final Object[] args = invocation.getArgs();
	    MappedStatement ms = (MappedStatement) args[0];
	    String methodName=ms.getId();
	    if(methodName!=null) {
	    	if(methodName.contains("com.github.rexsheng.mybatis.mapper.DynamicMapper.selectByBuilder")) {
	    		Object parameterObject = args[1];
			    //获取参数中设置的返回值类型
			    Class<?> resultType = getResultType(parameterObject,null);
			    if(resultType == null){
			        return invocation.proceed();
			    }
			    //复制ms，重设类型
			    args[0] = MappedStatementFactory.changeMappedStatementResultType(ms, resultType);
	    	}
	    	else if(methodName.contains("com.github.rexsheng.mybatis.mapper.DynamicMapper.selectBySql")) {
	    		Object parameterObject = args[1];
			    //获取参数中设置的返回值类型
	    		Class<?> resultType = getResultType(parameterObject,"arg1");
			    if(resultType == null){
			        return invocation.proceed();
			    }
			    //复制ms，重设类型
			    args[0] = MappedStatementFactory.changeMappedStatementResultType(ms, resultType);
	    	}
	    }	    
	    return invocation.proceed();
	}

	 
    /**
     * 获取设置的返回值类型
     *
     * @param parameterObject
     * @return
     */
    private Class<?> getResultType(Object parameterObject,String resultTypeKey){
        if (parameterObject == null) {
            return null;
        } 
        else if(parameterObject instanceof com.github.rexsheng.mybatis.extension.QueryBuilder){
        	com.github.rexsheng.mybatis.extension.QueryBuilder<?> queryBuilder=(com.github.rexsheng.mybatis.extension.QueryBuilder<?>)parameterObject;
        	queryBuilder.setBuiderConfig(builderConfig);
        	return queryBuilder.getOutputClazz();
        } else if (parameterObject instanceof Class) {
            return (Class<?>)parameterObject;
        } else if (parameterObject instanceof Map) {
            //解决不可变Map的情况
            if(((Map<?,?>)(parameterObject)).containsKey(resultTypeKey)){
                Object result = ((Map<?,?>)(parameterObject)).get(resultTypeKey);
                return objectToClass(result,resultTypeKey);
            } else {
                return null;
            }
        } else {
            MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
            Object result = metaObject.getValue(resultTypeKey);
            return objectToClass(result,resultTypeKey);
        }
    }

    /**
     * 将结果转换为Class
     *
     * @param object
     * @return
     */
    private Class<?> objectToClass(Object object,String resultTypeKey){
        if(object == null){
            return null;
        } else if(object instanceof Class){
            return (Class<?>)object;
        } else if(object instanceof String){
            try {
                return Class.forName((String)object);
            } catch (Exception e){
            	logger.error("非法的全限定类名字符串:" + object);
                throw new RuntimeException("非法的全限定类名字符串:" + object);
            }
        } else {
        	logger.error("方法参数类型错误，" + resultTypeKey + " 对应的参数类型只能为 Class 类型或者为 类的全限定名称字符串");
            throw new RuntimeException("方法参数类型错误，" + resultTypeKey + " 对应的参数类型只能为 Class 类型或者为 类的全限定名称字符串");
        }
    }

    @Override
    public Object plugin(Object target) {
//    	if(target instanceof Executor){
//            //调用插件
//            return Plugin.wrap(target, this);
//        }
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String beginDelimiter = properties.getProperty("beginDelimiter");
        if(beginDelimiter != null){
            builderConfig.setBeginDelimiter(beginDelimiter);
        }
        String endDelimiter = properties.getProperty("endDelimiter");
        if(beginDelimiter != null){
            builderConfig.setEndDelimiter(endDelimiter);
        }
        logger.info("QueryBuilderConfiguration:{}",builderConfig);
    }
}
