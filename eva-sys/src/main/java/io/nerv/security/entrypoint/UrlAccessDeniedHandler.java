package io.nerv.security.entrypoint;

import com.alibaba.fastjson.JSON;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.util.Response;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义权限不足处理器
 */
@Component
public class UrlAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        try(PrintWriter printWriter = response.getWriter()){
            printWriter.write(JSON.toJSONString(
                                new Response()
                                        .failure(BizCodeEnum.PERMISSION_DENY)));
            printWriter.flush();
        }
    }

}