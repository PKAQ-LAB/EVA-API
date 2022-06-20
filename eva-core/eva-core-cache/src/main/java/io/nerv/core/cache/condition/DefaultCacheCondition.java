package io.nerv.core.cache.condition;

import io.nerv.properties.Cache;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 缓存启用条件， 默认使用 caffeine 缓存
 * @author: S.PKAQ
 */
@Component
public class DefaultCacheCondition implements Condition {

    @Autowired
    private EvaConfig evaconfig;
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        System.out.println(evaconfig.getCache().getType()+"------------------------->");
        String cacheType =  Optional.ofNullable(evaconfig.getCache()).map(Cache::getType).orElse("redis");
        return null == cacheType || "".equals(cacheType) || "caffeine".equals(cacheType);
    }
}
