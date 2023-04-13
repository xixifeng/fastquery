package org.fastquery.core;

import org.apache.commons.lang3.StringUtils;

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
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Query
{

    /**
     * sql语句
     *
     * @return String
     */
    String value() default StringUtils.EMPTY;

    /**
     * 求和语句
     *
     * @return String
     */
    String countQuery() default StringUtils.EMPTY;

    /**
     * 求和字段
     *
     * @return String
     */
    String countField() default "id";
}