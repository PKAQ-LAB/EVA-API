package io.nerv.core.web.filter;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.threaduser.ThreadUser;
import io.nerv.core.threaduser.ThreadUserHelper;
import io.nerv.core.web.util.HeaderUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 请求拦截，避免服务绕过接口被直接访问
 *
 * @author PKAQ
 */
@Slf4j
@WebFilter(filterName = "BaseFilter", urlPatterns = {"/*"})
public class RequestFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        log.info("init filter");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("进入 服务请求拦截 过滤器========");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String gateway = request.getHeader("gatewayKey");
        if (StrUtil.isBlank(gateway) || !gateway.equals("key")) {
            log.info("非法请求");
            return;
        }

        // 获取用户信息设置到threadlocal中
        var tu = new ThreadUser();
        tu.setUserId(HeaderUtil.getUserId(request))
                .setUserName(HeaderUtil.getUserName(request))
                .setRoles(HeaderUtil.getRolesArray(request));
        ThreadUserHelper.setCurrentUser(tu);

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        System.out.println("destroy filter");
    }
}
 