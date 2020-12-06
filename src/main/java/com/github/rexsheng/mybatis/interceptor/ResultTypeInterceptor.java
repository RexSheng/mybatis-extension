package com.github.rexsheng.mybatis.interceptor;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.ArrayUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rexsheng.mybatis.config.BuilderConfiguration;
import com.github.rexsheng.mybatis.core.MappedStatementFactory;
import com.github.rexsheng.mybatis.mapper.DynamicMapper;

/**
 * @author RexSheng
 * 2020年8月28日 下午4:26:40
 */
@Intercepts({@Signature(
        type = Executor.class,
        method = "query",
        args = {
            MappedStatement.class, 
            Object.class, 
            RowBounds.class, 
            ResultHandler.class
        }
    ),
		@Signature(
		        type = Executor.class,
		        method = "update",
		        args = {
		            MappedStatement.class, 
		            Object.class
		        }
		    )
}
)
public class ResultTypeInterceptor implements Interceptor{

	private final static Logger logger=LoggerFactory.getLogger(ResultTypeInterceptor.class);
	
	private final static Logger mapperLogger=LoggerFactory.getLogger(DynamicMapper.class); 
	
	private BuilderConfiguration builderConfig=new BuilderConfiguration();
	
	@SuppressWarnings("unchecked")
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		final Object[] args = invocation.getArgs();
	    MappedStatement ms = (MappedStatement) args[0];
	    String methodName=ms.getId();
	    if(methodName!=null) {
	    	if(methodName.contains("com.github.rexsheng.mybatis.mapper.DynamicMapper.selectByBuilder")) {//$NON-NLS-1$
	    		Object parameterObject = args[1];
	    		
	    		com.github.rexsheng.mybatis.extension.QueryBuilder<?> queryBuilder=(com.github.rexsheng.mybatis.extension.QueryBuilder<?>)parameterObject;
            	queryBuilder.setBuiderConfig(builderConfig);
            	Boolean ifCalculateTotal=queryBuilder.getTable().getTotalCountEnabled();
            	BoundSql boundSql = ms.getSqlSource().getBoundSql(parameterObject);
            	if(ifCalculateTotal) {
            		Object params = boundSql.getParameterObject();
            		String countSql=builderConfig.getDatabaseDialect().generateCountSql(boundSql.getSql(), params, boundSql, ms, queryBuilder);
            		
                	Connection conn =ms.getConfiguration().getEnvironment().getDataSource().getConnection();
                	PreparedStatement countStatement = null;
                	ResultSet rs=null;
                    long totalItemCount = 0;
                    try {
                    	mapperLogger.debug("==> TotalCount SQL: {}",countSql);
                    	
                    	//预编译统计总记录数的sql
                    	countStatement = conn.prepareStatement(countSql);
                    	                        
                        MetaObject metaObject = ms.getConfiguration()
        						.newMetaObject(parameterObject);
                    	List<Object> typeList = new ArrayList<>();
        				for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
        					String propertyName = parameterMapping.getProperty();
        					Object obj = null;
        					if (metaObject.hasGetter(propertyName)) {
        						obj = metaObject.getValue(propertyName);
        					} else if (boundSql.hasAdditionalParameter(propertyName)) {
        						obj = boundSql.getAdditionalParameter(propertyName);
        					}
        					if (obj == null) {
	        		            typeList.add("null");
	        		          } else {
	        		            typeList.add(objectValueToString(obj));
	        		          }
        				}
        		        final String parameters = typeList.toString();        		        
        				mapperLogger.debug("==> TotalCount Parameters: {}",parameters.substring(1, parameters.length() - 1));

                        BoundSql countBs=copyAndNewBS(ms,boundSql,countSql);
        				//当sql带有参数时，下面的这句话就是获取查询条件的参数 
        				DefaultParameterHandler parameterHandler = new DefaultParameterHandler(ms,params,countBs);
        				//经过set方法，就可以正确的执行sql语句  
        				parameterHandler.setParameters(countStatement);
                        //执行查询语句
                        rs = countStatement.executeQuery();
                        while (rs.next()) {	                    	
                        	totalItemCount = rs.getInt(1);
                        	mapperLogger.debug("<== TotalCount Result: {}",totalItemCount);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                    	try {
                			if(rs!=null) {
                				rs.close();
                			}
                			if(countStatement!=null) {
                				countStatement.close();
                			}
                			if(conn!=null) {
                				conn.close();
                			}
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    queryBuilder.getTable().setTotalItemCount(totalItemCount);
                    if(queryBuilder.getTable().getTemporarySkipSelectIfCountZero()!=null) {
                    	if(Boolean.TRUE.equals(queryBuilder.getTable().getTemporarySkipSelectIfCountZero())) {
                        	return new ArrayList<>();
                        }
                    }
                    else {
                    	if(totalItemCount==0 && builderConfig.getDatabaseDialect().skipSelectIfCountZero()) {
                        	return new ArrayList<>();
                        }
                    }
            	}
            	
				List<ParameterMapping> newParamterMappings=boundSql.getParameterMappings()==null?new ArrayList<>():new ArrayList<>(boundSql.getParameterMappings());
				String pageSql=builderConfig.getDatabaseDialect().generatePaginationSql(boundSql.getSql(), newParamterMappings, boundSql, ms, queryBuilder);
				BoundSql newBoundSql=new BoundSql(ms.getConfiguration(),pageSql,newParamterMappings,boundSql.getParameterObject());
				//复制ms，重设类型
			    args[0] = MappedStatementFactory.changeMappedStatementResultType(ms,new SonOfSqlSource(newBoundSql), queryBuilder.getOutputClazz());
	    	}
	    	else if(methodName.contains("com.github.rexsheng.mybatis.mapper.DynamicMapper.updateByBuilder")) {//$NON-NLS-1$
	    		Object parameterObject = args[1];
	    		com.github.rexsheng.mybatis.extension.QueryBuilder<?> queryBuilder=(com.github.rexsheng.mybatis.extension.QueryBuilder<?>)parameterObject;
            	queryBuilder.setBuiderConfig(builderConfig);
	    	}
	    	else if(methodName.contains("com.github.rexsheng.mybatis.mapper.DynamicMapper.deleteByBuilder")) {//$NON-NLS-1$
	    		Object parameterObject = args[1];
	    		com.github.rexsheng.mybatis.extension.QueryBuilder<?> queryBuilder=(com.github.rexsheng.mybatis.extension.QueryBuilder<?>)parameterObject;
            	queryBuilder.setBuiderConfig(builderConfig);
	    	}
	    	else if(methodName.equalsIgnoreCase("com.github.rexsheng.mybatis.mapper.DynamicMapper.selectBySql")) {//$NON-NLS-1$
	    		Object parameterObject = args[1];
			    //获取参数中设置的返回值类型
	    		Class<?> resultType = getResultType(parameterObject,"arg1");
			    if(resultType == null){
			        return invocation.proceed();
			    }
			    //复制ms，重设类型
			    args[0] = MappedStatementFactory.changeMappedStatementResultType(ms, resultType);
	    	}
	    	else if(methodName.equalsIgnoreCase("com.github.rexsheng.mybatis.mapper.DynamicMapper.selectBySqlWithParams") ||//$NON-NLS-1$
	    			methodName.equalsIgnoreCase("com.github.rexsheng.mybatis.mapper.DynamicMapper.selectByMapWithParams") ||//$NON-NLS-1$
	    			methodName.equalsIgnoreCase("com.github.rexsheng.mybatis.mapper.DynamicMapper.countBySqlWithParams")) {//$NON-NLS-1$
	    		Object parameterObject = args[1];
			    //获取参数中设置的返回值类型
	    		Class<?> resultType = null;
	    		if(methodName.equalsIgnoreCase("com.github.rexsheng.mybatis.mapper.DynamicMapper.selectBySqlWithParams")) {
	    			resultType=getResultType(parameterObject,"clazz");
	    		}
	    		else if(methodName.equalsIgnoreCase("com.github.rexsheng.mybatis.mapper.DynamicMapper.selectByMapWithParams")) {
	    			resultType=Map.class;
	    		}
	    		else if(methodName.equalsIgnoreCase("com.github.rexsheng.mybatis.mapper.DynamicMapper.countBySqlWithParams")) {
	    			resultType=long.class;
	    		}
			    if(resultType == null){
			        return invocation.proceed();
			    }
			    //获取参数中设置的返回值类型
	    		String sql=(String)getResultValue(parameterObject,"sql");
	    		Map<String,Object> paramMap=(Map<String,Object>)getResultValue(parameterObject,"params");//$NON-NLS-1$
	    		logger.debug("interceptor sql:{}",sql);
	    		BoundSql boundSql=ms.getBoundSql(parameterObject);
	    		
	    		BoundSql newBoundSql=new BoundSql(ms.getConfiguration(),boundSql.getSql(),boundSql.getParameterMappings()==null?new ArrayList<>():new ArrayList<>(boundSql.getParameterMappings()),boundSql.getParameterObject());
	    		int i=1;
	    		Pattern pattern=Pattern.compile("#\\{(\\s)*\\w+(\\s)*(,(\\s)*jdbcType(\\s)*=(\\s)*\\w+(\\s)*)?}");//$NON-NLS-1$
				Matcher matcher=pattern.matcher(sql);
				while(matcher.find()) {
					String variable=matcher.group().substring(2, matcher.group().length()-1);
					if(variable.indexOf(",")>-1) {
						variable=variable.substring(0, variable.indexOf(",")).trim();
					}
					Object value=paramMap.get(variable);
					if(value==null) {
						throw new NullPointerException("参数值"+variable+"不能为空");//$NON-NLS-1$
					}
					if(value instanceof Iterable<?>) {
						Iterable<?> iter=(Iterable<?>)value;
						Iterator<?> iterator=iter.iterator();
						while(iterator.hasNext()) {
							Object currentValue=iterator.next();
							newBoundSql.getParameterMappings().add(createNewParameterMapping(ms,String.valueOf(i),currentValue.getClass()));
							newBoundSql.setAdditionalParameter(String.valueOf(i),currentValue);
							i++;
						}
					}
					else {
						newBoundSql.getParameterMappings().add(createNewParameterMapping(ms,String.valueOf(i),value.getClass()));
						newBoundSql.setAdditionalParameter(String.valueOf(i),value);
						i++;
					}
				}
	    		MappedStatement ms2=MappedStatementFactory.changeMappedStatementResultType(ms,new SonOfSqlSource(newBoundSql), resultType);
				args[0] = ms2;
	    	}
	    	else if(methodName.contains("com.github.rexsheng.mybatis.mapper.DynamicMapper.insertBatch")) {//$NON-NLS-1$
	    		Object parameterObject = args[1];
	    		if (parameterObject instanceof Map) {
	    			((Map<String, Object>)parameterObject).put("config", builderConfig);//$NON-NLS-1$
	    		}
	    	}
	    	
	    }	    
	    return invocation.proceed();
	}

	/**
	 * example: BuilderConfigurationFactory.builder().dialect(new MySqlDialect()).build()
	 * @param builderConfig BuilderConfiguration
	 */
	public void setConfig(BuilderConfiguration builderConfig) {
		this.builderConfig=builderConfig;
		logger.debug("QueryBuilderConfiguration:{}",builderConfig);
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
    
    private Object getResultValue(Object parameterObject,String resultKey){
        if (parameterObject == null) {
            return null;
        } 
        else if (parameterObject instanceof Map) {
            //解决不可变Map的情况
            if(((Map<?,?>)(parameterObject)).containsKey(resultKey)){
                Object result = ((Map<?,?>)(parameterObject)).get(resultKey);
                return result;
            } else {
                return null;
            }
        } else {
            MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
            Object result = metaObject.getValue(resultKey);
            return result;
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
                throw new RuntimeException("非法的全限定类名字符串:" + object);//$NON-NLS-1$
            }
        } else {
        	logger.error("方法参数类型错误，" + resultTypeKey + " 对应的参数类型只能为 Class 类型或者为 类的全限定名称字符串");
            throw new RuntimeException("方法参数类型错误，" + resultTypeKey + " 对应的参数类型只能为 Class 类型或者为 类的全限定名称字符串");//$NON-NLS-1$
        }
    }
        
    /**
	 *构建一个新的BoundSql
	 */
	private BoundSql copyAndNewBS(MappedStatement mappedStatement, BoundSql boundSql, String countSql) {
		//根据新的sql构建一个全新的boundsql对象，并将原来的boundsql中的各属性复制过来
		BoundSql newBs=new BoundSql(mappedStatement.getConfiguration(),countSql
				,boundSql.getParameterMappings(),boundSql.getParameterObject());
		for(ParameterMapping mapping:boundSql.getParameterMappings()) {
			String prop=mapping.getProperty();
			if(boundSql.hasAdditionalParameter(prop)) {
				newBs.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
			}
		}
		return newBs;
	}
	
	private ParameterMapping createNewParameterMapping(MappedStatement mappedStatement,String name,Class<?> javaType) {
		ParameterMapping.Builder builder=new ParameterMapping.Builder(mappedStatement.getConfiguration(),name, javaType);
		return builder.build();
	}
	
//	@SuppressWarnings("unused")
//	private ParameterMapping createNewParameterMapping(MappedStatement mappedStatement,String name,JdbcType jdbcType) {
//		TypeHandler<?> typeHandler=mappedStatement.getConfiguration().getTypeHandlerRegistry().getTypeHandler(jdbcType);
//		ParameterMapping.Builder builder=new ParameterMapping.Builder(mappedStatement.getConfiguration(),name, typeHandler);
//		builder.jdbcType(jdbcType);
//		return builder.build();
//	}

	/**
	 *复制一个新的MappedStatement
	 */
    @SuppressWarnings("unused")
	private MappedStatement copyAndNewMS(MappedStatement ms,SqlSource ss) {
    	//通过builder对象重新构建一个MappedStatement对象
    	Builder builder =new Builder(ms.getConfiguration(),ms.getId(),ss,ms.getSqlCommandType());
    	builder.resource(ms.getResource());
    	builder.fetchSize(ms.getFetchSize());
    	builder.statementType(ms.getStatementType());
    	builder.keyGenerator(ms.getKeyGenerator());
    	builder.timeout(ms.getTimeout());
    	builder.parameterMap(ms.getParameterMap());
    	builder.resultMaps(ms.getResultMaps());
    	builder.resultSetType(ms.getResultSetType());
    	builder.cache(ms.getCache());
    	builder.flushCacheRequired(ms.isFlushCacheRequired());
    	builder.useCache(ms.isUseCache());
		return builder.build();
	}
        
    protected String removeBreakingWhitespace(String original) {
        StringTokenizer whitespaceStripper = new StringTokenizer(original);
        StringBuilder builder = new StringBuilder();
        while (whitespaceStripper.hasMoreTokens()) {
          builder.append(whitespaceStripper.nextToken());
          builder.append(" ");
        }
        return builder.toString();
      }
    
    protected String objectValueToString(Object value) {
        if (value instanceof Array) {
          try {
            return ArrayUtil.toString(((Array) value).getArray()) + "(" + value.getClass().getSimpleName() + ")";
          } catch (SQLException e) {
            return value.toString();
          }
        }
        else if (value instanceof Date) {
			value = new Timestamp(((Date)value).getTime());
		}
        
        return value.toString()+ "(" + value.getClass().getSimpleName() + ")";
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
    }
    
    /**
     * 自定义sql源
     * @author RexSheng
     * 2020年10月2日 上午12:13:50
     */
    class SonOfSqlSource implements SqlSource {
    	private BoundSql boundSql;
    	
    	public SonOfSqlSource(BoundSql boundSql) {
    		this.boundSql=boundSql;
    	}
		public BoundSql getBoundSql(Object arg0) {
			// TODO Auto-generated method stub
			return boundSql;
		}
    	
    }
}
