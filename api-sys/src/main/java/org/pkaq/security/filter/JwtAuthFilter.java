package org.pkaq.security.filter;

import org.pkaq.security.jwt.JwtConstant;
import org.pkaq.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

@SuppressWarnings("SpringJavaAutowiringInspection")
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader(JwtConstant.JWT_HEADER);
        if (authHeader != null && authHeader.startsWith(JwtConstant.JWT_TOKENHEAD)) {
            // The part after "Bearer "
            final String authToken = authHeader.substring(JwtConstant.JWT_TOKENHEAD.length());
            String account = jwtUtil.getUid(authToken);

            logger.info("checking authentication " + account);

            if (account != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 根据用户id获取用户权限列表
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(account);

                if (jwtUtil.valid(authToken)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    logger.info("authenticated user " + account + ", setting security context");
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        chain.doFilter(request, response);
    }
}