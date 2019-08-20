package io.nerv.cache.condition;

import cn.hutool.core.util.StrUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 缓存启用条件， 默认使用map缓存
 * @author: S.PKAQ
 * @Datetime: 2018/9/28 12:38
 */
public class DefaultCacheCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String cacheType = context.getEnvironment().getProperty("eva.cache.type");
        return StrUtil.isBlank(cacheType) || "map".equals(cacheType);
    }
}