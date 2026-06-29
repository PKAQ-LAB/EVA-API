package org.pkaq.sys.tenant.pkg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.sys.tenant.pkg.entity.TenantPackageResourceEntity;

import java.util.Set;

/**
 * 租户套餐授权资源 Mapper。
 *
 * @author PKAQ
 */
@Mapper
public interface TenantPackageResourceMapper extends BaseMapper<TenantPackageResourceEntity> {
    Set<Long> selectAuthorizedResourceIds(@Param("packageId") Long packageId);
}
