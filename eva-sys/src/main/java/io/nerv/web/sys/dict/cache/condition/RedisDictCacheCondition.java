package io.nerv.web.sys.dict.cache.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 是否启用redis字典缓存
 */
public class RedisDictCacheCondition implements Condition {

    private final static String cacheStr = "redis";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String impl = context.getEnvironment().getProperty("cache.impl");

        return cacheStr.equals(impl);
    }
}