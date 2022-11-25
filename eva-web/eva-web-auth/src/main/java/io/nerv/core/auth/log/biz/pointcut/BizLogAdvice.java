package io.nerv.core.auth.log.biz.pointcut;

import cn.hutool.core.date.DateUtil;
import io.nerv.core.log.annotation.BizLog;
import io.nerv.core.log.base.BizLogEntity;
import io.nerv.core.log.base.BizLogSupporter;
import io.nerv.core.log.condition.BizlogSupporterCondition;
import io.nerv.core.threaduser.ThreadUserHelper;
import io.nerv.core.util.json.JsonUtil;
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
    private final BizLogSupporter bizLogSupporter;

    @Pointcut("@annotation(io.nerv.core.log.annotation.BizLog)")
    private void bizLog() {
    }

    @Around("bizLog()")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        BizLog bizlog = signature.getMethod().getAnnotation(BizLog.class);

        Object result = joinPoint.proceed();

        var className = joinPoint.getTarget().getClass().getName();
        var methodName = joinPoint.getSignature().getName();
        var args = JsonUtil.toJson(joinPoint.getArgs());
        var response = JsonUtil.toJson(result);

        if (null != bizlog) {
            BizLogEntity bizLogEntity = new BizLogEntity();

            bizLogEntity.setOperator(ThreadUserHelper.getUserName());

            bizLogEntity.setDescription(bizlog.description())
                    .setOperateDatetime(DateUtil.now())
                    .setOperateType(bizlog.operateType().getCode());

            bizLogEntity.setClassName(className)
                    .setMethod(methodName)
                    .setParams(args)
                    .setResponse(response);

            this.bizLogSupporter.save(bizLogEntity);
        }
    }
}

 