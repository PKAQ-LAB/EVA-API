package org.pkaq.core.auth.security.ctrl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.auth.user.entity.AuthUserEntity;
import org.pkaq.core.auth.user.service.AuthUserService;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.web.core.utils.CookieUtils;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
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

    /**
     * 使用refresh token换取access token
     * 1. 签发新的 access_token
     * 2. 删除旧的 access_token
     * 3. 签发新的 refreshToken
     * 4. 删除旧的 refreshToken
     */
    @PostMapping("/auth/getAlpha")
    public Response<Object> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTk = tokenUtil.getRefreshToken(request);

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
        String account = jwtUtil.getAccount(refreshTk);
        List<Long> roleIds = jwtUtil.getRoles(refreshTk);
        long tokenPermVer = jwtUtil.getPermVer(refreshTk);

        // 权限版本号校验
        AuthUserEntity authState = authUserService.getAuthState(uid);
        if (!isAuthStateAvailable(authState)) {
            this.clearCookie(response);
            AuthCodes.LOGIN_EXPIRED.newException(AuthenticationException.class);
        }
        if (cacheToken && cacheTokenUtil.getToken(uid) == null) {
            this.clearCookie(response);
            AuthCodes.LOGIN_EXPIRED.newException(AuthenticationException.class);
        }
        long dbPermVer = authState.getPermVer() == null ? 0L : authState.getPermVer();
        if (dbPermVer != tokenPermVer) {
            this.clearCookie(response);
            AuthCodes.PERM_VER_CHANGED.newException(AuthenticationException.class);
        }

        // 签发新的 access token
        String newAlpha = jwtUtil.build(evaConfig.getJwt().getAlphaTtl(), uid, account, roleIds, dbPermVer);

        // 签发新的 refresh token
        String newBravo = jwtUtil.buildRefreshToken(evaConfig.getJwt().getBravoTtl(), uid, account, roleIds, dbPermVer);

        // 替换客户端的旧token
        String domain = evaConfig.getCookie().getDomain();
        String path = "/";

        CookieUtils.addCookie(response, CommonConstant.ACCESS_TOKEN_KEY,
                newAlpha, evaConfig.getCookie().getMaxAge(), path, domain);
        CookieUtils.addCookie(response, CommonConstant.REFRESH_TOKEN_KEY,
                newBravo, evaConfig.getCookie().getMaxAge(), path, domain);

        // 持久化token
        if (cacheToken) {
            cacheTokenUtil.saveToken(uid, cacheTokenUtil.buildCacheValue(request, uid, newAlpha));
        }

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

        CookieUtils.clearCookie(response, CommonConstant.ACCESS_TOKEN_KEY, "/", domain);
        CookieUtils.clearCookie(response, CommonConstant.REFRESH_TOKEN_KEY, "/", domain);
        CookieUtils.clearCookie(response, CommonConstant.USER_KEY, "/", domain);
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
