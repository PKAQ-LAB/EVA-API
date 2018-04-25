package org.pkaq.core.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.pkaq.core.annotation.BizLog;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * AOP记录web日志
 * @author PKAQ
 */
@Aspect
@Component
@Slf4j
public class WebLogAdvice {
    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    /**

     * 定义一个切入点.
     * ~ 第一个 * 代表任意修饰符及任意返回值.
     * ~ 第二个 * 任意包名
     * ~ 第三个 * 代表任意方法.
     * ~ 第四个 * 定义在web包或者子包
     * ~ 第五个 * 任意方法
     * ~ .. 匹配任意数量的参数.
     */
    @Pointcut("execution(public * org.pkaq.web..*.*(..))")
    public void webLog(){}

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {

        //记录开始时间
        startTime.set(System.currentTimeMillis());

        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(null != attributes){
            HttpServletRequest request = attributes.getRequest();

            // 记录下请求内容
            log.info("---------------------------start---------------------------");
            log.info("URL : " + request.getRequestURL().toString());
            log.info("HTTP_METHOD : " + request.getMethod());
            log.info("IP : " + request.getRemoteAddr());
            log.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
            log.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));
            log.info("---------------------------end---------------------------");
        }
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(JoinPoint joinPoint, Object ret) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 处理完请求，返回内容
        log.info("RESPONSE : " + ret);
        log.info("SPEND TIME : " + (System.currentTimeMillis() - startTime.get()));

        //TODO 记录业务日志 这里需要存数据库
        BizLog  bizlog = signature.getMethod().getAnnotation(BizLog.class);

        if (null != bizlog){
            String bizDes = bizlog.description();
            System.out.println(" this is biz log : " + bizDes);
        }

    }

}

 