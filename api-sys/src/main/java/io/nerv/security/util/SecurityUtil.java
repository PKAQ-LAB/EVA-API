package io.nerv.security.util;

import io.nerv.security.domain.JwtUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Spring security 工具类
 */
@Component
public class SecurityUtil {

    /**
     *  获取权限对象
     * @return
     */
    public Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取用户名
     * @param request
     * @return
     */
    public String getUser(HttpServletRequest request) {
        return request.getUserPrincipal().getName();
    }

    /**
     * 获取当前登录用户对象
     * @return
     */
    public JwtUser getJwtUser(){
        return (JwtUser)this.getAuthentication().getPrincipal();
    }

    /**
     * 获取用户ID
     * @return
     */
    public String getJwtUserId(){
        return this.getJwtUser().getId();
    }

    /**
     * 获取用户名
     * @return
     */
    public String getJwtUserName(){
        return this.getJwtUser().getAccount();
    }
}
