package org.pkaq.sys.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.tenant.entity.TenantRoleEntity;
import org.springframework.stereotype.Repository;

/**
 * 租户角色管理
 *
 * @author PKAQ
 */
@Mapper
@Repository
@Ignore
public interface TenantRoleMapper extends BaseMapper<TenantRoleEntity> {

   /**
    * 根据角色ID，删除拥有该角色的租户自行创建的角色中的模块资源脏数据
    */
   void removeRedundantModulesForTenantByRoleId(@Param("id") String roleId);

   /**
    * 根据角色id和租户id 删除该租户创建的角色中多余的模块
    * @param id 租户id
    * @param roleId 角色id
    *
    */
   void removeRedundantModulesForTenant(@Param("id")String id, @Param("roleId") String roleId);
}
