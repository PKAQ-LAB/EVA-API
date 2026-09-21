package org.pkaq.core.mybatis.tenant;

/**
 * 根据服务端租户 ID 解析可信 schema。
 *
 * @author PKAQ
 */
public interface TenantSchemaResolver {
    String resolve(Long tenantId);
}
