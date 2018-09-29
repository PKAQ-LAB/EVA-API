package org.pkaq.pay.alipay.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * 是否启用了支付宝支付
 * @author: S.PKAQ
 * @Datetime: 2018/9/29 8:39
 */
public class AlipayEnabled implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String isEnabled = context.getEnvironment().getProperty("pay.alipay.enabled");
        //  是否启用并且配置了 相关的key
        if ("true".equalsIgnoreCase(isEnabled)){

            String appId = context.getEnvironment().getProperty("pay.alipay.app_id");
            String appPrivateKey = context.getEnvironment().getProperty("pay.alipay.app_private_key");
            String alipayPublicKey = context.getEnvironment().getProperty("pay.alipay.alipay_public_key");
            String signType = context.getEnvironment().getProperty("pay.alipay.sign_type");

            return !StringUtils.isEmpty(appId) &&  !StringUtils.isEmpty(appPrivateKey) &&
                   !StringUtils.isEmpty(alipayPublicKey) &&  !StringUtils.isEmpty(signType);
        }
        return false;
    }
}
