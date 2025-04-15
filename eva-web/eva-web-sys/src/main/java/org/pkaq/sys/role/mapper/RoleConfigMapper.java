package org.pkaq.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.role.entity.RoleConfigEntity;
import org.springframework.stereotype.Repository;

/**
 * 角色参数权限关系mapper
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
@Ignore
public interface RoleConfigMapper extends BaseMapper<RoleConfigEntity> {

}
