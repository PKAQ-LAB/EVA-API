package org.pkaq.core.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.pkaq.core.auth.session.RedisSessionStore;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.web.core.utils.RequestUtil;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author PKAQ
 */
@Component
public class CacheTokenUtil {

    private final JwtUtil jwtUtil;

    private final RedisSessionStore sessionStore;

    private final Duration sessionTtl;

    public CacheTokenUtil(JwtUtil jwtUtil, RedisSessionStore sessionStore, EvaConfig evaConfig) {
        this.jwtUtil = jwtUtil;
        this.sessionStore = sessionStore;
        this.sessionTtl = Duration.ofMillis(evaConfig.getJwt().getTtl());
    }

    /**
     * 构造token缓存的value
     */
    public Map<String, Object> buildCacheValue(HttpServletRequest request, Long uid, String token) {
        return Map.of("device", RequestUtil.getDeivce(request),
                "version", RequestUtil.getVersion(request),
                "issuedAt", jwtUtil.getIssuedAt(token),
                "expireAt", jwtUtil.getExpirationDateFromToken(token),
                "loginTime", LocalDateTime.now(),
                "account", uid,
                "token", token);
    }

    /**
     * 持久化 token
     *
     * @param key
     * @param value
     */
    public void saveToken(Long tenantId, Long userId, Object value) {
        this.sessionStore.save(tenantId, userId, value, sessionTtl);
    }

    public void saveToken(Long userId, Object value) {
        saveToken(0L, userId, value);
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
    public Object getToken(Long tenantId, Long userId) {
        return this.sessionStore.get(tenantId, userId);
    }

    public Object getToken(Long userId) {
        return getToken(0L, userId);
    }

    /**
     * 从缓存中清除token
     *
     * @param key
     */
    public void removeToken(String key) {
        String[] keyParts = key == null ? new String[0] : key.split(":", 2);
        if (keyParts.length == 2) {
            this.sessionStore.remove(Long.valueOf(keyParts[0]), Long.valueOf(keyParts[1]));
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
            this.sessionStore.remove(tenantId, userId);
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

}
