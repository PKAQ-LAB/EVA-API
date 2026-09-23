package org.pkaq.core.auth.session;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Redis 在线会话状态存储。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class RedisSessionStore {

    private static final String KEY_PREFIX = "eva:session:";

    private final RedisTemplate<Object, Object> redisTemplate;

    /**
     * 保存在线会话。
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @param sessionId 会话标识
     * @param value 会话元数据
     * @param ttl 有效时间
     */
    public void save(Long tenantId, Long userId, String sessionId, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(storageKey(tenantId, userId, sessionId), value, ttl);
    }

    /**
     * 获取在线会话。
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @param sessionId 会话标识
     * @return 会话元数据
     */
    public Object get(Long tenantId, Long userId, String sessionId) {
        return redisTemplate.opsForValue().get(storageKey(tenantId, userId, sessionId));
    }

    /**
     * 删除在线会话。
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @param sessionId 会话标识
     */
    public void remove(Long tenantId, Long userId, String sessionId) {
        redisTemplate.delete(storageKey(tenantId, userId, sessionId));
    }

    /**
     * 删除指定用户的全部设备会话。
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     */
    public void removeUser(Long tenantId, Long userId) {
        scan(storageKeyPrefix(tenantId, userId) + "*").keySet().forEach(this::removeLogicalKey);
    }

    /**
     * 按租户读取在线会话，tenantId 为空时读取全部租户。
     *
     * @param tenantId 租户ID
     * @return 逻辑会话键到元数据的映射
     */
    public Map<String, Object> list(Long tenantId) {
        String tenantPrefix = tenantId == null ? "" : trustedTenantId(tenantId) + ":";
        return scan(KEY_PREFIX + tenantPrefix + "*");
    }

    private Map<String, Object> scan(String pattern) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(500)
                .build();
        Map<String, Object> sessions = new LinkedHashMap<>();
        try (Cursor<Object> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(item -> addSession(sessions, item));
        }
        return sessions;
    }

    /**
     * 删除指定租户的全部会话。
     *
     * @param tenantId 租户ID
     */
    public void removeTenant(Long tenantId) {
        list(tenantId).keySet().forEach(this::removeLogicalKey);
    }

    private void addSession(Map<String, Object> sessions, Object item) {
        String storedKey = String.valueOf(item);
        if (!storedKey.startsWith(KEY_PREFIX)) {
            return;
        }
        Object value = redisTemplate.opsForValue().get(storedKey);
        if (value != null) {
            sessions.put(storedKey.substring(KEY_PREFIX.length()), value);
        }
    }

    private void removeLogicalKey(String logicalKey) {
        redisTemplate.delete(KEY_PREFIX + logicalKey);
    }

    private String storageKey(Long tenantId, Long userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("会话标识不能为空");
        }
        return storageKeyPrefix(tenantId, userId) + sessionId;
    }

    private String storageKeyPrefix(Long tenantId, Long userId) {
        return KEY_PREFIX + trustedTenantId(tenantId) + ":" + userId + ":";
    }

    private long trustedTenantId(Long tenantId) {
        return tenantId == null ? 0L : tenantId;
    }
}
