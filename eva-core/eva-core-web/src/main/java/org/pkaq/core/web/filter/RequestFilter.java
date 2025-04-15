package org.pkaq.core.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.web.util.RequestUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * 请求拦截，避免服务绕过接口被直接访问
 *
 * @author PKAQ
 */
@Slf4j
@Component
public class RequestFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // 用户信息为null  新建一个匿名用户
        var tu = ThreadUserHelper.getCurrentUser();
        if (Objects.isNull(tu)) {
            tu = new ThreadUser();
            tu.setModuleId(RequestUtil.getModuleId(request));
            tu.setModuleCode(RequestUtil.getModuleCode(request));
            ThreadUserHelper.setCurrentUser(tu);
        }

        filterChain.doFilter(request, response);
    }

}
 