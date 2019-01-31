package io.nerv.web.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.nerv.web.sys.role.entity.RoleModuleEntity;
import org.springframework.stereotype.Repository;

/**
 * 角色模块关系mapper
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 22:08
 */
@Mapper
@Repository
public interface RoleModuleMapper extends BaseMapper<RoleModuleEntity> {
}
