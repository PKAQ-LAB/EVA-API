package org.pkaq.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色用户关系mapper
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 22:08
 */
@Mapper
@Repository
public interface RoleUserMapper extends BaseMapper<RoleUserEntity> {

}
