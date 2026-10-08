package org.pkaq.core.auth.spi;

import java.util.function.Supplier;

/**
 * 认证租户执行上下文契约。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface ITenantAuthRouter {
    /** 在可信租户上下文执行并恢复调用前上下文。 */
    <T> T execute(Long tenantId, Supplier<T> action);
}
