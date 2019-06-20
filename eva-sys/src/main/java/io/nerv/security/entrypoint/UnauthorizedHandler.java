package io.nerv.security.entrypoint;

import io.nerv.core.enums.HttpCodeEnum;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * 鉴权失败时的响应
 * @author: S.PKAQ
 * @Datetime: 2018/4/24 23:24
 */
@Component
public class UnauthorizedHandler implements AuthenticationEntryPoint, Serializable {
    //当访问的资源没有权限，会调用这里
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // This is invoked when user tries to access a secured REST resource without supplying any credentials
        // We should just send a 401 Unauthorized response because there is no 'login page' to redirect to
        //返回json形式的错误信息
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, HttpCodeEnum.ROLE_ERROR.getName());
    }
}
