package org.pkaq.security.filter;

import org.pkaq.core.enums.HttpCodeEnum;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定403返回值
 *
 * @author hackyo
 * Created on 2017/12/9 20:10.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.sendError(HttpServletResponse.SC_FORBIDDEN, HttpCodeEnum.REQEUST_REFUSED.getName());
    }

}