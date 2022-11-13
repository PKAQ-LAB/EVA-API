package io.nerv.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置
 * @author: S.PKAQ
 */
@Data
@Component
@ConfigurationProperties(prefix = "pay.alipay")
public class AlipayConfig {
    /** 是否啓用 **/
    private boolean enabled;
    /** 支付宝网关 **/
    private String URL;
    /** APPID即创建应用后生成 **/
    private String APP_ID;
    /** 开发者应用私钥，由开发者自己生成 **/
    private String APP_PRIVATE_KEY;
    /** 参数返回格式，只支持json **/
    private final String FORMAT = "json";
    /** 请求和签名使用的字符编码格式，支持GBK和UTF-8 **/
    private String CHARSET;
    /** 支付宝公钥，由支付宝生成 **/
    private String ALIPAY_PUBLIC_KEY;
    /** 商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2 **/
    private String SIGN_TYPE;
    /** 异步回调地址 **/
    private String notify_url;
}
