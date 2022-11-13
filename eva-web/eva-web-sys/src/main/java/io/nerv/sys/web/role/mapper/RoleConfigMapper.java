package io.nerv.sys.web.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.common.annotation.Ignore;
import io.nerv.sys.web.role.entity.RoleConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 角色参数权限关系mapper
 * @author: S.PKAQ
 */
@Mapper
@Repository
@Ignore
public interface RoleConfigMapper extends BaseMapper<RoleConfigEntity> {

}
