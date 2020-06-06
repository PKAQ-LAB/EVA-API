package io.nerv.core.upload.condition

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * 文件上传启用条件， 是否启用go-fastdfs
 * @author: S.PKAQ
 */
class FastDfsCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val cacheType = context.environment.getProperty("eva.upload.type")
        return "dfs" == cacheType
    }
}