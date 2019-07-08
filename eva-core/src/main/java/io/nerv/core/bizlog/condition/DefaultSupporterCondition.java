package io.nerv.core.bizlog.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/9/28 12:38
 */
public class DefaultSupporterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String impl = context.getEnvironment().getProperty("bizlog.impl");
        String enabled = context.getEnvironment().getProperty("bizlog.enabled");
        return "true".equalsIgnoreCase(enabled)
                &&
                (null == impl || impl.trim().length()<1 || "console".equalsIgnoreCase(impl));
    }
}
