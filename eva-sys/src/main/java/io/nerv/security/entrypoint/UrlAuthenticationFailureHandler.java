package io.nerv.security.entrypoint;

import com.alibaba.fastjson.JSON;
import io.nerv.core.enums.ErrorCodeEnum;
import io.nerv.core.mvc.util.Response;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义登录失败处理器
 */
@Component
public class UrlAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        AuthenticationException e) throws IOException {
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        var msg = e.getMessage();

        if (e instanceof BadCredentialsException){
            msg = ErrorCodeEnum.ACCOUNT_OR_PWD_ERROR.getName();
        }
        Response response = new Response().failure(ErrorCodeEnum.LOGIN_FAILED.getIndex(), msg);
        httpServletResponse.getWriter().write(JSON.toJSONString(response));
    }
}
