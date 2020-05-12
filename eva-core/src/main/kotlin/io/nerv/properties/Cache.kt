package io.nerv.properties

/**
 * Cache配置
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:39
 */
class Cache {
    /**  缓存类型  默认 caffeine  */
    var type = "caffeine"
    var config: List<CacheObject>? = null

    class CacheObject {
        /**  缓存名  */
        var name = "default"

        /**  缓存初始大小  */
        var initialCapacity = 100

        /**  缓存过期时间  */
        var secondsToExpire = 100

        /*** 使用描述创建缓存  */
        var spec: String? = null
    }
}