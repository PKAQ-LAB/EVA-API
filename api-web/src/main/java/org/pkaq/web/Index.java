package org.pkaq.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/7/10 7:15
 */
@Controller
public class Index {
    @RequestMapping({"/",""})
    @ApiIgnore
    public void index(HttpServletResponse response) throws IOException {
        response.sendRedirect("swagger-ui.html");
    }
}
