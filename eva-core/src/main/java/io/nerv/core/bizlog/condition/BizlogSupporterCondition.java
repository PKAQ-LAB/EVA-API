package io.nerv.core.bizlog.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/10/12 16:11
 */
public class BizlogSupporterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment().getProperty("bizlog.enabled");
        return "true".equalsIgnoreCase(enabled);
    }
}
