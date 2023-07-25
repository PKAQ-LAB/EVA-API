package tech.yunyue.core.web.log;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import org.springframework.context.ApplicationEventPublisher;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.core.util.json.JsonUtil;
import tech.yunyue.core.web.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * AOP记录web日志
 *
 * @author PKAQ
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class WebLogAdvice {
    private final ApplicationEventPublisher eventPublisher;
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
    public void webLog() {
    }

    private ErrorlogEntity print(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        ErrorlogEntity errorlogEntity = new ErrorlogEntity();
        if (null != attributes) {
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
            errorlogEntity.setIp(ip)
                    .setClassName(className)
                    .setMethod(methodName)
                    .setParams(JsonUtil.toJson(joinPoint.getArgs()));
        }
        return errorlogEntity;
    }

    @Around("webLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //记录开始时间
        long beginTime = System.currentTimeMillis();

        ErrorlogEntity logEntity = this.print(joinPoint);
        //执行方法
        Object result = null;
        try {
            result = joinPoint.proceed();
        }catch (Throwable ex) {
            // 记录异常日志
            logEntity.setRequestTime(DateUtil.now())
                    .setLoginUser(ThreadUserHelper.getUserName())
                    .setCreateId(ThreadUserHelper.getUserId())
                    .setPostId(ThreadUserHelper.getPostId())
                    .setOrgId(ThreadUserHelper.getOrgId())
                    .setTenantId(ThreadUserHelper.getTenantId())
                    .setExDesc(ExceptionUtil.stacktraceToString(ex))
                    .setSpendTime(String.valueOf(System.currentTimeMillis() - beginTime));
            eventPublisher.publishEvent(new ErrorLogEvent(logEntity));
            throw ex;
        }
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;

        // 处理完请求，返回内容
        log.debug("RESPONSE : " + result);
        log.debug("SPEND TIME : " + time);
        log.debug("---------------------------start---------------------------");
        return result;
    }
}

