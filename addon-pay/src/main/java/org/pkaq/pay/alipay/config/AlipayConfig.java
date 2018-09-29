package org.pkaq.pay.alipay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置
 * @author: S.PKAQ
 * @Datetime: 2018/9/28 22:59
 */
@Data
@Component
@ConfigurationProperties(prefix = "pay.alipay")
public class AlipayConfig {
    private boolean enabled;
    private String url;
    private String app_id;
    private String app_private_key;
    private final String format = "json";
    private String charset;
    private String alipay_public_key;
    private String sign_type;
}
