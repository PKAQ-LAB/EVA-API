package io.nerv.security.entrypoint;

import com.alibaba.fastjson.JSON;
import io.nerv.core.enums.ErrorCodeEnum;
import io.nerv.core.mvc.util.Response;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义登录失败处理器
 */
@SuppressWarnings("Duplicates")
@Component
public class UrlAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        AuthenticationException e) throws IOException {
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        Response response = new Response().failure(ErrorCodeEnum.LOGIN_ERROR.getIndex(), ErrorCodeEnum.LOGIN_ERROR.getName());
        httpServletResponse.getWriter().write(JSON.toJSONString(response));
    }
}
