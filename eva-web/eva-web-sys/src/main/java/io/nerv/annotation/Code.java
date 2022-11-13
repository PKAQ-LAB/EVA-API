package io.nerv.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.nerv.config.JacksoCodeSerialize;

import java.lang.annotation.*;

/**
 * @author PKAQ
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = JacksoCodeSerialize.class)
public @interface Code {
    /**
     * 该属性表示在字典中的code值，为空时则表示cede值与属性名一致。
     * @return
     */
    String value() default "";
}
