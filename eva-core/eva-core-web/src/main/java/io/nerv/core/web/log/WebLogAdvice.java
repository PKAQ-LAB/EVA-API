package io.nerv.core.web.log;

import cn.hutool.core.exceptions.ExceptionUtil;
import io.nerv.core.web.util.IpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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
@RequiredArgsConstructor
public class WebLogAdvice {
    /**
     * 定义一个切入点.
     * ~ 第一个 * 代表任意修饰符及任意返回值.
     * ~ 第二个 * 任意包名
     * ~ 第三个 * 代表任意方法.
     * ~ 第四个 * 定义在web包或者子包
     * ~ 第五个 * 任意方法
     * ~ .. 匹配任意数量的参数.
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)" +
              "@within(org.springframework.stereotype.Controller)")
    public void webLog(){}

    private void print(JoinPoint joinPoint){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if(null != attributes){
            HttpServletRequest request = attributes.getRequest();
            var ip = IpUtil.getIPAddress(request);
            var className = joinPoint.getTarget().getClass().getName();
            var methodName = joinPoint.getSignature().getName();

            log.debug("---------------------------start---------------------------");
            log.debug("URL : " + request.getRequestURL().toString());
            log.debug("Header: " + request.getHeaderNames());
            log.debug("Device: " + request.getHeader("device"));
            log.debug("Version: " + request.getHeader("version"));
            log.debug("HTTP_METHOD : " + request.getMethod());
            log.debug("IP : " + ip);
            log.debug("CLASS_NAME : " + className);
            log.debug("CLASS_METHOD : " + methodName);
            log.debug("ARGS : " + Arrays.toString(joinPoint.getArgs()));
        }
    }
    @Around("webLog()")
    public Object  around(ProceedingJoinPoint joinPoint) throws Throwable {

        //记录开始时间
        long beginTime = System.currentTimeMillis();

        this.print(joinPoint);
        //执行方法
        Object result = joinPoint.proceed();
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;

        // 处理完请求，返回内容
        log.debug("RESPONSE : " + result);
        log.debug("SPEND TIME : " + time);
        log.debug("---------------------------start---------------------------");
        return result;
        //记录日志
    }
    /**
     * 记录异常日志
     * @param joinPoint
     * @param ex
     */
    @AfterThrowing(pointcut = "webLog()", throwing="ex")
    public void doWhenThrowing(JoinPoint joinPoint,Throwable ex){
        String jsontStack = ExceptionUtil.stacktraceToString(ex);
        log.error(jsontStack);
        this.print(joinPoint);
    }
}

