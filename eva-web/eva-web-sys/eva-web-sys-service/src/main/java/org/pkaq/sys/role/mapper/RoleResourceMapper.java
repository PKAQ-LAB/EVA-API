package org.pkaq.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.module.vo.ModuleResourcesVo;
import org.pkaq.sys.role.bo.RoleResourceRefBo;
import org.pkaq.sys.role.entity.RoleResourceEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 角色模块关系Mapper
 *
 * @author PKAQ
 */
@Mapper
@Repository
@Ignore
public interface RoleResourceMapper extends BaseMapper<RoleResourceEntity> {

    /**
     * 获取已选择且为叶子节点的模块
     *
     * @param bo 参数
     * @return 模块列表
     */
    List<RoleResourceEntity> roleModuleList(@Param("entity") RoleResourceRefBo bo);

    /**
     * 查询资源并对该角色已拥有的资源打标记
     *
     * @param roleId 角色ID
     * @return 资源列表
     */
    List<ModuleResourcesVo> listGrantedResource(Long roleId);

    /**
     * 删除权限中失效的引用关系
     *
     * @param moduleId 模块ID
     */
    void purgeBrokenRoleResourceRefs(@Param("moduleId") Long moduleId);

    /**
     * 删除指定资源的角色授权关系
     *
     * @param resourceIds 资源ID集合
     * @return 影响行数
     */
    int deleteByResourceIds(@Param("resourceIds") Set<Long> resourceIds);

    /**
     * 删除授权的资源
     *
     * @param ids 模块ID集合
     * @return 影响行数
     */
    int deleteByModuleIds(@Param("moduleIds") Set<Long> ids);

    /**
     * 查询引用指定资源的角色。
     *
     * @param resourceIds 资源ID集合
     * @return 角色ID集合
     */
    Set<Long> selectRoleIdsByResourceIds(@Param("resourceIds") Set<Long> resourceIds);

    /**
     * 查询引用指定模块资源的角色。
     *
     * @param moduleIds 模块ID集合
     * @return 角色ID集合
     */
    Set<Long> selectRoleIdsByModuleIds(@Param("moduleIds") Set<Long> moduleIds);

    /**
     * 查询仍然有效的资源ID。
     *
     * @param resourceIds 资源ID集合
     * @return 有效资源ID集合
     */
    Set<Long> selectValidResourceIds(@Param("resourceIds") Set<Long> resourceIds);
}
