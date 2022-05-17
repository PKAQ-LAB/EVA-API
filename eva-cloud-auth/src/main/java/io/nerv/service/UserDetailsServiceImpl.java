package io.nerv.service;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.enums.LockEnumm;
import io.nerv.domain.SecurityUserDetails;
import io.nerv.exception.OathException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户管理业务类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    /**
     * 登录判断逻辑
     * 提供一种从用户名可以查到用户并返回的方法
     * @param account the username identifying the user whose data is required.
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {

        if (StrUtil.isBlank(account)){
            throw new OathException(BizCodeEnum.PERMISSION_DENY);
        }

        String sql =  "SELECT * FROM " +
                        "sys_user_info su " +
                        "LEFT JOIN sys_role_user sr " +
                        "ON sr.user_id = su.id " +
                        "AND " +
                            "sr.ROLE_ID='0000' " +
                        "where " +
                            "deleted = '0000' " +
                            "and (su.account = ? or su.tel = ? or su.email = ? )";

        try {
            Map<String, Object> mapUser = this.jdbcTemplate.queryForMap(sql, account, account, account);

            if (null == mapUser ){
                throw new OathException(BizCodeEnum.ACCOUNT_NOT_EXIST);
            }

            var roleId =  StrUtil.toStringOrNull(mapUser.get("ROLE_ID"));
            var nonLocked =  LockEnumm.UNLOCK.getIndex().equals(mapUser.get("LOCKED"));
            // 没有登录权限
            if (StrUtil.isBlank(roleId)) {
                throw new OathException(BizCodeEnum.PERMISSION_DENY);
            }
            //用户已锁定
            if (!nonLocked){
                throw new OathException(BizCodeEnum.ACCOUNT_LOCKED);
            }

            SecurityUserDetails securityUserDetails = new SecurityUserDetails(StrUtil.toStringOrNull(mapUser.get("ID")),
                    StrUtil.toStringOrNull(mapUser.get("ACCOUNT")),
                    StrUtil.toStringOrNull(mapUser.get("PASSWORD")),
                    roleId,
                    true,
                    null);

            return securityUserDetails;
        }catch (EmptyResultDataAccessException e){
            throw new OathException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
    }

}
