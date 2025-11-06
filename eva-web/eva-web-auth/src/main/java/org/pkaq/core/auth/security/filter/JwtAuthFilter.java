package org.pkaq.core.auth.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.CookieUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.core.util.TokenUtil;
import org.pkaq.core.util.json.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    @Qualifier("jwtUserDetailsService")
    private final UserDetailsService userDetailsService;

    private final JwtUtil jwtUtil;

    private final EvaConfig evaConfig;

    private final CacheTokenUtil cacheTokenUtil;

    private final TokenUtil tokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        var isvalid = false;
        var inCache = false;
        var cacheToken = evaConfig.getJwt().isPersistence();

        String authToken;
        try {
            authToken = tokenUtil.getToken(request);
        } catch (Exception e) {
            authToken = null;
            logger.warn(e);
        }

        if (StrUtils.isNotBlank(authToken)) {
            Long uid = jwtUtil.getUid(authToken);

            /**
             * token即将过期 刷新
             */
            try {
                // 验证token 是否合法
                isvalid = jwtUtil.valid(authToken);
                // 验证缓存中是否存在该token
                if (cacheToken) {
                    Object token = cacheTokenUtil.getToken(uid);

                    Map<String, Object> jsonObject = null;
                    if (null != token) {
                        jsonObject = JsonUtil.parse(String.valueOf(token), Map.class);
                    }

                    if (null != jsonObject && authToken.equals(jsonObject.get(CommonConstant.CACHE_TOKEN))) {
                        inCache = true;
                    } else {
                        logger.warn("鉴权失败 缓存中无法找到对应token");
                        // 清除cookie
                        this.clearCookie(response);
                        AuthCodes.LOGIN_EXPIRED.newException(AuthenticationException.class);
                    }
                }

                // token 即将过期 续命
                if (jwtUtil.isTokenExpiring(authToken)) {
                    String newToken = jwtUtil.refreshToken(authToken);
                    // 刷新缓存中的
                    // token放入缓存
                    if (cacheToken) {
                        cacheTokenUtil.saveToken(uid, cacheTokenUtil.buildCacheValue(request, uid, newToken));
                    }

                    // reponse请求头返回刷新后的token
                    response.setHeader(CommonConstant.ACCESS_TOKEN_KEY, newToken);
                    // 后台设置前台cookie值
                    CookieUtils.addCookie(response, CommonConstant.ACCESS_TOKEN_KEY,
                            newToken,
                            evaConfig.getCookie().getMaxAge(),
                            "/",
                            evaConfig.getCookie().getDomain());
                }
            } catch (AuthenticationException e) {
                logger.warn("鉴权失败 Token已过期", e);

                // 清除cookie
                // this.clearCookie(response);

                try (PrintWriter printWriter = response.getWriter()) {
                    response.setCharacterEncoding(StandardCharsets.UTF_8);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    printWriter.write(JsonUtil.toJson(
                            new Response().failure(AuthCodes.LOGIN_EXPIRED))
                    );
                    printWriter.flush();
                }
                return;
            }
        } else {
            logger.warn("couldn't find bearer string, will ignore the header");
        }

        if (isvalid && (inCache || !cacheToken)) {
            long uid = jwtUtil.getUid(authToken);
            String account = jwtUtil.getAccount(authToken);

            logger.info("checking authentication ：" + account);

            logger.info(SecurityContextHolder.getContext().getAuthentication());
//            if (StrUtils.isNotBlank(uid) && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (StrUtils.isNotBlank(account)) {
                logger.debug("org.pkaq.security context was null, so authorizing user");

                // 从redis中 根据用户id获取用户权限列表
                UserDetails userDetails;
                try {
                    userDetails = this.userDetailsService.loadUserByUsername(account);
                } catch (UsernameNotFoundException _) {
                    response.setCharacterEncoding(StandardCharsets.UTF_8);
                    response.setContentType("application/json");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "您的登录已过期, 请重新登录.");
                    return;
                }

                logger.info("authenticated user " + account + ", setting org.pkaq.security context");
                // 验证通过 将用户信息存入 threadlocal
                String[] roles = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toArray(String[]::new);

                ThreadUser currentUser = new ThreadUser().setUserId(uid)
                        .setName(account)
                        .setRoles(roles);

                ThreadUserHelper.runWithUser(currentUser, () -> {
                    try {
                        chain.doFilter(request, response);
                    } catch (IOException | ServletException e) {
                        throw new BizException(CommonCodes.SERVER_ERROR);
                    }
                });

//                将用户信息设置到security 上下文中
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * 清除cookie
     *
     * @param response
     */
    public void clearCookie(HttpServletResponse response) {
        CookieUtils.addCookie(response,
                CommonConstant.ACCESS_TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        CookieUtils.addCookie(response,
                CommonConstant.REFRESH_TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        CookieUtils.addCookie(response,
                CommonConstant.USER_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

    }
}