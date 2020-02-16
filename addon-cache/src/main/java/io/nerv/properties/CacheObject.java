package io.nerv.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * jwt配置
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:39
 */
@Data
public class CacheObject {
    /**  缓存名 **/
    private String name = "default";

    /**  缓存初始大小 **/
    private int initialCapacity = 100;

    /**  缓存过期时间 **/
    private int secondsToExpire = 100;

    /*** 使用描述创建缓存 ***/
    private String spec;

}
