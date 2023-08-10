package tech.yunyue.core.annotation;

import java.lang.annotation.*;

/**
 * 指定多表查询时，数据权限的sql拼接的表名/表别名
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DatapermissionTable {
    String value() default "";
}
