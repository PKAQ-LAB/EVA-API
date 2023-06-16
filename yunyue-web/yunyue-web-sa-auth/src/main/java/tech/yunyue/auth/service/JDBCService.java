package tech.yunyue.auth.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tech.yunyue.core.enums.BizCodeEnum;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class JDBCService {
    private final JdbcTemplate jdbcTemplate;

    /**
     * 根据账号、电话、email查询用户
     */
    public Map<String, Object> loadUserByUsername(String account){
        try {
            String sql = "SELECT ID,ACCOUNT,PASSWORD,LOCKED,DEPT_ID,DEPT_NAME,NAME,NICK_NAME,TENANT_ID,COMPANY_TENANT_ID,POST_ID,POST_NAME " +
                    "FROM SYS_USER_INFO SU " +
                    "WHERE  DELETED = '0000' AND (SU.ACCOUNT = ? OR SU.TEL = ? OR SU.EMAIL = ? )";
            return this.jdbcTemplate.queryForMap(sql, account, account, account);
        }catch (EmptyResultDataAccessException e){
            BizCodeEnum.ACCOUNT_NOT_EXIST.newException();
        }
        return Collections.emptyMap();
    }

    /**
     * 根据id查询用户
     */
    public Map<String, Object> loadUserById(String userId){
        try {
            String sql = "SELECT ID,ACCOUNT,PASSWORD,LOCKED,DEPT_ID,DEPT_NAME,NAME,NICK_NAME,TENANT_ID,COMPANY_TENANT_ID,POST_ID,POST_NAME " +
                    "FROM SYS_USER_INFO SU " +
                    "WHERE  DELETED = '0000' AND ID = ?";
            return this.jdbcTemplate.queryForMap(sql, userId);
        }catch (EmptyResultDataAccessException e){
            BizCodeEnum.ACCOUNT_NOT_EXIST.newException();
        }
        return Collections.emptyMap();
    }
    /**
     * 查询用户拥有的角色 包括角色的数据权限类型
     */
    public List<Map<String, Object>> getRoleById(String userId){
        try{
            String sql = "SELECT SR.NAME, SR.CODE, IFNULL(DATA_PERMISSION_TYPE, '0000') DATA_PERMISSION_TYPE, DATA_PERMISSION_DEPTID " +
                    "FROM SYS_ROLE SR, SYS_ROLE_USER SRU " +
                    "WHERE SR.ID = SRU.ROLE_ID  and SR.LOCKED != '0001' AND USER_ID=?";
            return this.jdbcTemplate.queryForList(sql, userId );
        }catch (EmptyResultDataAccessException e){
            return Collections.emptyList();
        }
    }
    /**
     * 查询系统全部的角色与其拥有的资源路径
     */
    public List<Map<String, Object>>  listRoleNamesWithPath(){
        try{
            String sql="SELECT DISTINCT M.PATH, R.CODE, MR.RESOURCE_URL "+
                    "FROM SYS_MODULE_RESOURCES MR, SYS_ROLE_MODULE RM, SYS_MODULE M, SYS_ROLE R " +
                    "WHERE RM.MODULE_ID = M.ID AND RM.ROLE_ID = R.ID AND M.ID = MR.MODULE_ID AND RM.RESOURCE_ID = MR.ID AND M.ISLEAF = '1'";
            return this.jdbcTemplate.queryForList(sql);
        }catch (EmptyResultDataAccessException e){
            return Collections.emptyList();
        }
    }
}
