package io.nerv.core.bizlog.condition

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * @author: S.PKAQ
 * @Datetime: 2018/9/28 12:38
 */
class MybatisSupporterCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val impl = context.environment.getProperty("eva.bizlog.impl")
        val enabled = context.environment.getProperty("eva.bizlog.enabled")
        return ("true".equals(enabled, ignoreCase = true)
                && supporterStr.equals(impl, ignoreCase = true))
    }

    companion object {
        private const val supporterStr = "MybatisSupporter"
    }
}