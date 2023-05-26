package tech.yunyue.core.log.annotation;

import tech.yunyue.core.log.base.BizLogEnum;

import java.lang.annotation.*;

/**
 * 业务日志注解
 *
 * @author: S.PKAQ
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
    BizLogEnum operateType() default BizLogEnum.CREATE;

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
     * @BizLog(args = {"name","code"})  取方法返回对象的name、code属性
     * @BizLog(args = {"this"})  取方法返回值[当返回对象是基本数据类型或者string类型时]
     * @BizLog(args = {"name","param:0","param:1.code"})  取方法返回对象的name属性以及第一个入参和第二个入参code属性的值
     */
    String[] args() default {};
}
