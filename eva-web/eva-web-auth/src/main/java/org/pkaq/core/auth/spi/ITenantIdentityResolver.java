package org.pkaq.core.auth.spi;

import org.pkaq.core.auth.tenant.TenantLoginIdentity;

/**
 * 可信租户身份解析契约。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface ITenantIdentityResolver {
    /** 通过租户编码解析登录身份。 */
    TenantLoginIdentity resolveCode(String tenantCode);
    /** 通过可信租户标识解析登录身份。 */
    TenantLoginIdentity resolveId(Long tenantId);
}
