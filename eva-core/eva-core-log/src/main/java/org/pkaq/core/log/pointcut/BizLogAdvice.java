package org.pkaq.core.log.pointcut;

import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.condition.BizlogSupporterCondition;
import org.pkaq.core.util.json.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * 记录业务日志
 *
 * @author PKAQ
 */
@Slf4j
@Aspect
@Component
@Conditional(BizlogSupporterCondition.class)
@RequiredArgsConstructor
public class BizLogAdvice {
    private final BizLogger bizLogger;

    @Pointcut("@annotation(org.pkaq.core.log.annotation.BizLog)")
    private void bizLog() {
    }

    @Around("bizLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        BizLog bizlog = signature.getMethod().getAnnotation(BizLog.class);
        Object result = joinPoint.proceed();

        if (null != bizlog) {


            var className = joinPoint.getTarget().getClass().getName();
            var methodName = joinPoint.getSignature().getName();
            var args = JsonUtil.toJson(joinPoint.getArgs());
            var response = JsonUtil.toJson(result);

            BizLogEntity bizLogEntity = new BizLogEntity();
            bizLogEntity.setClassName(className)
                    .setMethod(methodName)
                    .setParams(args)
                    .setResponse(response);

            bizLogger.write(bizLogEntity, bizlog);
        }

        return result;
    }
}

 