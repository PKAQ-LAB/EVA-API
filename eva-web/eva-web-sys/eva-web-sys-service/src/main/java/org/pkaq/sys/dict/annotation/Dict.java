package org.pkaq.sys.dict.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.pkaq.config.JacksonCodeSerializer;

import java.lang.annotation.*;

/**
 * 用以字典值转换的注解
 *
 * @author PKAQ
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = JacksonCodeSerializer.class)
public @interface Dict {
    /**
     * 该属性表示在字典中的code值，为空时则表示cede值与属性名一致。
     */
    String value() default "";
}

