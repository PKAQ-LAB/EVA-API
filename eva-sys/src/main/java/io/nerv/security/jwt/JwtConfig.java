package io.nerv.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * jwt配置
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:39
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    //刷新TOKEN(有返回数据)
    //private int rescode_reftoken_msg = 1006;
    ////刷新TOKEN
    //private int rescode_reftoken = 1007;

    /**  token header参数名 **/
    private String header = "Authorization";

    /**  token 前缀 **/
    private String tokenHead = "Bearer";

    /**  签发人 **/
    private String sign = "PKAQ";

    /** 密匙 **/
    private String secert = "aHR0cDovL3BrYXEub3Jn";

    /** token有效时间 **/
    private long ttl = 30 * 24 * 60 * 60 * 1000;

    /** 续期时间 **/
    private long threshold = 60 * 60 * 24 * 1000;

    public void setSign(String sign){
        this.sign = sign;
    }
}
