package org.pkaq.core.auth.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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

    /**
     * 获取用户的权限版本号
     *
     * @param userId 用户ID
     * @return 权限版本号
     */
    @Select("SELECT PERM_VER FROM SYS_USER WHERE ID = #{userId} AND DELETED = 0")
    Long getPermVer(Long userId);

    /**
     * 获取认证期用户状态。
     *
     * @param userId 用户ID
     * @return 用户认证状态
     */
    @Select("""
            SELECT
                su.ID,
                su.FROZEN,
                su.PERM_VER,
                st.FROZEN AS TENANT_FROZEN,
                st.EXPIRATION_DATE AS TENANT_EXPIRATION_DATE
            FROM SYS_USER su
                LEFT JOIN SYS_TENANT st ON su.TENANT_ID = st.ID AND (st.DELETED = 0 OR st.DELETED IS NULL)
            WHERE su.ID = #{userId}
                AND su.DELETED = 0
            """)
    AuthUserEntity getAuthState(Long userId);

    /**
     * 自增用户的权限版本号
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE SYS_USER SET PERM_VER = COALESCE(PERM_VER, 0) + 1 WHERE ID = #{userId} AND DELETED = 0")
    int incrementPermVer(Long userId);
}
