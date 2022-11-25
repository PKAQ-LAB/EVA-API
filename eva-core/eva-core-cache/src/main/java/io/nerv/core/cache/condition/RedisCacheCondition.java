package io.nerv.core.cache.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 缓存启用条件， 是否启用redis
 *
 * @author: S.PKAQ
 */
public class RedisCacheCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String cacheType = context.getEnvironment().getProperty("eva.cache.type");
        return "redis".equals(cacheType);
    }
}
