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

    /**
     * 查询需要重建接口权限缓存的有效角色。
     *
     * @return 有效角色 ID
     */
    @Select("SELECT ID FROM SYS_ROLE WHERE COALESCE(DELETED, 0) = 0")
    List<Long> selectActiveRoleIds();

    /**
     * 查询角色当前有效的模块资源授权。
     *
     * @param roleId 角色ID
     * @return 有效资源授权
     */
    @Select("""
            SELECT
                rr.ROLE_ID AS ROLE_ID,
                mr.RESOURCE_URL AS RESOURCE_PATH,
                UPPER(mr.RESOURCE_TYPE) AS HTTP_METHOD
            FROM SYS_ROLERES_REF rr
                JOIN SYS_MODULE_RESOURCES mr ON rr.RESOURCE_ID = mr.ID
                JOIN SYS_MODULE m ON mr.MAIN_ID = m.ID
                JOIN SYS_ROLE role ON rr.ROLE_ID = role.ID
            WHERE rr.ROLE_ID = #{roleId}
                AND (mr.DELETED = 0 OR mr.DELETED IS NULL)
                AND (m.DELETED = 0 OR m.DELETED IS NULL)
                AND (m.FROZEN <> 1 OR m.FROZEN IS NULL)
                AND (role.DELETED = 0 OR role.DELETED IS NULL)
                AND (role.FROZEN <> 1 OR role.FROZEN IS NULL)
                AND UPPER(mr.RESOURCE_TYPE) IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH', '*')
                AND mr.RESOURCE_URL IS NOT NULL
                AND mr.RESOURCE_URL <> ''
                AND (COALESCE(role.TENANT_ID, 0) = 0 OR EXISTS (
                    SELECT 1 FROM SYS_TENANT_RESOURCE tenant_resource
                    WHERE tenant_resource.TENANT_ID = role.TENANT_ID
                        AND tenant_resource.RESOURCE_ID = mr.ID
                ))
            ORDER BY mr.SORT ASC, mr.ID ASC
            """)
    List<SysRoleResource> selectEffectiveResourcesByRoleId(Long roleId);
}
