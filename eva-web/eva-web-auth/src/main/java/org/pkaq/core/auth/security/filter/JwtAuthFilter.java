package org.pkaq.core.auth.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.auth.rbac.service.RoleResourceCacheService;
import org.pkaq.core.auth.user.entity.AuthUserEntity;
import org.pkaq.core.auth.user.service.AuthUserService;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.ArrayUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.core.util.json.JsonUtil;
import org.pkaq.web.core.utils.CookieUtils;
import org.pkaq.web.core.utils.ResponseUtil;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT认证过滤器
 * 从JWT中解析用户信息、校验permVer、检查RBAC资源权限
 *
 * @author PKAQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(200)
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final EvaConfig evaConfig;
    private final CacheTokenUtil cacheTokenUtil;
    private final TokenUtils tokenUtil;
    private final AuthUserService authUserService;
    private final RoleResourceCacheService roleResourceCacheService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String requestPath = resolveRequestPath(request);
        if (!evaConfig.getAuth().matchJwtPath(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        boolean isvalid = false;
        boolean inCache = false;
        boolean cacheToken = evaConfig.getJwt().isPersistence();

        String authToken;
        try {
            authToken = tokenUtil.getToken(request);
        } catch (Exception e) {
            authToken = null;
            log.warn(e.getLocalizedMessage());
        }

        if (StrUtils.isNotBlank(authToken)) {
            try {
                // 验证token是否合法
                isvalid = jwtUtil.valid(authToken) && jwtUtil.isAccessToken(authToken);
                Long uid = isvalid ? jwtUtil.getUid(authToken) : null;
                // 验证缓存中是否存在该token
                if (isvalid && cacheToken) {
                    Object token = cacheTokenUtil.getToken(uid);

                    Map<String, Object> jsonObject = null;
                    if (null != token) {
                        jsonObject = JsonUtil.parse(String.valueOf(token), Map.class);
                    }

                    if (null != jsonObject && authToken.equals(jsonObject.get(CommonConstant.CACHE_TOKEN))) {
                        inCache = true;
                    } else {
                        log.warn("鉴权失败 缓存中无法找到对应token");
                        this.clearCookie(response);
                        ResponseUtil.write(response, Response.failure(AuthCodes.LOGIN_EXPIRED));
                        return;
                    }
                }

                // token即将过期 续命
                if (isvalid && jwtUtil.isTokenExpiring(authToken)) {
                    String newToken = jwtUtil.refreshToken(authToken);
                    if (cacheToken) {
                        cacheTokenUtil.saveToken(uid, cacheTokenUtil.buildCacheValue(request, uid, newToken));
                    }
                    response.setHeader(CommonConstant.ACCESS_TOKEN_KEY, newToken);
                    CookieUtils.addCookie(response, CommonConstant.ACCESS_TOKEN_KEY,
                            newToken, evaConfig.getCookie().getMaxAge(), "/", evaConfig.getCookie().getDomain());
                }
            } catch (AuthenticationException e) {
                log.warn("鉴权失败 Token已过期", e);
                ResponseUtil.write(response, Response.failure(AuthCodes.LOGIN_EXPIRED));
                return;
            }
        } else {
            log.warn("couldn't find bearer string, will ignore the header");
        }

        if (isvalid && (inCache || !cacheToken)) {
            long uid = jwtUtil.getUid(authToken);
            String account = jwtUtil.getAccount(authToken);

            if (StrUtils.isNotBlank(account)) {
                // 从JWT解析角色和权限版本号
                List<Long> roleIds = jwtUtil.getRoles(authToken);
                long tokenPermVer = jwtUtil.getPermVer(authToken);

                AuthUserEntity authState = authUserService.getAuthState(uid);
                if (authState == null) {
                    this.clearCookie(response);
                    ResponseUtil.write(response, Response.failure(AuthCodes.LOGIN_EXPIRED));
                    return;
                }
                if (FrozenEnumm.FROZEN == authState.getFrozen()) {
                    this.clearCookie(response);
                    ResponseUtil.write(response, Response.failure(AuthCodes.ACCOUNT_LOCKED));
                    return;
                }
                if (evaConfig.isSaasMode() && isTenantUnavailable(authState)) {
                    this.clearCookie(response);
                    ResponseUtil.write(response, Response.failure(AuthCodes.LOGIN_TENANT_AUTH_EXPIRED));
                    return;
                }

                // 校验权限版本号：不一致即要求重新登录
                long dbPermVer = authState.getPermVer() == null ? 0L : authState.getPermVer();
                if (dbPermVer != tokenPermVer) {
                    log.warn("用户 {} 权限已变更, tokenPermVer={}, dbPermVer={}", account, tokenPermVer, dbPermVer);
                    this.clearCookie(response);
                    ResponseUtil.write(response, Response.failure(AuthCodes.PERM_VER_CHANGED));
                    return;
                }

                // RBAC资源权限校验（permit路径跳过校验）
                if (evaConfig.getResourcePermission().isEnable() && !isPermitPath(requestPath)) {
                    String httpMethod = request.getMethod();
                    if (roleIds == null || roleIds.isEmpty()) {
                        log.warn("用户 {} 无角色, 禁止访问: {} {}", account, httpMethod, requestPath);
                        ResponseUtil.write(response, Response.failure(AuthCodes.RESOURCE_FORBIDDEN));
                        return;
                    }

                    if (!roleResourceCacheService.hasPermission(roleIds, httpMethod, requestPath)) {
                        log.warn("用户 {} 无权访问: {} {}", account, httpMethod, requestPath);
                        ResponseUtil.write(response, Response.failure(AuthCodes.RESOURCE_FORBIDDEN));
                        return;
                    }
                }

                // 从JWT构建ThreadUser
                String[] roleNames = roleIds == null
                        ? new String[0]
                        : roleIds.stream().map(String::valueOf).toArray(String[]::new);
                ThreadUser currentUser = new ThreadUser()
                        .setUserId(uid)
                        .setName(account)
                        .setTenantId(authState.getTenantId() == null ? 0L : authState.getTenantId())
                        .setDeptId(authState.getDeptId() == null ? 0L : authState.getDeptId())
                        .setRoles(roleNames);

                // 设置SecurityContext
                List<SimpleGrantedAuthority> authorities = roleIds == null || roleIds.isEmpty()
                        ? Collections.emptyList()
                        : roleIds.stream().map(id -> new SimpleGrantedAuthority("ROLE_" + id)).toList();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(account, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                ThreadUserHelper.runWithUser(currentUser, () -> {
                    try {
                        chain.doFilter(request, response);
                    } catch (IOException | ServletException e) {
                        throw new BizException(CommonCodes.SERVER_ERROR);
                    }
                });
            }
        } else {
            // 没有有效的token, 让Spring Security后续过滤器处理认证
            chain.doFilter(request, response);
        }
    }

    /**
     * 判断是否为无需资源鉴权的路径
     */
    private boolean isPermitPath(String path) {
        String[] permitPaths = evaConfig.getAuth().getPermit();
        if (ArrayUtils.isEmpty(permitPaths)) {
            return false;
        }
        for (String pattern : permitPaths) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断租户是否不可用。
     */
    private boolean isTenantUnavailable(AuthUserEntity authState) {
        if (FrozenEnumm.FROZEN == authState.getTenantFrozen()) {
            return true;
        }
        return authState.getTenantExpirationDate() != null
                && !authState.getTenantExpirationDate().after(new Date());
    }

    /**
     * 获取去除 context-path 后的请求路径。
     */
    private String resolveRequestPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (StrUtils.isNotBlank(servletPath)) {
            return servletPath;
        }
        return request.getRequestURI();
    }

    /**
     * 清除cookie
     */
    public void clearCookie(HttpServletResponse response) {
        CookieUtils.addCookie(response, CommonConstant.ACCESS_TOKEN_KEY,
                null, 0, "/", evaConfig.getCookie().getDomain());
        CookieUtils.addCookie(response, CommonConstant.REFRESH_TOKEN_KEY,
                null, 0, "/", evaConfig.getCookie().getDomain());
        CookieUtils.addCookie(response, CommonConstant.USER_KEY, null, 0, "/", evaConfig.getCookie().getDomain());
    }
}
