package io.nerv.common.filter;

import io.nerv.common.threaduser.ThreadUser;
import io.nerv.common.threaduser.ThreadUserHelper;
import io.nerv.common.util.HeaderUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 请求拦截，避免服务绕过接口被直接访问
 * @author PKAQ
 */
@Slf4j
@WebFilter(filterName = "BaseFilter",urlPatterns = {"/*"})
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
        if (gateway == null || gateway.equals("") || !gateway.equals("key")) {
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
 