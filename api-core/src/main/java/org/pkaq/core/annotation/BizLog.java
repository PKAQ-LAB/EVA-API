package org.pkaq.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务日志注解
 * @author: S.PKAQ
 * @Datetime: 2018/4/12 11:53
 */
// 该注解能被声明在一个方法参数 或者 类的方法前
@Target({ElementType.PARAMETER, ElementType.METHOD})
// 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Retention(RetentionPolicy.RUNTIME)
// 该注解将被包含在javadoc中
@Documented
public @interface BizLog {
    /**
     * 日志描述
     */
    String description()  default "";

    /**
     * 操作表类型
     */
    int tableType() default 0;
}
