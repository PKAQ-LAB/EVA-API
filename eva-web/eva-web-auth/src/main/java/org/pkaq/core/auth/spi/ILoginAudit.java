package org.pkaq.core.auth.spi;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录审计写入契约。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface ILoginAudit {
    /** 记录成功登录。 */
    void saveSuccess(HttpServletRequest request, Long tenantId, Long userId, String account, String sessionId);
    /** 记录失败登录。 */
    void saveFailure(HttpServletRequest request, String failReason);
    /** 记录当前用户退出。 */
    void saveLogout(HttpServletRequest request, String sessionId, String logoutReason);
    /** 记录可信令牌身份退出。 */
    void saveLogout(HttpServletRequest request, Long tenantId, Long userId, String account,
                    String sessionId, String logoutReason);
    /** 兼容无会话标识的退出。 */
    void saveLogout(HttpServletRequest request);
    /** 关闭指定会话的审计记录。 */
    void closeSession(Long tenantId, Long userId, String sessionId, String logoutReason);
    /** 轮换会话标识。 */
    void rotateSession(Long tenantId, Long userId, String oldSessionId, String newSessionId);
    /** 关闭用户全部会话的审计记录。 */
    void closeUserSessions(Long tenantId, Long userId, String logoutReason);
}
