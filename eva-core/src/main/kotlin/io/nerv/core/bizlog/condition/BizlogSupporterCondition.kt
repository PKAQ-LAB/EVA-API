package io.nerv.core.bizlog.condition

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * @author: S.PKAQ
 * @Datetime: 2018/10/12 16:11
 */
class BizlogSupporterCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val enabled = context.environment.getProperty("eva.bizlog.enabled")
        return "true".equals(enabled, ignoreCase = true)
    }
}