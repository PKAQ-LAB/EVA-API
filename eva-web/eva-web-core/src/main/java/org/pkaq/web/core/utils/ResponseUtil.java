package org.pkaq.web.core.utils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.json.JsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 统一响应工具类
 */
@Slf4j
public class ResponseUtil {
    public static void write(HttpServletResponse response,
                             Response<?> body) throws IOException {
        write(response, HttpStatus.OK, body);
    }

    public static void write(HttpServletResponse response,
                             HttpStatus status,
                             Response<?> body) throws IOException {

        if (response.isCommitted()) {
            log.warn("响应已提交，无法再次写入");
            return;
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8);

        String json = JsonUtil.toJson(body);
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
