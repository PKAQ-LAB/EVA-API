package org.pkaq.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色用户关系mapper
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
@Ignore
public interface RoleUserMapper extends BaseMapper<RoleUserEntity> {

    /**
     * 根据用户id获取角色id列表
     * @param id
     * @return
     */
    List<String> selectRoleIds(String id);
}
