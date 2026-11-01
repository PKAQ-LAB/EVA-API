package org.pkaq.sys.dict.cache;

import org.pkaq.core.cache.util.RedisUtil;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.util.json.JsonUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 字典内部缓存；来源必须由服务端核验后显式传入。
 * @author PKAQ
 * @date 2026-11-01
 */
@Component
public class DictCacheHelper {
    private final RedisUtil redisUtil;
    private final Cache cache;

    public DictCacheHelper(CacheManager cacheManager, RedisUtil redisUtil) {
        this.cache = cacheManager.getCache(CommonConstant.CACHE_DICTDATA);
        this.redisUtil = redisUtil;
    }

    /** 不从临时线程上下文或客户端参数猜测归属。 */
    public record Owner(String schema, String category) {
        public Owner {
            if (schema == null || !schema.matches("[a-z_][a-z0-9_]{0,62}")
                    || !("SYSTEM".equals(category) || "BUSINESS".equals(category))) {
                throw new IllegalArgumentException("字典缓存来源非法");
            }
        }

        String prefix() {
            return "v2::" + schema + "::" + category + "::";
        }
    }

    /** 仅读取已确认来源的第二版键，旧版键不参与命中。 */
    @SuppressWarnings("unchecked")
    public Map<String, String> get(Owner owner, String code) {
        Cache.ValueWrapper value = cache.get(key(owner, code));
        return value == null ? null : JsonUtil.parse((String) value.get(), LinkedHashMap.class);
    }

    /** 只接收受控数据库读取形成的完整字典值。 */
    public void cachePut(Owner owner, String code, Map<String, String> items) {
        cache.put(key(owner, code), JsonUtil.toJson(items));
    }

    /** 提交回调使用已捕获来源，无须重新绑定上下文。 */
    public void remove(Owner owner, String code) {
        cache.evict(key(owner, code));
    }

    /** 限定非空精确前缀，禁止全缓存清理及读取旧键。 */
    public void removeAll(Owner owner) {
        String prefix = owner.prefix();
        Map<?, ?> entries = redisUtil.scanPureAll(CommonConstant.CACHE_DICTDATA, prefix);
        if (entries != null) {
            entries.keySet().stream().map(String::valueOf)
                    .filter(value -> value.startsWith(prefix)).forEach(cache::evict);
        }
    }

    private String key(Owner owner, String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("字典编码不能为空");
        }
        return owner.prefix() + code;
    }
}
