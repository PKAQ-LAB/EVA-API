package org.pkaq.core.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.pkaq.core.auth.session.RedisSessionStore;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.web.core.client.ClientInfo;
import org.pkaq.web.core.client.ClientInfoResolver;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author PKAQ
 */
@Component
public class CacheTokenUtil {

    private static final String ACCESS_TOKEN_HASH = "accessTokenHash";
    private static final String REFRESH_TOKEN_HASH = "refreshTokenHash";
    private static final String SESSION_EXPIRE_AT = "sessionExpireAt";

    private final JwtUtil jwtUtil;

    private final RedisSessionStore sessionStore;

    private final ClientInfoResolver clientInfoResolver;

    private final Duration sessionTtl;

    private final Duration activityUpdateInterval;

    public CacheTokenUtil(JwtUtil jwtUtil,
                          RedisSessionStore sessionStore,
                          ClientInfoResolver clientInfoResolver,
                          EvaConfig evaConfig) {
        this.jwtUtil = jwtUtil;
        this.sessionStore = sessionStore;
        this.clientInfoResolver = clientInfoResolver;
        this.sessionTtl = Duration.ofMillis(evaConfig.getJwt().getBravoTtl());
        this.activityUpdateInterval = Duration.ofSeconds(
                Math.max(1L, evaConfig.getClientInfo().getActivityUpdateIntervalSeconds()));
    }

    /**
     * 构造token缓存的value
     */
    public Map<String, Object> buildCacheValue(HttpServletRequest request,
                                               Long uid,
                                               String accessToken,
                                               String refreshToken) {
        Map<String, Object> value = new LinkedHashMap<>();
        ClientInfo clientInfo = clientInfoResolver.resolve(request);
        value.put("device", clientInfo.deviceType());
        value.put("version", clientInfo.clientVersion());
        value.put("ip", clientInfo.ip());
        value.put("deviceModel", clientInfo.deviceModel());
        value.put("osName", clientInfo.osName());
        value.put("osVersion", clientInfo.osVersion());
        value.put("browserName", clientInfo.browserName());
        value.put("browserVersion", clientInfo.browserVersion());
        value.put("deviceFingerprint", clientInfo.fingerprint());
        value.put("issuedAt", jwtUtil.getIssuedAt(accessToken));
        value.put("expireAt", jwtUtil.getExpirationDateFromToken(accessToken));
        value.put("loginTime", System.currentTimeMillis());
        value.put("lastActiveAt", System.currentTimeMillis());
        value.put("account", uid);
        value.put(ACCESS_TOKEN_HASH, hash(accessToken));
        value.put(REFRESH_TOKEN_HASH, hash(refreshToken));
        Date refreshExpireAt = jwtUtil.getExpirationDateFromToken(refreshToken);
        value.put(SESSION_EXPIRE_AT, refreshExpireAt == null
                ? System.currentTimeMillis() + sessionTtl.toMillis()
                : refreshExpireAt.getTime());
        return value;
    }

    /**
     * 持久化 token
     *
     * @param key
     * @param value
     */
    public void saveToken(Long tenantId, Long userId, String sessionId, Object value) {
        this.sessionStore.save(tenantId, userId, sessionId, value, remainingTtl(value));
    }

    public void saveToken(Long userId, String sessionId, Object value) {
        saveToken(0L, userId, sessionId, value);
    }


    /***
     * 获取所有的token
     * @return
     */
    public Map<?, ?> getAllToken() {
        return getTokens(null);
    }

    /**
     * 按可信租户范围读取在线会话。
     *
     * @param tenantId 租户ID；null 表示平台全局范围
     * @return 逻辑缓存键到会话元数据的映射
     */
    public Map<?, ?> getTokens(Long tenantId) {
        return this.sessionStore.list(tenantId);
    }

    /**
     * 获取token缓存
     *
     * @param uid
     * @return
     */
    public Object getToken(Long tenantId, Long userId, String sessionId) {
        return this.sessionStore.get(tenantId, userId, sessionId);
    }

    public Object getToken(Long userId, String sessionId) {
        return getToken(0L, userId, sessionId);
    }

    /**
     * 校验当前 access token 是否属于指定会话。
     */
    public boolean matchesAccessToken(Long tenantId, Long userId, String sessionId, String token) {
        return matchesToken(getToken(tenantId, userId, sessionId), ACCESS_TOKEN_HASH, token);
    }

    /**
     * 校验当前 refresh token 是否属于指定会话。
     */
    public boolean matchesRefreshToken(Long tenantId, Long userId, String sessionId, String token) {
        return matchesToken(getToken(tenantId, userId, sessionId), REFRESH_TOKEN_HASH, token);
    }

