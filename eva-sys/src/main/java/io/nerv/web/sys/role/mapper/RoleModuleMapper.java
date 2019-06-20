package io.nerv.web.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.nerv.web.sys.role.entity.RoleModuleEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色模块关系mapper
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 22:08
 */
@Mapper
@Repository
public interface RoleModuleMapper extends BaseMapper<RoleModuleEntity> {

    /**
     * 获取已选且是叶子节点的模块
     * @param roleModuleEntity
     * @return
     */
    List<RoleModuleEntity> roleModuleList(@Param("entity") RoleModuleEntity roleModuleEntity);
}
