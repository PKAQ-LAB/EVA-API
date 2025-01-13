package org.pkaq.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 角色用户关系mapper
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
@Ignore
public interface RoleUserMapper extends BaseMapper<RoleUserEntity> {

}
