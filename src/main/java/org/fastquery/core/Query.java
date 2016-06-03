package org.fastquery.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询语句
 * @author xixifeng (fastquery@126.com)
 */
@Repeatable(Querys.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface Query {
	
	/**
	 * sql语句
	 */
	String value() default "";

	/**
	 * 求和语句
	 */
	String countQuery() default "";
	
	/**
	 * 求和字段
	 * @return
	 */
	String countField() default "id";
	
	/**
	 * 是否采用本地查询
	 */
	boolean nativeQuery() default true;
}