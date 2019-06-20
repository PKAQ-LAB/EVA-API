package io.nerv.security.filter;

import cn.hutool.core.util.StrUtil;
import io.nerv.exception.OathException;
import io.nerv.security.jwt.JwtConfig;
import io.nerv.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        var isvalid = false;
        var authToken = "";

        String authHeader = request.getHeader(jwtConfig.getHeader());
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(jwtConfig.getTokenHead())) {
            // The part after "Bearer "
            authToken = authHeader.substring(jwtConfig.getTokenHead().length());
            logger.debug(" ------------------ > Auth token is : " + authHeader);

            /**
             * token即将过期 刷新
             */
            try {
                isvalid = jwtUtil.valid(authToken);
                if (jwtUtil.isTokenExpiring(authToken)){
                    response.setHeader("Auth_Token",jwtUtil.refreshToken(authToken));
                }
            } catch (OathException e) {
                logger.warn("鉴权失败 Token已过期");
                //response.sendError(JwtErrorCode.TOKEN_EXPIRED, "您的登录已过期, 请重新登录.");
                //return;
            }
        } else {
            logger.warn("couldn't find bearer string, will ignore the header");
        }

        if (isvalid) {
            String uid = jwtUtil.getUid(authToken);

            logger.info("checking authentication ：" + uid);

            if (StrUtil.isNotBlank(uid) && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("security context was null, so authorizing user");

                // 从redis中 根据用户id获取用户权限列表
                UserDetails userDetails;
                try{
                    userDetails = this.userDetailsService.loadUserByUsername(uid);
                } catch(UsernameNotFoundException e){
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "您的登录已过期, 请重新登录.");
                    return;
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                logger.info("authenticated user " + uid + ", setting security context");

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}