package org.pkaq.core.upload.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 文件上传启用条件， 是否启用MinIO
 */
@Conditional(MinIOCondition.class)
public class MinIOCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String cacheType = context.getEnvironment().getProperty("eva.file.type");
        return "minio".equalsIgnoreCase(cacheType);
    }
}