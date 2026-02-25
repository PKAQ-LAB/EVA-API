package org.pkaq.core.log.annotation;

import org.pkaq.core.log.base.BizLogCodes;

import java.lang.annotation.*;

/**
 * 业务日志注解
 *
 * @author PKAQ
 */
// 该注解能被声明在一个方法参数 或者 类的方法前
@Target({ElementType.PARAMETER, ElementType.METHOD})
// 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Retention(RetentionPolicy.RUNTIME)
// 该注解将被包含在javadoc中
@Documented
@Inherited
public @interface BizLog {
    /**
     * 日志描述
     */
    String description() default "";

    /**
     * 操作表类型
     */
    BizLogCodes operateType() default BizLogCodes.CREATE;

    /**
     * 区分新增或修改的参数
     * 数字表示第几个入参 默认通过第一个参数的id属性来区分操作类型是新增还是修改 id有值为修改 无则新增
     */
    String distinguishParam() default "0.id";

    /***
     * 操作人
     */
    String operator() default "";

    /**
     * 操作时间
     *
     * @return
     */
    String operateDateTime() default "";

    /**
     * 格式化日志描述的参数名
     *
     * @BizLog(args = {"name","code"})  取方法返回对象的name、code属性
     * @BizLog(args = {"this"})  取方法返回值[当返回对象是基本数据类型或者string类型时]
     * @BizLog(args = {"name","param:0","param:1.code"})  取方法返回对象的name属性以及第一个入参和第二个入参code属性的值
     */
    String[] args() default {};

    /**
     * 业务id的参数名
     *
     * @BizLog(bizId = "id")  取方法返回对象的id属性值
     * @BizLog(bizId = "this")  取方法返回值[当返回对象是基本数据类型或者string类型时]
     * @BizLog(bizId = "param:0")  取方法第一个入参的属性值
     * @BizLog(bizId = "param:1.id")  取方法第一个入参的id属性值
     */
    String bizId() default "";
}
