package tech.yunyue.auth.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tech.yunyue.auth.domain.JwtUserFactory;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.enums.DeleteEnumm;
import tech.yunyue.core.enums.LockEnumm;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.util.json.JsonUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class JDBCService {
    private final JdbcTemplate jdbcTemplate;

    /**
     * 根据账号@租户code、电话查询用户
     */
    public Map<String, Object> loadUserByUsername(String account) {
        try {
            String sql = "SELECT ID,ACCOUNT,TEL,PASSWORD,LOCKED,DEPT_ID,DEPT_NAME,NAME,NICK_NAME,TENANT_ID,TENANT_CODE,U_POST_ID,POST_NAME " +
                    "FROM SYS_USER_INFO SU  " +
                    "WHERE  DELETED is null AND (IF(ISNULL(SU.TENANT_ID), SU.ACCOUNT, CONCAT(ACCOUNT,'@',TENANT_CODE)) = ? OR SU.TEL = ?)";

            return this.jdbcTemplate.queryForMap(sql, account, account);
        } catch (EmptyResultDataAccessException e) {
            BizCodeEnum.ACCOUNT_NOT_EXIST.newException();
        }
        return Collections.emptyMap();
    }

    // 因为用户登录时存的是jsonStirng 所以这边也存string
    @Cacheable(cacheNames = CommonConstant.CACHE_USERDATA, key = "'" + CommonConstant.REDIS_USER_INFO_PREFIX_KEY + "'" + "+#userId")
    public String loadUserById(String userId) {
        return JsonUtil.toJson(JwtUserFactory.create(loadUserMapById(userId), Collections.emptyMap()));
    }

    /**
     * 根据id查询用户
     */
    private Map<String, Object> loadUserMapById(String userId) {
        try {
            String sql = "SELECT ID,ACCOUNT,TEL,PASSWORD,LOCKED,DEPT_ID,DEPT_NAME,NAME,NICK_NAME,TENANT_ID,TENANT_CODE,U_POST_ID,POST_NAME " +
                    "FROM SYS_USER_INFO SU " +
                    "WHERE  DELETED is null AND ID = ?";
            return this.jdbcTemplate.queryForMap(sql, userId);
        } catch (EmptyResultDataAccessException e) {
            BizCodeEnum.ACCOUNT_NOT_EXIST.newException();
        }
        return Collections.emptyMap();
    }

    /**
     * 查询用户拥有的角色 并转成ThreadUser.GrantedRoles对象 且缓存在redis中
     */
    @Cacheable(cacheNames = CommonConstant.CACHE_USERDATA, key = "'" + CommonConstant.REDIS_USER_ROLES_PREFIX_KEY + "'" + "+#userId")
    public Map<String, ThreadUser.GrantedRoles> getRoleById(String userId) {
        return JwtUserFactory.mapToGrantedAuthorities(getRoleMapById(userId));
    }

    /**
     * 查询用户拥有的角色 包括角色的数据权限类型
     */
    private List<Map<String, Object>> getRoleMapById(String userId) {
        try {
            String sql = "SELECT SR.ID, SR.NAME, SR.CODE, IFNULL(DATA_PERMISSION_TYPE, '0000') DATA_PERMISSION_TYPE, DATA_PERMISSION_DEPTID " +
                    "FROM SYS_ROLE SR, SYS_ROLE_USER SRU " +
                    "WHERE SR.ID = SRU.ROLE_ID  and SR.LOCKED != '0001' AND USER_ID=?";
            return this.jdbcTemplate.queryForList(sql, userId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    /**
     * 根据角色名称查询其拥有的资源路径
     */
    @Cacheable(cacheNames = CommonConstant.CACHE_AUTHDATA, key = "'" + CommonConstant.REDIS_ROLES_PERMISSION_PREFIX_KEY + "'" + "+#roleId")
    public List<String> listRoleNamesWithPath(String roleId) {
        try {
            String sql = "SELECT DISTINCT REPLACE(CONCAT(M.AUTHEN_PATH,'/',MR.RESOURCE_URL),'//','/') PATH " +
                    "FROM SYS_MODULE_RESOURCES MR, SYS_ROLE_MODULE RM, SYS_MODULE M, SYS_ROLE R " +
                    "WHERE RM.MODULE_ID = M.ID " +
                    "AND RM.ROLE_ID = R.ID " +
                    "AND M.ID = MR.MODULE_ID " +
                    "AND RM.RESOURCE_ID = MR.ID " +
                    "AND M.ISLEAF = '1' " +
                    "AND R.ID = ? " +
                    "AND CHAR_LENGTH(M.AUTHEN_PATH) != 0;";
            return this.jdbcTemplate.queryForList(sql, String.class, roleId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取系统的白名单资源
     */
    @Cacheable(cacheNames = CommonConstant.CACHE_SYSDATA, key = "'" + CommonConstant.REDIS_SYS_ALLOWED_RESOURCES_PREFIX_KEY + "'")
    public List<String> allowedResourcese() {
        try {
            String sql = "SELECT RESOURCE_URL FROM SYS_ALLOWED_RESOURCES";
            return this.jdbcTemplate.queryForList(sql, String.class);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    /**
     * 检查租户是否有效 启用/未删除/到期时间大于当前时间
     *
     * @param tenantId 租户id
     * @return
     */
    public boolean checkTenantEffective(String tenantId) {
        try {
            String sql = "select id from sys_tenant where id = ? and `STATUS` != ? and DELETED is null and now() < EXPIRATION_DATE;";
            var obj = this.jdbcTemplate.queryForObject(sql, String.class, tenantId, LockEnumm.LOCK.getCode());
            return Objects.nonNull(obj);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * 根据用户id检查用户所属租户是否有效 启用/未删除/到期时间大于当前时间
     *
     * @param userId 用户id
     * @return
     */
    public boolean checkUserTenantEffective(String userId) {
        try {
            String sql = "select u.id from sys_user_info u left JOIN sys_tenant t on u.TENANT_ID = t.id " +
                    "where  u.id = ? and  u.locked != ?  and u.DELETED is null " +
                    "and (u.TENANT_ID is null or (t.`STATUS` != ? and t.DELETED is null and now() < t.EXPIRATION_DATE))";
            var obj = this.jdbcTemplate.queryForObject(sql, String.class, userId, LockEnumm.LOCK.getCode(), LockEnumm.LOCK.getCode());
            return Objects.nonNull(obj);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
