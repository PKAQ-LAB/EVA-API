package org.pkaq.app.doc;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * 当前应用默认入口，重定向到文档首页。
 *
 * @author PKAQ
 * @date 2026-10-09
 */
@Controller
public class IndexCtrl {
    /**
     * 保持默认入口的相对重定向，兼容应用上下文路径。
     *
     * @param response HTTP 响应
     * @throws IOException 重定向写入异常
     */
    @RequestMapping("/")
    @Hidden
    public void index(HttpServletResponse response) throws IOException {
        response.sendRedirect("doc.html");
    }
}
