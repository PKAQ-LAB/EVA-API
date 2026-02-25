package org.pkaq.core.upload.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 文件上传启用条件， 是否启用go-fastdfs
 *
 * @author PKAQ
 */
public class FastDfsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String fileType = context.getEnvironment().getProperty("eva.file.type");
        return "dfs".equalsIgnoreCase(fileType);
    }
}
