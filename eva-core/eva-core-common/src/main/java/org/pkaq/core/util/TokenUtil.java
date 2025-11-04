package org.pkaq.core.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.properties.EvaConfig;
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
     * 获取token 先拿cookie再找header
     *
     * @param request
     * @return
     */
    public String getToken(HttpServletRequest request, String tokenKey) {
        String authToken = null;

        var authHeader = request.getHeader(evaConfig.getJwt().getHeader());

        authToken = CookieUtils.getCookie(request, tokenKey);

         if (StrUtils.isBlank(authToken) && StrUtils.isNotBlank(authHeader) && authHeader.startsWith(evaConfig.getJwt().getTokenHead())) {
            authToken = authHeader.substring(evaConfig.getJwt().getTokenHead().length());
        }

        return authToken;
    }
}
