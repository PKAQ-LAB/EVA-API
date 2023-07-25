package tech.yunyue.core.log.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DefaultErrorLogSupporterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String impl = context.getEnvironment().getProperty("eva.errorlog.impl");
        String enabled = context.getEnvironment().getProperty("eva.errorlog.enabled");
        return "true".equalsIgnoreCase(enabled)
                &&
                (null == impl || impl.trim().length() < 1 || "console".equalsIgnoreCase(impl));
    }
}
