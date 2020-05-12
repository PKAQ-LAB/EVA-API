package io.nerv.core.annotation

import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

// 作用到方法上
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER) // 运行时有效
@Retention(RetentionPolicy.RUNTIME)
@Inherited
annotation class NoRepeatSubmit 