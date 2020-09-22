package io.nerv.properties;

import lombok.Data;

import java.util.List;

/**
 * Cache配置
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:39
 */
@Data
public class Cache {

    /**  缓存类型  默认 caffeine **/
    private String type = "caffeine";

    private List<CacheObject> config;

    @Data
    public static class CacheObject {
        /**  缓存名 **/
        private String name = "default";

        /**  缓存初始大小 **/
        private int initialCapacity = 100;

        /**  缓存过期时间 **/
        private int secondsToExpire = 100;

        /*** 使用描述创建缓存 ***/
        private String spec;
    }
}
