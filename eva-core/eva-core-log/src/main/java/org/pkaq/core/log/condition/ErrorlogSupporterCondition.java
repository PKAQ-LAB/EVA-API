package org.pkaq.core.log.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 错误日志是否启用的条件判断
 *
 * @author PKAQ
 */
public class ErrorlogSupporterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment().getProperty("eva.errorlog.enabled");
        if (enabled == null) {
            enabled = context.getEnvironment().getProperty("eva.error-log.enabled");
        }
        return "true".equalsIgnoreCase(enabled);
    }
}
