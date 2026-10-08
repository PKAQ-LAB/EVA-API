package org.pkaq.core.auth.adapter.mybatis.openapi.service;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.adapter.mybatis.openapi.entity.AppCredentialEntity;
import org.pkaq.core.auth.adapter.mybatis.openapi.mapper.AppCredentialMapper;
import org.pkaq.core.auth.spi.IAppCredentialQuery;
import org.pkaq.core.auth.spi.model.AppCredentialSnapshot;
import org.pkaq.core.util.json.JsonUtil;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 应用凭据存储适配与认证快照测试。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class AppKeyServiceTest {

    /** 凭据可供验签读取，但不得通过序列化或日志字符串泄漏。 */
    @Test
    void excludesSecretFromJsonAndLogString() {
        AppCredentialSnapshot snapshot = new AppCredentialSnapshot();
        snapshot.setAppKey("test-key");
        snapshot.setAppSecret("sensitive-test-secret");

        String json = JsonUtil.toJson(snapshot);

        assertEquals("sensitive-test-secret", snapshot.getAppSecret());
        assertTrue(json.contains("test-key"));
        assertFalse(json.contains("appSecret"));
        assertFalse(json.contains("sensitive-test-secret"));
        assertFalse(snapshot.toString().contains("sensitive-test-secret"));
    }

    /** 验证存储字段和认证行为完整映射到普通快照。 */
    @Test
    void mapsCredentialWithoutChangingAccessRules() {
        AppCredentialMapper mapper = mock(AppCredentialMapper.class);
        AppCredentialEntity entity = new AppCredentialEntity();
        entity.setAppKey("test-key");
        entity.setAppSecret("test-secret");
        entity.setAppName("测试应用");
        entity.setStatus(1);
        entity.setRateLimit(20);
        entity.setApiPermissions("/public/order/*,/public/profile");
        entity.setIpWhitelist("192.0.2.1");
        entity.setExpireTime(LocalDateTime.now().plusDays(1));
        when(mapper.findByAppKey("test-key")).thenReturn(entity);
        IAppCredentialQuery query = new AppKeyService(mapper);

        var credential = query.findCredential("test-key");

        assertEquals(entity.getAppSecret(), credential.getAppSecret());
        assertEquals(entity.getAppName(), credential.getAppName());
        assertEquals(entity.getRateLimit(), credential.getRateLimit());
        assertEquals(entity.getExpireTime(), credential.getExpireTime());
        assertTrue(credential.isValid());
        assertTrue(credential.hasApiPermission("/public/order/42"));
        assertTrue(credential.hasApiPermission("/public/profile"));
        assertFalse(credential.hasApiPermission("/private/profile"));
        assertTrue(credential.isIpAllowed("192.0.2.1"));
        assertFalse(credential.isIpAllowed("192.0.2.2"));
        entity.setStatus(0);
        assertFalse(query.findCredential("test-key").isValid());
        entity.setStatus(1);
        entity.setExpireTime(LocalDateTime.now().minusDays(1));
        assertFalse(query.findCredential("test-key").isValid());
        assertNull(query.findCredential("missing"));
    }
}
