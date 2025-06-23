package org.pkaq.core.upload.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author PKAQ
 */
public class DirFileCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return new DefaultNgCondition().matches(context, metadata) ||
               new FastDfsCondition().matches(context, metadata);
    }
}
