package io.nerv.common.annotation;

import java.lang.annotation.*;

// 作用到方法上
@Target(ElementType.METHOD)
// 运行时有效
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface NoRepeatSubmit {

}
