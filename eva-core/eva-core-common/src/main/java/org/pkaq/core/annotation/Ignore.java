package org.pkaq.core.annotation;

import java.lang.annotation.*;

/**
 * 忽略数据权限注解
 * @author PKAQ
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ignore {

}
