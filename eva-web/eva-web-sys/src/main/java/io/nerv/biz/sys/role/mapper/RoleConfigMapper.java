package io.nerv.biz.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.biz.sys.role.entity.RoleConfigEntity;
import io.nerv.core.annotation.Ignore;
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
