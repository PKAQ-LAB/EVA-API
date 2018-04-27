package org.pkaq.sys.role.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.sys.role.entity.RoleEntity;
import org.springframework.stereotype.Repository;

/**
 * 角色管理mapper
 * @author: S.PKAQ
 * @Datetime: 2018/4/13 7:25
 */
@Mapper
@Repository
public interface RoleMapper extends BaseMapper<RoleEntity>{
}
