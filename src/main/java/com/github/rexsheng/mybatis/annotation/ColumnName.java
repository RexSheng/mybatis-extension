package com.github.rexsheng.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.rexsheng.mybatis.core.ColumnType;

/**
 * 表字段信息
 * @author RexSheng
 * 2020年8月29日 下午11:58:15
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnName {

	/**
	 * 列名
	 * @return 列名
	 */
	String value() default "";
	
	/**
	 * 字段备注
	 * @return 字段备注
	 * @since 1.2.0
	 */
	String desc() default "";
	
	/**
	 * 字段类型
	 * @return 字段类型
	 * @since 1.4.0
	 */
	ColumnType type() default ColumnType.NORMAL;
}
