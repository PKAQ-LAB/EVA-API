package io.nerv.core.web.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.properties.EvaConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class TokenUtil {
    private final EvaConfig evaConfig;

    /**
     * 获取access token
     *
     * @param request
     * @return
     */
    public String getToken(HttpServletRequest request) {
        return this.getToken(request, CommonConstant.ACCESS_TOKEN_KEY);
    }

    /**
     * 获取refresh token
     *
     * @param request
     * @return
     */
    public String getRefreshToken(HttpServletRequest request) {
        return this.getToken(request, CommonConstant.REFRESH_TOKEN_KEY);
    }

    /**
     * 获取token
     *
     * @param request
     * @return
     */
    public String getToken(HttpServletRequest request, String tokenKey) {
        String authToken = null;

        var authHeader = request.getHeader(evaConfig.getJwt().getHeader());

        if (null != JakartaServletUtil.getCookie(request, CommonConstant.ACCESS_TOKEN_KEY)) {
            authToken = JakartaServletUtil.getCookie(request, CommonConstant.ACCESS_TOKEN_KEY).getValue();
        } else if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(evaConfig.getJwt().getTokenHead())) {
            authToken = authHeader.substring(evaConfig.getJwt().getTokenHead().length());
        }

        return authToken;
    }
}
