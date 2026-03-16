package org.pkaq.core.auth.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.auth.user.entity.AuthUserEntity;
import org.springframework.stereotype.Repository;

/**
 * 认证用户Mapper
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface AuthUserMapper extends BaseMapper<AuthUserEntity> {
    /**
     * 根据账号/手机号/邮箱获取包含角色的用户
     *
     * @param user 查询条件
     * @return 用户信息
     */
    AuthUserEntity getUserWithRole(AuthUserEntity user);
}
