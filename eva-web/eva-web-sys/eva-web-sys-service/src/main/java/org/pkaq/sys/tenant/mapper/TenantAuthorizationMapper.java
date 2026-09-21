package org.pkaq.sys.tenant.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 租户角色与套餐授权 Mapper。
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface TenantAuthorizationMapper {
    void revokeRoleGrants(@Param("tenantId") Long tenantId, @Param("roleIds") Set<Long> roleIds);

    void grantRoles(@Param("tenantId") Long tenantId, @Param("roleIds") Set<Long> roleIds);

    void revokePackageGrants(@Param("tenantId") Long tenantId, @Param("packageIds") Set<Long> packageIds);

    void grantPackages(@Param("tenantId") Long tenantId, @Param("packageIds") Set<Long> packageIds);

    void clearTenantResources(@Param("tenantId") Long tenantId);

    void insertDerivedTenantResources(@Param("tenantId") Long tenantId);

    Set<Long> selectGrantedRoleIds(@Param("tenantId") Long tenantId);

    Set<Long> selectGrantedPackageIds(@Param("tenantId") Long tenantId);

    long countActivePackageGrants(@Param("packageIds") Set<Long> packageIds);

    List<Long> selectInvalidTemplateRoleIds(@Param("roleIds") Set<Long> roleIds);

    List<Long> selectInvalidPackageIds(@Param("packageIds") Set<Long> packageIds);

    void ensureTenantAdminRole(@Param("tenantId") Long tenantId);

    void bindTenantAdminRole(@Param("tenantId") Long tenantId);

    void clearTenantAdminResources(@Param("tenantId") Long tenantId);

    void refreshTenantAdminResources(@Param("tenantId") Long tenantId);
}
