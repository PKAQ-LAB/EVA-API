package org.pkaq.core.auth.adapter.mybatis.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.pkaq.core.auth.adapter.mybatis.user.entity.AuthUserEntity;
import org.springframework.stereotype.Repository;

/**
 * 认证用户Mapper
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Mapper
@Repository
public interface AuthUserMapper extends BaseMapper<AuthUserEntity> {
    /**
     * 获取不包含角色集合的登录账号投影。
     *
     * @param user 查询条件
     * @return 账号信息
     */
    AuthUserEntity getUserAccount(AuthUserEntity user);

    /**
     * 获取租户内不包含角色集合的登录账号投影。
     *
     * @param user 查询条件
     * @return 账号信息
     */
    AuthUserEntity getTenantUserAccount(AuthUserEntity user);

    /**
     * 获取用户的权限版本号
     *
     * @param userId 用户ID
     * @return 权限版本号
     */
    @Select("SELECT PERM_VER FROM SYS_ACCOUNT WHERE ID = #{userId} AND DELETED = 0")
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
                su.NICK_NAME,
                0 AS TENANT_ID
            FROM SYS_ACCOUNT su
            WHERE su.ID = #{userId}
                AND su.DELETED = 0
            """)
    AuthUserEntity getAuthState(Long userId);

    /**
     * 获取当前租户schema中的账号安全状态，不查询管理资料。
     * @param userId 账号编号
     * @return 安全状态
     */
    @Select("""
            SELECT ID, FROZEN, PERM_VER, NICK_NAME
            FROM SYS_ACCOUNT
            WHERE ID = #{userId} AND COALESCE(DELETED, 0) = 0
            """)
    AuthUserEntity getTenantAuthState(Long userId);

    /**
     * 自增用户的权限版本号
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE SYS_ACCOUNT SET PERM_VER = COALESCE(PERM_VER, 0) + 1 WHERE ID = #{userId} AND DELETED = 0")
    int incrementPermVer(Long userId);
}
