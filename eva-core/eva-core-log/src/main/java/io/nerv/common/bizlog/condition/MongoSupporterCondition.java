package io.nerv.common.bizlog.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author: S.PKAQ
 */
public class MongoSupporterCondition implements Condition {
    private final static String supporterStr = "mongo";
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String impl = context.getEnvironment().getProperty("eva.bizlog.impl");
        String enabled = context.getEnvironment().getProperty("eva.bizlog.enabled");
        
        return "true".equalsIgnoreCase(enabled)
               && supporterStr.equalsIgnoreCase(impl);
    }
}
