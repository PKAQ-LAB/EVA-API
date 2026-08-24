package org.pkaq.web.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.properties.Cloud;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求拦截，避免服务绕过接口被直接访问
 *
 * @author PKAQ
 */
@Slf4j
@Component
public class RequestFilter extends OncePerRequestFilter {
    private final EvaConfig evaConfig;

    public RequestFilter(EvaConfig evaConfig) {
        this.evaConfig = evaConfig;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        Cloud cloud = this.evaConfig.getCloud();
        if (cloud.isEnable() && !cloud.getRequestValue().equals(request.getHeader(cloud.getRequestHeader()))) {
            log.warn("拒绝未经过网关转发的请求: method={}, uri={}", request.getMethod(), request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }

}
