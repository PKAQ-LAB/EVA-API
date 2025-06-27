package org.pkaq.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.module.vo.ResourcesVo;
import org.pkaq.sys.role.bo.RoleResourceRefBo;
import org.pkaq.sys.role.entity.RoleResourceEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 角色模块关系mapper
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
@Ignore
public interface RoleResourceMapper extends BaseMapper<RoleResourceEntity> {

    /**
     * 获取已选且是叶子节点的模块
     *
     * @param bo
     * @return
     */
    List<RoleResourceEntity> roleModuleList(@Param("entity") RoleResourceRefBo bo);

    /**
     * 根据URL返回匹配的权限名称
     *
     * @return
     */
    List<Map<String, String>> listRoleNamesWithPath();

    /**
     * 查询资源 并对该角色已拥有的资源打标记
     *
     * @param roleId
     * @return
     */
    @MapKey("MODULE_ID")
    Map<Long, List<ResourcesVo>> listGrantedResource(Long roleId);
}
