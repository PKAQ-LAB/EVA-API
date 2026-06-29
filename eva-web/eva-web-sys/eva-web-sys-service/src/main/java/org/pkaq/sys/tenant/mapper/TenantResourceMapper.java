package org.pkaq.sys.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.sys.tenant.entity.TenantResourceEntity;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * 租户授权资源 Mapper。
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface TenantResourceMapper extends BaseMapper<TenantResourceEntity> {
    Set<Long> selectAuthorizedResourceIds(@Param("tenantId") Long tenantId);

    int deleteUnauthorizedRoleResources(@Param("tenantId") Long tenantId,
                                        @Param("resourceIds") Set<Long> resourceIds);

    Set<Long> selectTenantUserIds(@Param("tenantId") Long tenantId);

    Set<Long> selectTenantRoleIds(@Param("tenantId") Long tenantId);
}
