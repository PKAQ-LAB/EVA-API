package org.pkaq.core.auth.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.auth.role.entity.AuthRoleEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 认证角色Mapper
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface AuthRoleMapper extends BaseMapper<AuthRoleEntity> {
    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<AuthRoleEntity> selectByUserId(String userId);
}
