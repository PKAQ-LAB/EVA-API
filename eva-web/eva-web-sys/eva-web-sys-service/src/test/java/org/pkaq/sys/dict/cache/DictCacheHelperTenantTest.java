package org.pkaq.sys.dict.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.cache.util.RedisUtil;
import org.pkaq.core.tenant.TenantContext;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * 验证字典缓存按租户 schema 隔离。
 *
 * @author PKAQ
 */
class DictCacheHelperTenantTest {

    @AfterEach
    void clearContext() {
        TenantContext.clear();
    }

    @Test
    void isolatesSameDictionaryCodeAcrossTenantSchemas() {
        ConcurrentMapCacheManager manager = new ConcurrentMapCacheManager(CommonConstant.CACHE_DICTDATA);
        RedisUtil redisUtil = mock(RedisUtil.class);
        DictCacheHelper helper = new DictCacheHelper(manager, redisUtil);

        TenantContext.bind(101L, "tenant_101");
        helper.cachePut("status", Map.of("1", "启用"));

        TenantContext.bind(102L, "tenant_102");
        assertNull(helper.get("status"));
        helper.cachePut("status", Map.of("1", "正常"));

        TenantContext.bind(101L, "tenant_101");
        assertEquals("启用", helper.get("status").get("1"));
        Map<String, Object> cachedDictionaries = Map.of(
                "tenant_101::status", Map.of("1", "启用"),
                "tenant_102::status", Map.of("1", "正常"));
        doReturn(cachedDictionaries).when(redisUtil)
                .scanPureAll(CommonConstant.CACHE_DICTDATA, "");
        helper.removeAll();
        assertNull(helper.get("status"));

        TenantContext.bind(102L, "tenant_102");
        assertEquals("正常", helper.get("status").get("1"));
    }
}
