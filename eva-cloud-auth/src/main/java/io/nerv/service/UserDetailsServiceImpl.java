package io.nerv.service;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.enums.LockEnumm;
import io.nerv.domain.SecurityUserDetails;
import io.nerv.exception.OathException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // 查询用户
        String userSQL =  "SELECT ID,ACCOUNT,PASSWORD,LOCKED FROM " +
                        "sys_user_info su " +
                        "where " +
                            "deleted = '0000' " +
                            "and (su.account = ? or su.tel = ? or su.email = ? )";
        // 查询用户拥有的权限
        String roleSQL = "SELECT SR.CODE,SRU.USER_ID " +
                            "FROM SYS_ROLE SR, SYS_ROLE_USER SRU " +
                         "WHERE SR.ID = SRU.ROLE_ID AND USER_ID=?";

        try {
            Map<String, Object> mapUser = this.jdbcTemplate.queryForMap(userSQL, account, account, account);

            if (null == mapUser ){
                throw new OathException(BizCodeEnum.ACCOUNT_NOT_EXIST);
            }

            var nonLocked =  LockEnumm.UNLOCK.getIndex().equals(mapUser.get("LOCKED"));
            //用户已锁定
            if (!nonLocked){
                throw new OathException(BizCodeEnum.ACCOUNT_LOCKED);
            }

            // 查询用户拥有的角色
            List<Map<String, Object>> roleList = this.jdbcTemplate.queryForList(roleSQL, mapUser.get("ID"));

            var roles = roleList.stream()
                    .map(item -> new SimpleGrantedAuthority(StrUtil.toString(item.get("CODE"))))
                    .collect(Collectors.toList());


            SecurityUserDetails securityUserDetails = new SecurityUserDetails(StrUtil.toStringOrNull(mapUser.get("ID")),
                    StrUtil.toStringOrNull(mapUser.get("ACCOUNT")),
                    StrUtil.toStringOrNull(mapUser.get("PASSWORD")),
                    true,
                    roles);

            return securityUserDetails;
        }catch (EmptyResultDataAccessException e){
            throw new OathException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
    }

}
