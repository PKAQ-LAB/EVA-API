package org.pkaq.core.auth.spi;

/**
 * 集中接口审计的有效管理员资格查询。
 * @author PKAQ
 * @date 2026-10-24
 */
public interface IOpenApiAuditAdministratorQuery {
    /**
     * 在调用者已确定的可信schema核验零号租户账号及有效管理员关联。
     * @param userId 服务端可信账号ID
     * @return 是否有未删除、未禁用的账号和管理员角色关联
     */
    boolean isActiveAdministrator(long userId);
}
