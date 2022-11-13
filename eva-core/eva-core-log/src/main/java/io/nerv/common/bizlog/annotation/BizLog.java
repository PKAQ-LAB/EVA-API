package io.nerv.common.bizlog.annotation;

import io.nerv.common.bizlog.base.BizLogEnum;

import java.lang.annotation.*;

/**
 * 业务日志注解
 * @author: S.PKAQ
 */
// 该注解能被声明在一个方法参数 或者 类的方法前
@Target({ElementType.PARAMETER, ElementType.METHOD})
// 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Retention(RetentionPolicy.RUNTIME)
// 该注解将被包含在javadoc中
@Documented
@Inherited
public @interface BizLog {
    /**
     * 日志描述
     */
    String description()  default "";

    /**
     * 操作表类型
     */
    BizLogEnum operateType() default BizLogEnum.CREATE;

    /***
     * 操作人
     */
    String operator() default "";

    /**
     * 操作时间
     * @return
     */
    String operateDateTime() default "";
}
