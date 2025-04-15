package org.pkaq.core.auth.security.entrypoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pkaq.core.enums.BizCodeEnum;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.json.JsonUtil;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义权限不足处理器
 * AccessDeineHandler 用来解决认证过的用户访问无权限资源时的异常
 *
 * @author PKAQ
 */
@Component
public class UrlAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter printWriter = response.getWriter()) {
            printWriter.write(JsonUtil.toJson(
                    new Response()
                            .failure(BizCodeEnum.PERMISSION_DENY)));
            printWriter.flush();
        }
    }

}