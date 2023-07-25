package tech.yunyue.core.log.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface HistoryLog {
    /**
     * 表名
     */
    String value() default "";

    /**
     * 唯一标识字段，java类属性名
     */
    String mark() default "id";
}
