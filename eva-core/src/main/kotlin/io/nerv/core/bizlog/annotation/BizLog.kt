package io.nerv.core.bizlog.annotation

import io.nerv.core.bizlog.base.BizLogEnum
import java.lang.annotation.Documented
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * 业务日志注解
 * @author: S.PKAQ
 * @Datetime: 2018/4/12 11:53
 */
// 该注解能被声明在一个方法参数 或者 类的方法前
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Retention(RetentionPolicy.RUNTIME) // 该注解将被包含在javadoc中
@Documented
@Inherited
annotation class BizLog(
        /**
         * 日志描述
         */
        val description: String = "",
        /**
         * 操作表类型
         */
        val operateType: BizLogEnum = BizLogEnum.CREATE,
        /***
         * 操作人
         */
        val operator: String = "",
        /**
         * 操作时间
         * @return
         */
        val operateDateTime: String = "") 