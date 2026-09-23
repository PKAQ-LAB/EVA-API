package org.pkaq.core.auth.security.entrypoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.auth.log.service.LoginLogService;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.web.core.utils.CookieUtils;
import org.pkaq.web.core.utils.ResponseUtil;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义注销成功处理器
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class UrlLogoutSuccessHandler implements LogoutSuccessHandler {

    private final EvaConfig evaConfig;

    private final CacheTokenUtil tokenUtil;
    private final TokenUtils requestTokenUtil;
    private final JwtUtil jwtUtil;
    private final LoginLogService loginLogService;

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Authentication authentication) throws IOException {

        var cacheToken = evaConfig.getJwt().isPersistence();
        // 仅清除当前设备会话，其他设备保持登录。
        String currentToken = requestTokenUtil.getToken(httpServletRequest);
        if (currentToken == null || currentToken.isBlank()) {
            currentToken = requestTokenUtil.getRefreshToken(httpServletRequest);
        }
        String sessionId = null;
        Long tenantId = null;
        Long userId = null;
        String account = null;
        if (currentToken != null && !currentToken.isBlank()
                && (jwtUtil.isAccessToken(currentToken) || jwtUtil.isRefreshToken(currentToken))) {
            sessionId = jwtUtil.getSessionId(currentToken);
            tenantId = jwtUtil.getTenantId(currentToken);
            userId = jwtUtil.getUid(currentToken);
            account = jwtUtil.getAccount(currentToken);
            if (cacheToken) {
                this.tokenUtil.removeToken(tenantId, userId, sessionId);
            }
        }
        String domain = evaConfig.getCookie().getDomain();

        CookieUtils.clearCookie(httpServletResponse, CommonConstant.ACCESS_TOKEN_KEY, "/", domain,
                evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());
        CookieUtils.clearCookie(httpServletResponse, CommonConstant.REFRESH_TOKEN_KEY, "/", domain,
                evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());
        CookieUtils.clearCookie(httpServletResponse, CommonConstant.USER_KEY, "/", domain,
                evaConfig.getCookie().isSecure(), evaConfig.getCookie().getSameSite());

        loginLogService.saveLogout(httpServletRequest, tenantId, userId, account, sessionId, "USER_LOGOUT");

        ResponseUtil.write(httpServletResponse, Response.success(null,
                CommonCodes.LOGINOUT_SUCCESS.getMsg(), CommonCodes.LOGINOUT_SUCCESS.getCode()));

    }
}