    /**
     * 替换会话中的 access token，保留 refresh token 及原始会话过期时间。
     */
    public boolean replaceAccessToken(Long tenantId,
                                      Long userId,
                                      String sessionId,
                                      HttpServletRequest request,
                                      String accessToken) {
        Object stored = getToken(tenantId, userId, sessionId);
        if (!(stored instanceof Map<?, ?> source)) {
            return false;
        }
        Map<String, Object> updated = new LinkedHashMap<>();
        source.forEach((key, value) -> updated.put(String.valueOf(key), value));
        ClientInfo clientInfo = clientInfoResolver.resolve(request);
        updated.put("device", clientInfo.deviceType());
        updated.put("version", clientInfo.clientVersion());
        updated.put("ip", clientInfo.ip());
        updated.put("deviceModel", clientInfo.deviceModel());
        updated.put("osName", clientInfo.osName());
        updated.put("osVersion", clientInfo.osVersion());
        updated.put("browserName", clientInfo.browserName());
        updated.put("browserVersion", clientInfo.browserVersion());
        updated.put("lastActiveAt", System.currentTimeMillis());
        updated.put("issuedAt", jwtUtil.getIssuedAt(accessToken));
        updated.put("expireAt", jwtUtil.getExpirationDateFromToken(accessToken));
        updated.put(ACCESS_TOKEN_HASH, hash(accessToken));
        saveToken(tenantId, userId, sessionId, updated);
        return true;
    }

    /**
     * 按配置的最小间隔更新在线会话最后活动时间。
     */
    public void touchSession(Long tenantId, Long userId, String sessionId) {
        Object stored = getToken(tenantId, userId, sessionId);
        if (!(stored instanceof Map<?, ?> source)) {
            return;
        }
        long now = System.currentTimeMillis();
        Object lastActiveValue = source.get("lastActiveAt");
        if (lastActiveValue instanceof Number lastActiveAt
                && lastActiveAt.longValue() + activityUpdateInterval.toMillis() > now) {
            return;
        }
        Map<String, Object> updated = new LinkedHashMap<>();
        source.forEach((key, value) -> updated.put(String.valueOf(key), value));
        updated.put("lastActiveAt", now);
        saveToken(tenantId, userId, sessionId, updated);
    }

    /**
     * 从缓存中清除token
     *
     * @param key
     */
    public void removeToken(String key) {
        String[] keyParts = key == null ? new String[0] : key.split(":", 3);
        if (keyParts.length == 3) {
            this.sessionStore.remove(Long.valueOf(keyParts[0]), Long.valueOf(keyParts[1]), keyParts[2]);
        }
    }

    /**
     * 从缓存中按用户 ID 清除 token（与 saveToken(Long) 配对）
     * 用于冻结用户 / 冻结租户下用户时立即踢下线
     *
     * @param uid 用户 ID
     */
    public void removeToken(Long uid) {
        removeToken(0L, uid);
    }

    public void removeToken(Long tenantId, Long userId) {
        if (userId != null) {
            this.sessionStore.removeUser(tenantId, userId);
        }
    }

    /**
     * 删除指定设备会话。
     */
    public void removeToken(Long tenantId, Long userId, String sessionId) {
        if (userId != null && sessionId != null && !sessionId.isBlank()) {
            this.sessionStore.remove(tenantId, userId, sessionId);
        }
    }

    /**
     * 批量按用户 ID 清除 token
     *
     * @param uids 用户 ID 集合
     */
    public void removeTokens(java.util.Collection<Long> uids) {
        removeTokens(0L, uids);
    }

    public void removeTokens(Long tenantId, java.util.Collection<Long> uids) {
        if (uids == null || uids.isEmpty()) {
            return;
        }
        for (Long uid : uids) {
            removeToken(tenantId, uid);
        }
    }

    public void removeTenantTokens(Long tenantId) {
        this.sessionStore.removeTenant(tenantId);
    }

    private boolean matchesToken(Object stored, String key, String token) {
        if (!(stored instanceof Map<?, ?> value) || token == null || token.isBlank()) {
            return false;
        }
        Object expected = value.get(key);
        if (expected == null) {
            return false;
        }
        return MessageDigest.isEqual(String.valueOf(expected).getBytes(StandardCharsets.UTF_8),
                hash(token).getBytes(StandardCharsets.UTF_8));
    }

    private Duration remainingTtl(Object value) {
        if (value instanceof Map<?, ?> map) {
            Object expireAt = map.get(SESSION_EXPIRE_AT);
            if (expireAt instanceof Number number) {
                long remaining = number.longValue() - System.currentTimeMillis();
                return Duration.ofMillis(Math.max(1L, remaining));
            }
        }
        return sessionTtl;
    }

    private String hash(String token) {
        if (token == null || token.isBlank()) {
            return "";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前JVM不支持SHA-256", exception);
        }
    }

}
