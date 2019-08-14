package io.nerv.log.pointcut;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import io.nerv.core.bizlog.annotation.BizLog;
import io.nerv.core.bizlog.base.BizLogEntity;
import io.nerv.core.bizlog.base.BizLogSupporter;
import io.nerv.core.bizlog.condition.BizlogSupporterCondition;
import io.nerv.security.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * 记录业务日志
 * @author PKAQ
 */
@Slf4j
@Aspect
@Component
@Conditional(BizlogSupporterCondition.class)
public class BizLogAdvice {

    @Autowired
    private BizLogSupporter bizLogSupporter;

    @Autowired
    private SecurityUtil securityUtil;

    @Pointcut("@annotation(io.nerv.core.bizlog.annotation.BizLog)")
    private void bizLog(){}

    @Around("bizLog()")
    public void doBefore(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        BizLog  bizlog = signature.getMethod().getAnnotation(BizLog.class);

        Object result = joinPoint.proceed();

        if (null != bizlog){
            BizLogEntity bizLogEntity = new BizLogEntity();
            bizLogEntity.setOperator(securityUtil.getJwtUserName())
                        .setDescription(bizlog.description())
                        .setOperateDatetime(DateUtil.now())
                        .setOperateType(bizlog.operateType().getIndex());

            var className = joinPoint.getTarget().getClass().getName();
            var methodName = joinPoint.getSignature().getName();
            var args = JSON.toJSONString(joinPoint.getArgs());
            var response = JSON.toJSONString(result);

            bizLogEntity.setClassName(className)
                        .setMethod(methodName)
                        .setParams(args)
                        .setResponse(response);

            this.bizLogSupporter.save(bizLogEntity);
        }
    }
}

 