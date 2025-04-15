package org.pkaq.core.auth.security.entrypoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pkaq.core.enums.BizCode;
import org.pkaq.core.enums.BizCodeEnum;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.json.JsonUtil;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义登录失败处理器
 *
 * @author PKAQ
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

        BizCode bizcode = BizCodeEnum.LOGIN_FAILED;

        if (e instanceof BadCredentialsException) {
            bizcode = BizCodeEnum.ACCOUNT_OR_PWD_ERROR;
        }


        try (PrintWriter printWriter = httpServletResponse.getWriter()) {
            printWriter.write(JsonUtil.toJson(
                    new Response().failure(bizcode)
            ));
            printWriter.flush();
        }
    }
}
