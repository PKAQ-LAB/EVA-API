package io.nerv.core.cache.condition

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * 缓存启用条件， 是否启用redis
 * @author: S.PKAQ
 * @Datetime: 2018/9/28 12:38
 */
class RedisCacheCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val cacheType = context.environment.getProperty("eva.cache.type")
        return "redis" == cacheType
    }
}