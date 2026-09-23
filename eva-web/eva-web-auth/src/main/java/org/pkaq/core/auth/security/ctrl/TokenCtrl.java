package org.pkaq.core.auth.security.ctrl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.auth.role.entity.AuthRoleEntity;
import org.pkaq.core.auth.log.service.LoginLogService;
import org.pkaq.core.auth.user.entity.AuthUserEntity;
import org.pkaq.core.auth.user.service.AuthUserService;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.auth.tenant.TenantAuthRoutingService;
import org.pkaq.core.auth.tenant.TenantLoginIdentity;
import org.pkaq.core.auth.tenant.TenantLoginResolver;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.web.core.utils.CookieUtils;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * Token控制器
 *
 * @author PKAQ
 */
@RestController
@RequiredArgsConstructor
public class TokenCtrl {
    private final JwtUtil jwtUtil;
    private final EvaConfig evaConfig;
    private final CacheTokenUtil cacheTokenUtil;
    private final TokenUtils tokenUtil;
    private final AuthUserService authUserService;
    private final TenantLoginResolver tenantLoginResolver;
    private final TenantAuthRoutingService tenantAuthRoutingService;
    private final LoginLogService loginLogService;

    /**
     * 使用refresh token换取access token
     * 1. 签发新的 access_token
     * 2. 删除旧的 access_token
     * 3. 签发新的 refreshToken
     * 4. 删除旧的 refreshToken
     */
    @PostMapping("/auth/getAlpha")
    public Response<Object> refreshToken(@RequestBody(required = false) Map<String, String> body,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        String refreshTk = tokenUtil.getRefreshToken(request);
        if ((refreshTk == null || refreshTk.isBlank()) && body != null) {
            refreshTk = body.get("refreshToken");
            if (refreshTk == null || refreshTk.isBlank()) {
                refreshTk = body.get(CommonConstant.REFRESH_TOKEN_KEY);
            }
        }

        // 是否持久化token
        var cacheToken = evaConfig.getJwt().isPersistence();

        // 验证 refresh token 是否有效(合法/未过期)
        var tokenStatus = jwtUtil.valid(refreshTk) && jwtUtil.isRefreshToken(refreshTk);

        // refresh token 过期/无效, 重新登录
        if (!tokenStatus) {
            // 清除cookie
            this.clearCookie(response);
            AuthCodes.LOGIN_EXPIRED.newException(AuthenticationException.class);
        }

        // 获取当前用户信息
        long uid = jwtUtil.getUid(refreshTk);
        long tenantId = jwtUtil.getTenantId(refreshTk);
        String account = jwtUtil.getAccount(refreshTk);
        String oldSessionId = jwtUtil.getSessionId(refreshTk);
        long tokenPermVer = jwtUtil.getPermVer(refreshTk);

        // 权限版本号校验
        TenantLoginIdentity tenantIdentity = tenantLoginResolver.resolveId(tenantId);
        if (tenantIdentity.schemaGeneration() != jwtUtil.getSchemaGeneration(refreshTk)) {
            this.clearCookie(response);
            AuthCodes.LOGIN_EXPIRED.newException(AuthenticationException.class);
        }
        AuthUserEntity authState = tenantAuthRoutingService.execute(tenantId,
                () -> authUserService.getAuthState(uid));
        if (!isAuthStateAvailable(authState)) {
            this.clearCookie(response);
            AuthCodes.LOGIN_EXPIRED.newException(AuthenticationException.class);
        }
        if (cacheToken && (oldSessionId == null || oldSessionId.isBlank()
                || !cacheTokenUtil.matchesRefreshToken(tenantId, uid, oldSessionId, refreshTk))) {
            this.clearCookie(response);
            AuthCodes.LOGIN_EXPIRED.newException(AuthenticationException.class);
        }
        long dbPermVer = authState.getPermVer() == null ? 0L : authState.getPermVer();
        if (dbPermVer != tokenPermVer) {
            this.clearCookie(response);
            AuthCodes.PERM_VER_CHANGED.newException(AuthenticationException.class);
        }

        var roleIds = authState.getRoles() == null
                ? java.util.Collections.<Long>emptyList()
                : authState.getRoles().stream().map(AuthRoleEntity::getId).toList();
        String newSessionId = jwtUtil.newSessionId();

        // access token 与 refresh token 共用同一会话标识，刷新后旧会话立即失效。
        String newAlpha = jwtUtil.build(evaConfig.getJwt().getAlphaTtl(), uid, account, roleIds, dbPermVer,
                tenantId, tenantIdentity.schemaGeneration(), newSessionId);

        // 签发新的 refresh token
        String newBravo = jwtUtil.buildRefreshToken(evaConfig.getJwt().getBravoTtl(), uid, account, roleIds, dbPermVer,
                tenantId, tenantIdentity.schemaGeneration(), newSessionId);

        // 先废止旧 refresh token，再登记新会话，阻止旧 token 重放。
        if (cacheToken) {
            cacheTokenUtil.removeToken(tenantId, uid, oldSessionId);
            cacheTokenUtil.saveToken(tenantId, uid, newSessionId,
                    cacheTokenUtil.buildCacheValue(request, uid, newAlpha, newBravo));
        }
        loginLogService.rotateSession(tenantId, uid, oldSessionId, newSessionId);

        // 替换客户端的旧token
        String domain = evaConfig.getCookie().getDomain();
        String path = "/";

        CookieUtils.addCookie(response, CommonConstant.ACCESS_TOKEN_KEY,
                newAlpha, evaConfig.getCookie().getMaxAge(), path, domain,
                evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());
        CookieUtils.addCookie(response, CommonConstant.REFRESH_TOKEN_KEY,
                newBravo, evaConfig.getCookie().getMaxAge(), path, domain,
                evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());

        var map = Map.of(CommonConstant.ACCESS_TOKEN_KEY, newAlpha,
                CommonConstant.REFRESH_TOKEN_KEY, newBravo);

        return Response.success(map);
    }

    /**
     * 清除cookie
     *
     * @param response 响应对象
     */
    public void clearCookie(HttpServletResponse response) {
        String domain = evaConfig.getCookie().getDomain();

        CookieUtils.clearCookie(response, CommonConstant.ACCESS_TOKEN_KEY, "/", domain,
                evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());
        CookieUtils.clearCookie(response, CommonConstant.REFRESH_TOKEN_KEY, "/", domain,
                evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());
        CookieUtils.clearCookie(response, CommonConstant.USER_KEY, "/", domain,
                evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());
    }

    /**
     * 判断刷新Token对应用户是否仍允许认证。
     */
    private boolean isAuthStateAvailable(AuthUserEntity authState) {
        if (authState == null || FrozenEnumm.FROZEN == authState.getFrozen()) {
            return false;
        }
        if (FrozenEnumm.FROZEN == authState.getTenantFrozen()) {
            return false;
        }
        return authState.getTenantExpirationDate() == null || authState.getTenantExpirationDate().after(new Date());
    }
}
