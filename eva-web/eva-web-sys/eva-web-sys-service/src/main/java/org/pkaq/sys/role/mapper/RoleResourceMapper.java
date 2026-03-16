package org.pkaq.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.module.vo.ModuleResourcesVo;
import org.pkaq.sys.role.bo.RoleResourceRefBo;
import org.pkaq.sys.role.entity.RoleResourceEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
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
    @MapKey("MODULE_ID")
    Map<Long, List<ModuleResourcesVo>> listGrantedResource(Long roleId);

    /**
     * 删除权限中失效的引用关系
     *
     * @param moduleId 模块ID
     */
    void purgeBrokenRoleResourceRefs(Long moduleId);

    /**
     * 删除授权的资源
     *
     * @param ids 模块ID集合
     * @return 影响行数
     */
    int deleteByModuleIds(@Param("moduleIds") Set<Long> ids);
}
