package io.nerv.security.filter;

import cn.hutool.core.util.StrUtil;
import io.nerv.security.exception.OathException;
import io.nerv.security.jwt.JwtConfig;
import io.nerv.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    @Qualifier("jwtUserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        //筛选登录接口和文档库的接口
        if (uri.contains("auth/login") || uri.contains("/vein/webjars") || uri.equals("/vein/") || uri.contains("/vein/doc.html") || uri.contains("/swagger")){
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(jwtConfig.getHeader());
        if (null != authHeader && authHeader.startsWith(jwtConfig.getTokenHead())) {
            // The part after "Bearer "
            final String authToken = authHeader.substring(jwtConfig.getTokenHead().length());

            logger.debug(" ------------------ > Auth token is : " + authHeader);
            boolean isvalid = false;
            try {
                isvalid = jwtUtil.valid(authToken);
            } catch (OathException e) {
                logger.warn("Token已过期");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "您的登录已过期, 请重新登录.");
                return;
            }

            if (isvalid) {
                String account = jwtUtil.getUid(authToken);

                logger.info("checking authentication ：" + account);

                if (StrUtil.isNotBlank(account) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 从redis中 根据用户id获取用户权限列表
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(account);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    logger.info("authenticated user " + account + ", setting security context");
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }else{
            //没有authHeader直接拦截
            return;
        }
        chain.doFilter(request, response);
    }
}