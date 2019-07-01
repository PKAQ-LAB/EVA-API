package io.nerv.core.bizlog.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/9/28 12:38
 */
public class MybatisSupporterCondition implements Condition {
    private final static String supporterStr = "MybatisSupporter";
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String impl = context.getEnvironment().getProperty("bizlog.impl");
        String enabled = context.getEnvironment().getProperty("bizlog.enabled");
        
        return "true".equalsIgnoreCase(enabled)
               && supporterStr.equals(impl);
    }
}
