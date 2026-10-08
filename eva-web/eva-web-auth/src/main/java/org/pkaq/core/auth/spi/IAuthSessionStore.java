package org.pkaq.core.auth.spi;

import java.time.Duration;
import java.util.Map;

/**
 * 在线会话存储契约。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface IAuthSessionStore {
    /** 保存会话与剩余有效期。 */
    void save(Long tenantId, Long userId, String sessionId, Object value, Duration ttl);
    /** 读取会话。 */
    Object get(Long tenantId, Long userId, String sessionId);
    /** 删除设备会话。 */
    void remove(Long tenantId, Long userId, String sessionId);
    /** 删除用户所有会话。 */
    void removeUser(Long tenantId, Long userId);
    /** 读取租户会话；空租户表示全部租户。 */
    Map<String, Object> list(Long tenantId);
    /** 删除租户全部会话。 */
    void removeTenant(Long tenantId);
}
