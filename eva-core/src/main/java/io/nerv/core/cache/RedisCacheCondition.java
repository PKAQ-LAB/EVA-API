package io.nerv.core.cache;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 是否启用redis字典缓存
 */
public class RedisCacheCondition implements Condition {

    private final static String cacheStr = "redis";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String impl = context.getEnvironment().getProperty("eva.cache.type");

        return cacheStr.equalsIgnoreCase(impl);
    }
}