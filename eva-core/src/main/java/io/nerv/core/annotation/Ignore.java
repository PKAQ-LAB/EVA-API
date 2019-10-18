package io.nerv.core.annotation;

import java.lang.annotation.*;

/**
 * 忽略数据权限注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ignore {
}
