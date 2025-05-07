package org.pkaq.core.annotation;

import java.lang.annotation.*;

/**
 * @author PKAQ
 */ // 作用到方法上
@Target(ElementType.METHOD)
// 运行时有效
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface NoRepeatSubmit {

}
