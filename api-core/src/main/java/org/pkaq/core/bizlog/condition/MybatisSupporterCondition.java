package org.pkaq.core.bizlog.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/9/28 12:38
 */
public class MybatisSupporterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String supporterStr = "org.pkaq.core.bizlog.supporter.mybatis.MybatisSupporter";
        String impl = context.getEnvironment().getProperty("bizlog.impl");

        return null != impl
                && impl.length() > 0
                && supporterStr.equals(impl);
    }
}
