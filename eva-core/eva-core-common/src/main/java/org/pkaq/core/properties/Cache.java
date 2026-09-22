package org.pkaq.core.properties;

import lombok.Data;

import java.time.Duration;
import java.util.List;

/**
 * Cache配置
 *
 * @author PKAQ
 */
@Data
public class Cache {

    private List<CacheObject> config;

    @Data
    public static class CacheObject {
        /**
         * 缓存名
         **/
        private String name = "default";

        /**
         * 缓存过期时间
         **/
        private Duration ttl = Duration.ZERO;
    }
}
