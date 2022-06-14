package io.nerv.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 请求拦截，避免服务绕过接口被直接访问
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
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        System.out.println("destroy filter");
    }
}
 