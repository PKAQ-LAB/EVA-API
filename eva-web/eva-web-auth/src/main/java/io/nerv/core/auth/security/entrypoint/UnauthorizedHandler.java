package io.nerv.core.auth.security.entrypoint;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.util.json.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.naming.InsufficientResourcesException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * 鉴权失败时的响应
 * AuthenticationEntryPoint 用来解决匿名用户访问无权限资源时的异常
 * @author: S.PKAQ
 */
@Component
public class UnauthorizedHandler implements AuthenticationEntryPoint, Serializable {

    /**
     * 当访问的资源没有权限，会调用这里
     *
     * @param request       that resulted in an <code>AuthenticationException</code>
     * @param response      so that the user agent can begin authentication
     * @param authException that caused the invocation
     * @throws IOException
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        //返回json形式的错误信息

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter printWriter = response.getWriter()) {
            printWriter.write(JsonUtil.toJson(
                    new Response()
                            .failure(BizCodeEnum.LOGIN_EXPIRED)));
            printWriter.flush();
        }
    }
}
