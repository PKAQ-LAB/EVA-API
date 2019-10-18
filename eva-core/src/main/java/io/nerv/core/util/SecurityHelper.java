package io.nerv.core.util;

import io.nerv.core.security.domain.JwtUserDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Spring security 工具类
 */
@Slf4j
@Component
public class SecurityHelper {
    /**
     * 判断是否是管理员
     * @return
     */
    public boolean isAdmin(){
        return this .getAuthentication()
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(str -> str.equals("ROLE_ADMIN"));
    }
    /**
     * 获取当前用户的权限数组
     * @return
     */
    public String[] getRoleNames(){
        return this .getAuthentication()
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray(String[]::new);
    }
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
    public JwtUserDetail getJwtUser() throws ClassCastException{
        return (JwtUserDetail)this.getAuthentication().getPrincipal();
    }

    /**
     * 获取用户ID
     * @return
     */
    public String getJwtUserId(){
        String userId = "Anonymous";
        try{
            userId = this.getJwtUser().getId();
        }catch (Exception e){
            e.printStackTrace();
            log.error("获取用户ID错误： " + e.getMessage());
        } finally {
            return userId;
        }
    }

    /**
     * 获取用户名
     * @return
     */
    public String getJwtUserName(){
        String userName = "Anonymous";
        try{
            userName = this.getJwtUser().getAccount();
        }catch (Exception e){
            e.printStackTrace();
            log.error("获取用户名错误： " + e.getMessage());
        } finally {
            return userName;
        }
    }
}
