package org.pkaq.core.auth.security.entrypoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.json.JsonUtil;
import org.pkaq.web.core.utils.CookieUtils;
import org.pkaq.web.core.utils.ResponseUtil;
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

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Authentication authentication) throws IOException {

        var cacheToken = evaConfig.getJwt().isPersistence();
        // 清空redis/caffeine中的token 刷新用户secret
        if (cacheToken) {
            this.tokenUtil.removeToken(ThreadUserHelper.getUserName());
        }
        String domain = evaConfig.getCookie().getDomain();

        CookieUtils.clearCookie(httpServletResponse, CommonConstant.ACCESS_TOKEN_KEY, "/", domain);
        CookieUtils.clearCookie(httpServletResponse, CommonConstant.REFRESH_TOKEN_KEY, "/", domain);
        CookieUtils.clearCookie(httpServletResponse, CommonConstant.USER_KEY, "/", domain);

        ResponseUtil.OK(httpServletResponse, JsonUtil.toJson(Response.failure(CommonCodes.LOGINOUT_SUCCESS)));

    }
}
