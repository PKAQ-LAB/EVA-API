package io.nerv.security.entrypoint;

import com.alibaba.fastjson.JSON;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.util.Response;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
            msg = BizCodeEnum.ACCOUNT_OR_PWD_ERROR.getName();
        }


        try(PrintWriter printWriter = httpServletResponse.getWriter()){
            printWriter.write(JSON.toJSONString(
                    new Response()
                            .failure(BizCodeEnum.LOGIN_FAILED.getIndex(), msg)));
            printWriter.flush();
        }
    }
}
