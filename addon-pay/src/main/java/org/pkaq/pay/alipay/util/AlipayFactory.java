package org.pkaq.pay.alipay.util;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.pkaq.pay.alipay.config.AlipayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
public class AlipayFactory {
    @Autowired
    private AlipayConfig alipayConfig;

    /** 支付宝网关 **/
    private static String URL;
    /** APPID即创建应用后生成 **/
    private static String APP_ID;
    /** 开发者应用私钥，由开发者自己生成 **/
    private static String APP_PRIVATE_KEY;
    /** 参数返回格式，只支持json **/
    private static final String FORMAT = "json";
    /** 请求和签名使用的字符编码格式，支持GBK和UTF-8 **/
    private static String CHARSET;
    /** 支付宝公钥，由支付宝生成 **/
    private static String ALIPAY_PUBLIC_KEY;
    /** 商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2 **/
    private static String SIGN_TYPE;

    private AlipayFactory(){}

    public AlipayClient getAlipayClient(){
        URL = alipayConfig.getURL();
        APP_ID = alipayConfig.getAPP_ID();
        APP_PRIVATE_KEY = alipayConfig.getAPP_PRIVATE_KEY();
        CHARSET = alipayConfig.getCHARSET();
        ALIPAY_PUBLIC_KEY = alipayConfig.getALIPAY_PUBLIC_KEY();
        SIGN_TYPE = alipayConfig.getSIGN_TYPE();

        return AlipayHolder.alipayClient;
    }

    private static class AlipayHolder{

        private static AlipayClient alipayClient = new DefaultAlipayClient(URL,
                APP_ID,
                APP_PRIVATE_KEY,
                FORMAT,
                CHARSET,
                ALIPAY_PUBLIC_KEY,
                SIGN_TYPE);
    }
}
