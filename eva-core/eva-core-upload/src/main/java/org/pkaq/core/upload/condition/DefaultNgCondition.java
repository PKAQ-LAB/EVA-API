package org.pkaq.core.upload.condition;

import cn.hutool.core.text.CharSequenceUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 文件上传启用条件， 默认使用 ng
 *
 * @author: S.PKAQ
 */
public class DefaultNgCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String cacheType = context.getEnvironment().getProperty("eva.file.type");
        return CharSequenceUtil.isBlank(cacheType) || "ng".equals(cacheType) || "nginx".equals(cacheType);
    }
}
