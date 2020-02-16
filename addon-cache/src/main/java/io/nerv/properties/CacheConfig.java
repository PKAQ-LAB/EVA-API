package io.nerv.properties;

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

    /**  缓存类型  默认 caffeine **/
    private String type = "caffeine";

    private List<CacheObject> config;

}
