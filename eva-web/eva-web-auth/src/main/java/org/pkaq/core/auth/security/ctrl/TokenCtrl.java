package org.pkaq.core.auth.security.ctrl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.CookieUtils;
import org.pkaq.core.util.TokenUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author PKAQ
 */
@RestController
@RequiredArgsConstructor
public class TokenCtrl {
    private final JwtUtil jwtUtil;
    private final EvaConfig evaConfig;
    private final CacheTokenUtil cacheTokenUtil;
    private final TokenUtil tokenUtil;

    /**
     * 使用refresh token 换取 access token
     * 1. 签发新的 access_token
     * 2. 删除老的 access_token
     * 3. 签发新的 refreshToken
     * 4. 删除老的 refreshToken
     *
     * @return
     */
    @PostMapping("/auth/getAlpha")
    public Response<Object> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTk = tokenUtil.getRefreshToken(request);

        // 是否持久化token
        var cacheToken = evaConfig.getJwt().isPersistence();

        // 验证 refresh token 是否有效(合法/未过期)
        var tokenStatus = jwtUtil.valid(refreshTk);

        // refresh token 过期/无效, 重新登录
        if (!tokenStatus) {
            // 清除cookie
            this.clearCookie(response);
            AuthCodes.LOGIN_EXPIRED.newException(AuthenticationException.class);
        }

        // 获取当前用户 account
        long uid = jwtUtil.getUid(refreshTk);
        String account = jwtUtil.getAccount(refreshTk);
        // 签发新 access token
        String new_alpha = jwtUtil.build(evaConfig.getJwt().getAlphaTtl(), uid, account);

        // 签发新 refresh token
        String new_bravo = jwtUtil.build(evaConfig.getJwt().getBravoTtl(), uid, account);

        // 替换客户端的旧token
        String domain = evaConfig.getCookie().getDomain();
        String path = "/";

        CookieUtils.addCookie(response, CommonConstant.ACCESS_TOKEN_KEY, new_alpha, 0, path, domain);
        CookieUtils.addCookie(response, CommonConstant.REFRESH_TOKEN_KEY, new_bravo, 0, path, domain);

        // 持久化 token
        if (cacheToken) {
            cacheTokenUtil.saveToken(uid, cacheTokenUtil.buildCacheValue(request, uid, new_alpha));
        }

        var map = Map.of(CommonConstant.ACCESS_TOKEN_KEY, new_alpha,
                CommonConstant.REFRESH_TOKEN_KEY, new_bravo);

        return Response.success(map);

    }

    /**
     * 清除cookie
     *
     * @param response
     */
    public void clearCookie(HttpServletResponse response) {
        String domain = evaConfig.getCookie().getDomain();

        CookieUtils.clearCookie(response, CommonConstant.ACCESS_TOKEN_KEY, "/", domain);
        CookieUtils.clearCookie(response, CommonConstant.REFRESH_TOKEN_KEY, "/", domain);
        CookieUtils.clearCookie(response, CommonConstant.USER_KEY, "/", domain);
    }
}
