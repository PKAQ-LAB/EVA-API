package io.nerv.web.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.core.annotation.Ignore;
import io.nerv.web.sys.role.entity.RoleConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 角色参数权限关系mapper
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 22:08
 */
@Mapper
@Repository
@Ignore
public interface RoleConfigMapper extends BaseMapper<RoleConfigEntity> {

}
