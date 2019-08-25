package io.nerv.security.entrypoint;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import io.nerv.core.constant.TokenConst;
import io.nerv.core.mvc.util.Response;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义注销成功处理器
 */
@Component
public class UrlLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private EvaConfig evaConfig;

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Authentication authentication) throws IOException {
        System.out.println("5");
        // 获取用户jwt
        // 清空redis中的jwt 刷新用户secret
        ServletUtil.addCookie(httpServletResponse,
                TokenConst.TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());


        ServletUtil.addCookie(httpServletResponse,
                TokenConst.USER_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        httpServletResponse.getWriter().write(JSON.toJSONString(new Response().success(null, "您已退出登录")));
    }
}
