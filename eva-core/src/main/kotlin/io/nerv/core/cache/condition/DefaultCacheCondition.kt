package io.nerv.core.cache.condition

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * 缓存启用条件， 默认使用 caffeine 缓存
 * @author: S.PKAQ
 * @Datetime: 2018/9/28 12:38
 */
class DefaultCacheCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val cacheType = context.environment.getProperty("eva.cache.type")
        return null == cacheType || "" == cacheType || "caffeine" == cacheType
    }
}