package org.pkaq.core.log.condition;

import org.pkaq.core.util.StrUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 默认错误日志处理器启用条件
 *
 * @author PKAQ
 */
public class DefaultErrorlogSupporterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment().getProperty("eva.error-log.enabled");
        return StrUtils.isEmpty(enabled) || "false".equalsIgnoreCase(enabled);
    }
}
