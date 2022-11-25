package io.nerv.core.web.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.servlet.ServletUtil;
import io.nerv.core.constant.CommonConstant;
import jakarta.servlet.http.HttpServletRequest;


public class HeaderUtil {
    /**
     * 获取header中的 userid
     *
     * @param request
     * @return
     */
    public static String getUserId(HttpServletRequest request) {
        return ServletUtil.getHeader(request, CommonConstant.JWT_USER_ID_STR, CharsetUtil.UTF_8);
    }

    /**
     * 获取header中的 username
     *
     * @param request
     * @return
     */
    public static String getUserName(HttpServletRequest request) {
        return ServletUtil.getHeader(request, CommonConstant.JWT_USER_NAME_STR, CharsetUtil.UTF_8);
    }

    /**
     * 获取header中的角色
     *
     * @param request
     * @return
     */
    public static String getRoles(HttpServletRequest request) {
        return ServletUtil.getHeader(request, CommonConstant.JWT_USER_ROLES_STR, CharsetUtil.UTF_8);
    }

    public static String[] getRolesArray(HttpServletRequest request) {
        String roles = ServletUtil.getHeader(request, CommonConstant.JWT_USER_ROLES_STR, CharsetUtil.UTF_8);
        return null == roles ? null : roles.split(",");
    }

}
