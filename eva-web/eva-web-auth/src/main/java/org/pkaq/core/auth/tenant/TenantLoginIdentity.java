package org.pkaq.core.auth.tenant;

/**
 * 服务端解析的可信租户登录身份。
 *
 * @author PKAQ
 */
public record TenantLoginIdentity(Long tenantId, long schemaGeneration) {
}
