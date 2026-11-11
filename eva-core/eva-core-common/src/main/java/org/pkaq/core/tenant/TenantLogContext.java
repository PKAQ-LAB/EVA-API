package org.pkaq.core.tenant;

import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.slf4j.MDC;

/**
 * 日志租户上下文，不改变认证身份或数据库路由。
 *
 * @author PKAQ
 * @date 2026-11-11
 */
public final class TenantLogContext {
    /** 日志专用的可信租户标识。 */
    public static final String KEY = "evaLogTenantId";

    private TenantLogContext() {
    }

    /**
     * 获取事件所属租户，优先保留日志显式身份，避免核心库路由改变归属。
     */
    public static String getTenantId() {
        String explicit = MDC.get(KEY);
        // 显式日志身份优先于临时数据库路由。
        if (explicit != null) {
            return explicit;
        }
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        Long tenantId = user == null ? TenantContext.tenantId() : Long.valueOf(user.getTenantId());
        return tenantId == null ? null : tenantId.toString();
    }

    /**
     * 在可信租户归属下输出日志，退出时恢复调用者原上下文。
     */
    public static void runWithTenant(Long tenantId, Runnable action) {
        String previous = MDC.get(KEY);
        try {
            // 未知身份不能继承线程池残留的租户。
            MDC.put(KEY, tenantId == null ? "0" : tenantId.toString());
            action.run();
        } finally {
            // 包含异常和调用者执行策略，均恢复原值。
            if (previous == null) {
                MDC.remove(KEY);
            } else {
                MDC.put(KEY, previous);
            }
        }
    }
}
