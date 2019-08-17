package io.nerv.cache.condition;

import cn.hutool.core.util.StrUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 缓存启用条件， 是否启用redis
 * @author: S.PKAQ
 * @Datetime: 2018/9/28 12:38
 */
public class RedisCacheCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String cacheType = context.getEnvironment().getProperty("eva.cache.type");
        return StrUtil.isBlank(cacheType) || "redis".equals(cacheType);
    }
}
