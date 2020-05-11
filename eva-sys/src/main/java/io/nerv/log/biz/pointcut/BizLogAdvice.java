package io.nerv.log.biz.pointcut;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nerv.core.bizlog.annotation.BizLog;
import io.nerv.core.bizlog.base.BizLogEntity;
import io.nerv.core.bizlog.base.BizLogSupporter;
import io.nerv.core.bizlog.condition.BizlogSupporterCondition;
import io.nerv.core.util.JsonUtil;
import io.nerv.core.util.SecurityHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
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
    private JsonUtil jsonUtil;

    @Autowired
    private BizLogSupporter bizLogSupporter;

    @Autowired
    private SecurityHelper securityHelper;

    @Pointcut("@annotation(io.nerv.core.bizlog.annotation.BizLog)")
    private void bizLog(){}

    @Before("bizLog()")
    public void doBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        BizLog  bizlog = signature.getMethod().getAnnotation(BizLog.class);

//        Object result = joinPoint.proceed(joinPoint.getArgs());

        var className = joinPoint.getTarget().getClass().getName();
        var methodName = joinPoint.getSignature().getName();
        var args = jsonUtil.toJSONString(joinPoint.getArgs());
//        var response = JSON.toJSONString(result);

        if (null != bizlog){
            BizLogEntity bizLogEntity = new BizLogEntity();

            if( !"anonymousUser".equals(securityHelper.getAuthentication().getPrincipal())){
                bizLogEntity.setOperator(securityHelper.getJwtUserName());
            }

            bizLogEntity.setDescription(bizlog.description())
                        .setOperateDatetime(DateUtil.now())
                        .setOperateType(bizlog.operateType().getIndex());

            bizLogEntity.setClassName(className)
                        .setMethod(methodName)
                        .setParams(args);
//                        .setResponse(response);

            this.bizLogSupporter.save(bizLogEntity);
        }
    }
}

 