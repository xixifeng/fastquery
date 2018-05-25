package org.fastquery.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询语句
 * 
 * @author xixifeng (fastquery@126.com)
 */
@Repeatable(Queries.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface Query {

	/**
	 * sql语句
	 * 
	 * @return String
	 */
	String value() default "";

	/**
	 * 求和语句
	 * 
	 * @return String
	 */
	String countQuery() default "";

	/**
	 * 求和字段
	 * 
	 * @return String
	 */
	String countField() default "id";

	/**
	 * 是否采用本地查询
	 * 
	 * @return 布尔 boolean
	 */
	boolean nativeQuery() default true; // NO_UCD (unused code)
}