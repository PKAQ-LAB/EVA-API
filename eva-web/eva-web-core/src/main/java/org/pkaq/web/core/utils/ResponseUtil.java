package org.pkaq.web.core.utils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;

/**
 * 统一响应工具类
 */
@Slf4j
public class ResponseUtil {
    public static void json(HttpServletResponse response,
                                 HttpStatus status,
                                 String message) throws IOException {
        // 检查响应是否已提交
        if (response.isCommitted()) {
            log.warn("响应已提交，无法再次写入");
            return;
        }
        if (status != null) {
            response.setStatus(status.value());
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
        response.getWriter().flush();
    }

    public static void OK(HttpServletResponse response,
                            String message) throws IOException {
        // 检查响应是否已提交
        if (response.isCommitted()) {
            log.warn("响应已提交，无法再次写入");
            return;
        }
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
        response.getWriter().flush();
    }

    public static void fail(HttpServletResponse response,
                          String message) throws IOException {
        // 检查响应是否已提交
        if (response.isCommitted()) {
            log.warn("响应已提交，无法再次写入");
            return;
        }
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
        response.getWriter().flush();
    }

}
