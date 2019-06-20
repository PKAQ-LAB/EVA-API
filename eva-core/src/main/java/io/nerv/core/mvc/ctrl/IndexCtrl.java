package io.nerv.core.mvc.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认控制器 访问swagger首页
 * @author: S.PKAQ
 * @Datetime: 2018/9/29 10:53
 */
@Controller
public class IndexCtrl {
    @RequestMapping({"/",""})
    @ApiIgnore
    public void index(HttpServletResponse response) throws IOException {
        response.sendRedirect("doc.html");
    }
}

