package io.nerv.core.log;

import cn.hutool.core.exceptions.ExceptionUtil;
import io.nerv.core.util.IpUtil;
import io.nerv.properties.EvaConfig;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private EvaConfig evaConfig;
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
            log.info("Header: " + request.getHeaderNames());
            log.info("Device: " + request.getHeader("device"));
            log.info("Version: " + request.getHeader("version"));
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

        if (!evaConfig.getErrorLog().isEnabled()) return;

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if(null != attributes){
            HttpServletRequest request = attributes.getRequest();
            var ip = IpUtil.getIPAddress(request);
            var className = joinPoint.getTarget().getClass().getName();
            var methodName = joinPoint.getSignature().getName();

            Object[] args = joinPoint.getArgs();

            String argStr = "";
            for (Object arg : args) {
                if (arg instanceof MultipartFile) continue;
                argStr += arg;
            }

            log.error("---------------------------start---------------------------");
            log.error("URL : " + request.getRequestURL().toString());
            log.error("Header: " + request.getHeaderNames());
            log.error("Device: " + request.getHeader("device"));
            log.error("Version: " + request.getHeader("version"));
            log.error("HTTP_METHOD : " + request.getMethod());
            log.error("IP : " + request.getRemoteAddr());
            log.error("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
            log.error("ARGS : " + Arrays.toString(joinPoint.getArgs()));
            log.error("---------------------------end---------------------------");
        }
    }
}

 