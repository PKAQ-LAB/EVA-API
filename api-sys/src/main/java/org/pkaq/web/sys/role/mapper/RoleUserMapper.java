package org.pkaq.web.sys.role.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.web.sys.role.entity.RoleModuleEntity;
import org.springframework.stereotype.Repository;

/**
 * 角色用户关系mapper
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 22:08
 */
@Mapper
@Repository
public interface RoleUserMapper extends BaseMapper<RoleModuleEntity> {
}
