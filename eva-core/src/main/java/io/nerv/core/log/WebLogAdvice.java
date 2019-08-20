package io.nerv.core.log;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import io.nerv.properties.EvaConfig;
import io.nerv.core.exception.entity.ErrorlogEntity;
import io.nerv.core.exception.mapper.ErrorlogMapper;
import io.nerv.core.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ErrorlogMapper errorlogMapper;

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

    @AfterThrowing(pointcut = "webLog()", throwing="ex")
    public void doWhenThrowing(JoinPoint joinPoint,Throwable ex){
        String jsontStack = JSON.toJSONString(ex.getStackTrace());
        log.error(jsontStack);

        if (!evaConfig.getErrorLog().isEnabled()) return;

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if(null != attributes){
            HttpServletRequest request = attributes.getRequest();
            var ip = IpUtil.getIPAddress(request);
            var className = joinPoint.getTarget().getClass().getName();
            var methodName = joinPoint.getSignature().getName();
            var args = JSON.toJSONString(joinPoint.getArgs());

            ErrorlogEntity errorlogEntity = new ErrorlogEntity();


            errorlogEntity.setRequestTime(DateUtil.now())
                          .setClassName(className)
                          .setMethod(methodName)
                          .setParams(JSON.toJSONString(args))
                          .setExDesc(jsontStack)
                          .setIp(ip)
                          .setSpendTime(String.valueOf(System.currentTimeMillis() - startTime.get()));
            this.errorlogMapper.insert(errorlogEntity);
        }
    }
}

 