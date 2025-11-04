package org.pkaq.core.util;

import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.pkaq.core.constant.CommonConstant;

import java.nio.charset.StandardCharsets;


public class HeaderUtil {
    /**
     * 获取header中的 userid
     *
     * @param request
     * @return
     */
    public static String getUserId(HttpServletRequest request) {
        return JakartaServletUtil.getHeader(request, CommonConstant.JWT_USER_ID_STR, StandardCharsets.UTF_8);
    }

    /**
     * 获取header中的 username
     *
     * @param request
     * @return
     */
    public static String getUserName(HttpServletRequest request) {
        return JakartaServletUtil.getHeader(request, CommonConstant.JWT_USER_NAME_STR, StandardCharsets.UTF_8);
    }

    /**
     * 获取header中的角色
     *
     * @param request
     * @return
     */
    public static String getRoles(HttpServletRequest request) {
        return JakartaServletUtil.getHeader(request, CommonConstant.JWT_USER_ROLES_STR, StandardCharsets.UTF_8);
    }

    public static String[] getRolesArray(HttpServletRequest request) {
        String roles = JakartaServletUtil.getHeader(request, CommonConstant.JWT_USER_ROLES_STR, StandardCharsets.UTF_8);
        return null == roles ? null : roles.split(",");
    }

}
