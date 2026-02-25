package org.pkaq.core.cache.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 缓存启用条件， 默认使用 caffeine 缓存
 *
 * @author PKAQ
 */
public class CaffeineCacheCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String cacheType = context.getEnvironment().getProperty("eva.cache.type");

        return "caffeine".equals(cacheType);
    }
}
