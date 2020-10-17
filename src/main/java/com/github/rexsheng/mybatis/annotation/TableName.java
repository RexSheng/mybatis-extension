package com.github.rexsheng.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库表信息
 * @author RexSheng
 * 2020年8月29日 下午11:58:15
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {

	/**
	 * 表名
	 * Alias for the {@link #table()} Attribute.
	 * @return 表名
	 */
	String value() default "";
	
	/**
	 * 表名
	 * @return 表名
	 * @since 1.2.0
	 */
	String table() default "";
	
	/**
	 * schema
	 * @return schema
	 * @since 1.2.0
	 */
	String schema() default "";
	
	/**
	 * catalog
	 * @return catalog
	 * @since 1.2.0
	 */
	String catalog() default "";
	
	/**
	 * 表备注信息
	 * @return 表备注
	 * @since 1.2.0
	 */
	String desc() default "";
	
}
