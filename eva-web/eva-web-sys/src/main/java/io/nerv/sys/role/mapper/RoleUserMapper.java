package io.nerv.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.sys.role.entity.RoleUserEntity;
import io.nerv.core.annotation.Ignore;
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
