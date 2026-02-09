package org.pkaq.web.core.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Objects;

public class CookieUtils {

    /**
     * 添加或更新 Cookie
     *
     * @param response 响应对象
     * @param name     Cookie 名称
     * @param value    Cookie 值（传 null 表示删除）
     * @param maxAge   存活时间（秒），0 表示删除，负数表示会话级 Cookie
     * @param path     Cookie 作用路径（推荐为 "/"）
     * @param domain   Cookie 作用域（如 "example.com"，可为 null）
     */
    public static void addCookie(HttpServletResponse response,
                                 String name,
                                 String value,
                                 int maxAge,
                                 String path,
                                 String domain) {
        Objects.requireNonNull(response, "HttpServletResponse must not be null");
        Objects.requireNonNull(name, "Cookie name must not be null");

        Cookie cookie = new Cookie(name, value);

        // 设置有效期
        cookie.setMaxAge(maxAge);

        // 规范化路径（推荐 "/"）
        if (path == null || path.isBlank()) {
            cookie.setPath("/");
        } else {
            cookie.setPath(path);
        }

        // 仅当域名不为空时才设置，否则可能导致跨域写入失败
        if (domain != null && !domain.isBlank()) {
            cookie.setDomain(domain);
        }

        // 安全策略：HttpOnly 默认启用
        cookie.setHttpOnly(true);

        // 若启用 HTTPS，可改为 true
        cookie.setSecure(false);

        response.addCookie(cookie);
    }

    /**
     * 删除 Cookie（封装版）
     */
    public static void clearCookie(HttpServletResponse response,
                                   String name,
                                   String path,
                                   String domain) {
        addCookie(response, name, null, 0, path, domain);
    }

    /**
     * 从cookie中获取值
     */
    public static String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
