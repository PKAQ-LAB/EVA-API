package io.nerv.core.annotation;

import java.lang.annotation.*;

/**
 * @author PKAQ
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonFilter {
    /**
     * 该属性表示返回的响应对象中不需要序列化的属性名，多级用.的方式：集合和数组里的对象的某属性用 .属性名  map集合用key.属性名
     * 如 @JsonFilter(exclude = {"message","data.identityInfo.id","data.faceTemplates.records.cardNo"})
     *
     * @return
     */
    String[] exclude() default {};
}
