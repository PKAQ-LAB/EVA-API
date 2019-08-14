package io.nerv.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Code {
    /**
     * 该属性表示在字典中的code值，为空时则表示cede值与属性名一致。
     * @return
     */
    String value() default "";
}
