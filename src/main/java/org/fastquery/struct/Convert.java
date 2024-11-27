package org.fastquery.struct;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Convert {

    // converter
    Class<? extends AttributeConverter> value();
}
