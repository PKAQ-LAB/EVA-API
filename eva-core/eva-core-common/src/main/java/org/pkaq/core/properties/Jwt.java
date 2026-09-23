package org.pkaq.core.properties;

import lombok.Data;

import java.util.List;

/**
 * jwt配置
 *
 * @author PKAQ
 */
@Data
public class Jwt {
    /**
     * 是否持久化
     **/
    private boolean persistence = true;
    /**
     * token header参数名
     **/
    private String header = "Authorization";
    /**
     * token 前缀
     **/
    private String tokenHead = "Bearer";
    /**
     * 签发人
     **/
    private String sign = "PKAQ";
    /**
     * 密匙
     **/
    private String secert = "6MNSobBRCHGIO0fS6MNSobBRCHGIO0fS";
    /**
     * Redis 会话有效时间，默认与 refresh token 一致
     **/
    private long ttl = 30 * 24 * 60 * 60 * 1000;
    /**
     * access token有效时间，默认 30 分钟
     **/
    private long alphaTtl = 30 * 60 * 1000;
    /**
     * refresh token有效时间, 30 天
     **/
    private long bravoTtl = 30 * 24 * 60 * 60 * 1000;
    /**
     * access token 自动续期阈值，默认 5 分钟
     **/
    private long threshold = 5 * 60 * 1000;
    /**
     * 可信任域
     **/
    private List<String> creditUrl;

}
