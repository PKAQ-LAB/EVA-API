package tech.yunyue.core.upload.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 是否启用缓存目录文件清理任务
 * 当文件上传类型为minio时，不需要启动。
 */
public class TempFileCleanTaskCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String type = context.getEnvironment().getProperty("eva.upload.type");
        return !"minio".equals(type);
    }
}
