package io.nerv.core.auth.security.entrypoint;

import cn.hutool.extra.servlet.JakartaServletUtil;
import io.nerv.core.auth.util.CacheTokenUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.response.Response;
import io.nerv.core.properties.EvaConfig;
import io.nerv.core.threaduser.ThreadUserHelper;
import io.nerv.core.util.json.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
            ThreadUserHelper.remove();
        }

        // 清除cookie
        JakartaServletUtil.addCookie(httpServletResponse,
                CommonConstant.ACCESS_TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        JakartaServletUtil.addCookie(httpServletResponse,
                CommonConstant.REFRESH_TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());


        JakartaServletUtil.addCookie(httpServletResponse,
                CommonConstant.USER_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        httpServletResponse.getWriter().write(JsonUtil.toJson(new Response().failure(BizCodeEnum.LOGINOUT_SUCCESS)));
    }
}
