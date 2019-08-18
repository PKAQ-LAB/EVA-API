package io.nerv.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * jwt配置
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:39
 */
@Data
@ConfigurationProperties(prefix = "eva.cache")
public class CacheConfig {

    /**  缓存类型  默认map **/
    private String type = "map";

    /**  容量 - 0 无限制 **/
    private int capacity = 0;

    /**  有效时长 默认3秒 **/
    /** <code>0</code> 表示没有设置，单位毫秒 **/
    private long timeout = 1000 * 3;

}
