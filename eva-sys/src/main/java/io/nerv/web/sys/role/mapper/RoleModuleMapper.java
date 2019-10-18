package io.nerv.web.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.core.annotation.Ignore;
import org.apache.ibatis.annotations.Mapper;
import io.nerv.web.sys.role.entity.RoleModuleEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 角色模块关系mapper
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 22:08
 */
@Mapper
@Repository
@Ignore
public interface RoleModuleMapper extends BaseMapper<RoleModuleEntity> {

    /**
     * 获取已选且是叶子节点的模块
     * @param roleModuleEntity
     * @return
     */
    List<RoleModuleEntity> roleModuleList(@Param("entity") RoleModuleEntity roleModuleEntity);

    /**
     * 根据URL返回匹配的权限名称
     * @return
     */
    List<Map<String, String>> listRoleNamesWithPath();
}
