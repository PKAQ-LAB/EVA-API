package io.nerv.properties;

import lombok.Data;

import java.util.List;

/**
 * jwt配置
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:39
 */
@Data
public class Jwt {
    /** 是否持久化 **/
    private boolean persistence;
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
    /** access token有效时间,  6 小时 **/
    private long alphaTtl =  6 * 60 * 60 * 1000;
    /** refresh token有效时间, 30 天 **/
    private long bravoTtl = 30 * 24 * 60 * 60 * 1000;
    /** 续期时间 , 6 小时 **/
    private long threshold = 60 * 60 * 6 * 1000;
    /** 可信任域 **/
    private List<String> creditUrl;

}
