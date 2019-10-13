package io.nerv.core.cache;

import cn.hutool.core.util.StrUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 是否启用默认hashmap缓存
 */
public class DefaultCacheCondition implements Condition {

    private final static String cacheStr = "map,default";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String impl = context.getEnvironment().getProperty("eva.cache.type");

        return StrUtil.isBlank(impl) || cacheStr.contains(impl);
    }
}