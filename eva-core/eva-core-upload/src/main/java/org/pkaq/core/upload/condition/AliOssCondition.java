package org.pkaq.core.upload.condition;

import cn.hutool.core.text.CharSequenceUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 文件上传启用条件，阿里云对象存储
 *
 * @author: S.PKAQ
 */
public class AliOssCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String cacheType = context.getEnvironment().getProperty("eva.file.type");
        return CharSequenceUtil.isBlank(cacheType) || "ali".equalsIgnoreCase(cacheType) || "aliyun".equalsIgnoreCase(cacheType);
    }
}
