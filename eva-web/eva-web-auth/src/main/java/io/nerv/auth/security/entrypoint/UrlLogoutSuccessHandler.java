package io.nerv.auth.security.entrypoint;

import cn.hutool.extra.servlet.ServletUtil;
import io.nerv.auth.util.CacheTokenUtil;
import io.nerv.common.constant.CommonConstant;
import io.nerv.common.enums.BizCodeEnum;
import io.nerv.common.mvc.vo.Response;
import io.nerv.common.threaduser.ThreadUserHelper;
import io.nerv.common.util.json.JsonUtil;
import io.nerv.properties.EvaConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义注销成功处理器
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
        ServletUtil.addCookie(httpServletResponse,
                CommonConstant.ACCESS_TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        ServletUtil.addCookie(httpServletResponse,
                CommonConstant.REFRESH_TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());


        ServletUtil.addCookie(httpServletResponse,
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
