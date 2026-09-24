package org.pkaq.core.auth.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.auth.rbac.service.RoleResourceCacheService;
import org.pkaq.core.auth.role.entity.AuthRoleEntity;
import org.pkaq.core.auth.user.entity.AuthUserEntity;
import org.pkaq.core.auth.user.service.AuthUserService;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.auth.tenant.TenantAuthRoutingService;
import org.pkaq.core.auth.tenant.TenantLoginIdentity;
import org.pkaq.core.auth.tenant.TenantLoginResolver;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.constant.PlatformCapabilities;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.ArrayUtils;
import org.pkaq.core.util.StrUtils;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final TenantLoginResolver tenantLoginResolver;
    private final TenantAuthRoutingService tenantAuthRoutingService;

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
                long tenantId = isvalid ? jwtUtil.getTenantId(authToken) : 0L;
                String sessionId = isvalid ? jwtUtil.getSessionId(authToken) : null;
                if (isvalid && evaConfig.getTenant().isSchemaMode()) {
                    TenantLoginIdentity identity = tenantLoginResolver.resolveId(tenantId);
                    isvalid = identity.schemaGeneration() == jwtUtil.getSchemaGeneration(authToken);
                }
                // 验证缓存中是否存在该token
                if (isvalid && cacheToken) {
                    if (StrUtils.isNotBlank(sessionId)
                            && cacheTokenUtil.matchesAccessToken(tenantId, uid, sessionId, authToken)) {
                        inCache = true;
                        cacheTokenUtil.touchSession(tenantId, uid, sessionId);
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
                    if (cacheToken && !cacheTokenUtil.replaceAccessToken(
                            tenantId, uid, sessionId, request, newToken)) {
                        log.warn("鉴权续期失败 服务端会话已不存在, tenantId={}, userId={}, sessionId={}",
                                tenantId, uid, sessionId);
                        this.clearCookie(response);
                        ResponseUtil.write(response, Response.failure(AuthCodes.LOGIN_EXPIRED));
                        return;
                    }
                    response.setHeader(CommonConstant.ACCESS_TOKEN_KEY, newToken);
                    CookieUtils.addCookie(response, CommonConstant.ACCESS_TOKEN_KEY,
                            newToken, evaConfig.getCookie().getMaxAge(), "/", evaConfig.getCookie().getDomain(),
                            evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());
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
            long tenantId = jwtUtil.getTenantId(authToken);
            String account = jwtUtil.getAccount(authToken);

            if (StrUtils.isNotBlank(account)) {
                // JWT仅携带权限版本，不将其中的角色声明作为本次请求的授权依据。
                long tokenPermVer = jwtUtil.getPermVer(authToken);

                AuthUserEntity authState = evaConfig.getTenant().isSchemaMode()
                        ? tenantAuthRoutingService.execute(tenantId, () -> authUserService.getAuthState(uid))
                        : authUserService.getAuthState(uid);
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

                List<AuthRoleEntity> trustedRoles = authState.getRoles() == null
                        ? Collections.emptyList() : authState.getRoles();
                List<Long> roleIds = trustedRoles.stream()
                        .map(AuthRoleEntity::getId)
                        .filter(Objects::nonNull)
                        .toList();
                Map<Long, ThreadUser.GrantedRoles> rolesMap = trustedRoles.stream()
                        .filter(role -> role.getId() != null)
                        .collect(Collectors.toMap(AuthRoleEntity::getId,
                                role -> new ThreadUser.GrantedRoles(role.getName(), role.getCode()),
                                (first, second) -> first, LinkedHashMap::new));

                // RBAC资源权限校验（permit路径跳过校验）
                if (evaConfig.getResourcePermission().isEnable() && !isPermitPath(requestPath)) {
                    String httpMethod = request.getMethod();
                    if (roleIds == null || roleIds.isEmpty()) {
                        log.warn("用户 {} 无角色, 禁止访问: {} {}", account, httpMethod, requestPath);
                        ResponseUtil.write(response, Response.failure(AuthCodes.RESOURCE_FORBIDDEN));
                        return;
                    }

                    if (!roleResourceCacheService.hasPermission(tenantId, roleIds, httpMethod, requestPath)) {
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
                        .setAccount(account)
                        .setName(StrUtils.isBlank(authState.getName()) ? account : authState.getName())
                        .setTenantId(tenantId)
                        .setDeptId(authState.getDeptId() == null ? 0L : authState.getDeptId())
                        .setDataScopes(toDataScopes(authState))
                        .setRoles(roleNames)
                        .setRolesMap(rolesMap)
                        .setCapabilities(resolveCapabilities(tenantId, rolesMap));

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

    private Set<String> resolveCapabilities(long tenantId,
                                            Map<Long, ThreadUser.GrantedRoles> rolesMap) {
        boolean platformAdministrator = evaConfig.isPlatformMode()
                && tenantId == 0L
                && rolesMap.values().stream()
                .anyMatch(role -> CommonConstant.ADMIN_ROLE_NAME.equals(role.getCode()));
        return platformAdministrator ? Set.of(PlatformCapabilities.TENANT_INSPECT) : Set.of();
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
     * 将数据库角色权限转换为当前请求使用的数据权限快照。
     */
    private List<ThreadUser.DataScope> toDataScopes(AuthUserEntity authState) {
        if (authState.getRoles() == null) {
            return Collections.emptyList();
        }
        return authState.getRoles().stream()
                .filter(Objects::nonNull)
                .map(role -> new ThreadUser.DataScope(role.getDataScope(), parseOrgIds(role.getDataOrgIds())))
                .toList();
    }

    private List<Long> parseOrgIds(String dataOrgIds) {
        if (dataOrgIds == null || dataOrgIds.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(dataOrgIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(this::parsePositiveLong)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private Long parsePositiveLong(String value) {
        try {
            long id = Long.parseLong(value);
            return id > 0L ? id : null;
        } catch (NumberFormatException exception) {
            log.warn("忽略非法的数据权限组织 ID: {}", value);
            return null;
        }
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
                null, 0, "/", evaConfig.getCookie().getDomain(),
                evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());
        CookieUtils.addCookie(response, CommonConstant.REFRESH_TOKEN_KEY,
                null, 0, "/", evaConfig.getCookie().getDomain(),
                evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());
        CookieUtils.addCookie(response, CommonConstant.USER_KEY, null, 0, "/",
                evaConfig.getCookie().getDomain(), evaConfig.getCookie().isSecure(),
                evaConfig.getCookie().getSameSite());
    }
}
