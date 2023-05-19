package tech.yunyue.core.log.pointcut;

import cn.hutool.core.date.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import tech.yunyue.core.log.annotation.BizLog;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.condition.BizlogSupporterCondition;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.core.util.json.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private final ApplicationEventPublisher eventPublisher;

    @Pointcut("@annotation(tech.yunyue.core.log.annotation.BizLog)")
    private void bizLog() {
    }

    @Around("bizLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        BizLog bizlog = signature.getMethod().getAnnotation(BizLog.class);
        boolean isTransactional = Objects.nonNull(signature.getMethod().getAnnotation(Transactional.class)); //是否是事务方法

        //创建日志实体类
        Schema schema = joinPoint.getTarget().getClass().getAnnotation(Schema.class);
        var description = bizlog.description();
        if (Objects.nonNull(schema)) {
            description = new StringBuilder().append("操作模块：").append(schema.description())
                                            .append("。操作描述：").append(description).toString();
        }
        var className = joinPoint.getTarget().getClass().getName();
        var methodName = joinPoint.getSignature().getName();
        var args = JsonUtil.toJson(joinPoint.getArgs());
        BizLogEntity bizLogEntity = new BizLogEntity();
        bizLogEntity.setOperator(ThreadUserHelper.getUserName())
                    .setDescription(description)
                    .setOperateDatetime(DateUtil.now())
                    .setOperateType(bizlog.operateType().getCode())
                    .setClassName(className)
                    .setMethod(methodName)
                    .setParams(args);

        Object result;
        try {
            result = joinPoint.proceed();
            var response = JsonUtil.toJson(result);
            bizLogEntity.setResponse(response);
        } catch (Exception e){
            //无事务时操作失败不会走AFTER_ROLLBACK监听器 所以手动设置操作失败的记录
            if(!isTransactional){
                bizLogEntity.setDescription("【操作失败】" + bizLogEntity.getDescription());
            }
            throw e;
        } finally {
            //触发事件 使用事务监听器异步保存操作记录
            Map<String,Object> map = new HashMap<>(1);
            map.put(isTransactional ? LogConstant.TRANSACTIONAL_LOG : LogConstant.EVENT_LOG,bizLogEntity);
            BizLogEvent bizLogEvent = new BizLogEvent(map);
            eventPublisher.publishEvent(bizLogEvent);
        }
        return result;
    }
}

