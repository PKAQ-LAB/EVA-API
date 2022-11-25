package io.nerv.core.upload.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 文件上传启用条件， 是否启用go-fastdfs
 *
 * @author: S.PKAQ
 */
public class FastDfsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String cacheType = context.getEnvironment().getProperty("eva.upload.type");
        return "dfs".equals(cacheType);
    }
}
