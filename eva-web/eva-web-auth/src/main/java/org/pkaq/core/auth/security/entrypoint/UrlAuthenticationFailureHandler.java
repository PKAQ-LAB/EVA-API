package org.pkaq.core.auth.security.entrypoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.codes.BizCode;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.web.core.utils.ResponseUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

        BizCode bizcode = AuthCodes.LOGIN_FAILED;

        if (e instanceof BadCredentialsException) {
            bizcode = AuthCodes.ACCOUNT_OR_PWD_ERROR;
        }

        ResponseUtil.write(httpServletResponse,Response.failure(bizcode.getCode(), bizcode.getMsg()));
    }
}
