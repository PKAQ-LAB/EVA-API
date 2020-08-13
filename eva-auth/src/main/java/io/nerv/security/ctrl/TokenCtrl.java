package io.nerv.security.ctrl;

import cn.hutool.extra.servlet.ServletUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.token.jwt.JwtUtil;
import io.nerv.core.token.util.TokenUtil;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class TokenCtrl {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private EvaConfig evaConfig;
    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 使用refresh token 换取 access token
     * 1. 签发新的 access_token
     * 2. 删除老的 access_token
     * 3. 签发新的 refreshToken
     * 4. 删除老的 refreshToken
     * @return
     */
    @PostMapping("/auth/getAlpha")
    public Response refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTk = tokenUtil.getRefreshToken(request);

        // 是否持久化token
        var cacheToken = evaConfig.getJwt().isPersistence();

        // 获取当前用户 account
        String cur_user = jwtUtil.getUid(refreshTk);
        // 签发新 access token
        String new_alpha = jwtUtil.build(evaConfig.getJwt().getAlphaTtl(), cur_user);

        // 签发新 refresh token
        String new_bravo = jwtUtil.build(evaConfig.getJwt().getBravoTtl(), cur_user);

        // 替换客户端的旧token
        ServletUtil.addCookie(response,
                CommonConstant.ACCESS_TOKEN_KEY,
                new_alpha,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        ServletUtil.addCookie(response,
                CommonConstant.REFRESH_TOKEN_KEY,
                new_bravo,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        // 持久化 token
        if (cacheToken) {
            tokenUtil.saveToken(cur_user, tokenUtil.buildCacheValue(request, cur_user, new_alpha));
        }

        var map = Map.of(CommonConstant.ACCESS_TOKEN_KEY, new_alpha,
                                              CommonConstant.REFRESH_TOKEN_KEY, new_bravo );

        return new Response().success(map);

    }
}
