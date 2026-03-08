package org.pkaq.core.auth.security.entrypoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.web.core.utils.ResponseUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

/**
 * 鉴权失败时的响应
 * AuthenticationEntryPoint 用来解决匿名用户访问无权限资源时的异常
 *
 * @author PKAQ
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

        ResponseUtil.write(response, Response
                .failure(AuthCodes.LOGIN_EXPIRED));
    }
}
