package io.nerv.core.cache.config;

import cn.hutool.core.date.DateUnit;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;

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

    /**  有效时长 默认5秒 **/
    /** <code>0</code> 表示没有设置，单位毫秒 **/
    private long timeout = DateUnit.SECOND.getMillis() * 5;

}
