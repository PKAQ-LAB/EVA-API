package io.nerv.common.bizlog.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author: S.PKAQ
 */
public class BizlogSupporterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment().getProperty("eva.bizlog.enabled");
        return "true".equalsIgnoreCase(enabled);
    }
}
