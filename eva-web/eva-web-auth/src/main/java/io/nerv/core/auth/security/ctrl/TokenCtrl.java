package io.nerv.core.auth.security.ctrl;

import cn.hutool.extra.servlet.JakartaServletUtil;
import io.nerv.core.auth.util.CacheTokenUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.jwt.JwtUtil;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.properties.EvaConfig;
import io.nerv.core.web.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
    public Response refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTk = tokenUtil.getRefreshToken(request);

        // 是否持久化token
        var cacheToken = evaConfig.getJwt().isPersistence();

        // 验证 refresh token 是否有效(合法/未过期)
        var tokenStatus = jwtUtil.valid(refreshTk);

        // refresh token 过期/无效, 重新登录
        if (!tokenStatus) {
            // 清除cookie
            this.clearCookie(response);
            BizCodeEnum.LOGIN_EXPIRED.newException(AuthenticationException.class);
        }

        // 获取当前用户 account
        String uid = jwtUtil.getUid(refreshTk);
        String account = jwtUtil.getUid(refreshTk);
        // 签发新 access token
        String new_alpha = jwtUtil.build(evaConfig.getJwt().getAlphaTtl(), uid, account);

        // 签发新 refresh token
        String new_bravo = jwtUtil.build(evaConfig.getJwt().getBravoTtl(), uid, account);

        // 替换客户端的旧token
        JakartaServletUtil.addCookie(response,
                CommonConstant.ACCESS_TOKEN_KEY,
                new_alpha,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        JakartaServletUtil.addCookie(response,
                CommonConstant.REFRESH_TOKEN_KEY,
                new_bravo,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        // 持久化 token
        if (cacheToken) {
            cacheTokenUtil.saveToken(uid, cacheTokenUtil.buildCacheValue(request, account, new_alpha));
        }

        var map = Map.of(CommonConstant.ACCESS_TOKEN_KEY, new_alpha,
                CommonConstant.REFRESH_TOKEN_KEY, new_bravo);

        return new Response().success(map);

    }

    /**
     * 清除cookie
     *
     * @param response
     */
    public void clearCookie(HttpServletResponse response) {
        JakartaServletUtil.addCookie(response,
                CommonConstant.ACCESS_TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        JakartaServletUtil.addCookie(response,
                CommonConstant.REFRESH_TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        JakartaServletUtil.addCookie(response,
                CommonConstant.USER_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

    }
}
