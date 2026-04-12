package org.pkaq.core.auth.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.pkaq.core.auth.rbac.entity.SysRoleResource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色资源Mapper
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface SysRoleResourceMapper extends BaseMapper<SysRoleResource> {

    /**
     * 查询指定角色的所有资源权限
     *
     * @param roleId 角色ID
     * @return 资源列表
     */
    @Select("SELECT * FROM SYS_ROLE_RESOURCE WHERE ROLE_ID = #{roleId}")
    List<SysRoleResource> selectByRoleId(Long roleId);

    /**
     * 查询所有角色资源映射
     *
     * @return 全部角色资源
     */
    @Select("SELECT * FROM SYS_ROLE_RESOURCE")
    List<SysRoleResource> selectAll();
}
