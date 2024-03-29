package io.nerv.core.web.mvc;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * 默认控制器 访问swagger首页
 *
 * @author: S.PKAQ
 */
@Controller
public class IndexCtrl {
    @RequestMapping("/")
    @Hidden
    public void index(HttpServletResponse response) throws IOException {
        System.out.println("-------------------------->");
        response.sendRedirect("doc.html");
    }
}

