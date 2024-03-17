package com.zurich.annotation;

import java.lang.annotation.*;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/21 18:47
 * @description
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
    /**
     * Service version, default value is empty string
     */
    String version() default "";
    /**
     * Service group, default value is empty string
     */
    String group() default "";
}
