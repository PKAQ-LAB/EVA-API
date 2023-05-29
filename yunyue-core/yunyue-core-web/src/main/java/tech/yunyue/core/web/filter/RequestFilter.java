package tech.yunyue.core.web.filter;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import tech.yunyue.core.web.util.RequestUtil;

import java.io.IOException;
import java.util.Objects;

/**
 * 请求拦截
 *
 * @author PKAQ
 */
@Slf4j
@Component
public class RequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 用户信息为null  新建一个匿名用户
        var tu = ThreadUserHelper.getCurrentUser();
        if(Objects.isNull(tu)){
            tu = new ThreadUser();
            tu.setModuleId(RequestUtil.getModuleId(request));
            ThreadUserHelper.setCurrentUser(tu);
        }
        filterChain.doFilter(request, response);
    }

}
