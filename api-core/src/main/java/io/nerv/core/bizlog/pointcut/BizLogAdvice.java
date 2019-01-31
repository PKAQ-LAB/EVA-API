package io.nerv.core.bizlog.pointcut;

import cn.hutool.core.date.DateUtil;
import io.nerv.core.bizlog.condition.BizlogSupporterCondition;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import io.nerv.core.bizlog.annotation.BizLog;
import io.nerv.core.bizlog.base.BizLogEntity;
import io.nerv.core.bizlog.base.BizLogSupporter;
import io.nerv.core.bizlog.config.BizLogConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(BizLogConfig.class)
public class BizLogAdvice {

    @Autowired
    private BizLogSupporter bizLogSupporter;

    @Pointcut("@annotation(io.nerv.core.bizlog.annotation.BizLog)")
    private void bizLog(){}

    @Before("bizLog()")
    public void doBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        BizLog  bizlog = signature.getMethod().getAnnotation(BizLog.class);

        if (null != bizlog){
            BizLogEntity bizLogEntity = new BizLogEntity();
            bizLogEntity.setDescription(bizlog.description());
            bizLogEntity.setOperateDatetime(DateUtil.now());
            bizLogEntity.setOperateType(bizlog.operateType().getIndex());

            this.bizLogSupporter.save(bizLogEntity);
        }
    }
}

 