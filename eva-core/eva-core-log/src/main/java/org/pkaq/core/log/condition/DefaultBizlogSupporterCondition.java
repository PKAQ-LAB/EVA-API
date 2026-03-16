package org.pkaq.core.log.condition;

import org.pkaq.core.util.StrUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author PKAQ
 */
public class DefaultBizlogSupporterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String impl = context.getEnvironment().getProperty("eva.bizlog.impl");
        String enabled = context.getEnvironment().getProperty("eva.bizlog.enabled");
        return ("true".equalsIgnoreCase(enabled) || StrUtils.isEmpty(enabled))
                &&
                (null == impl || impl.trim().isEmpty() || "console".equalsIgnoreCase(impl));
    }
}
