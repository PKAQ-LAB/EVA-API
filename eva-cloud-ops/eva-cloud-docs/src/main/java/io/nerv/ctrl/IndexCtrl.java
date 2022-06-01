package io.nerv.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class IndexCtrl {
    public IndexCtrl() {
    }

    @RequestMapping({"/"})
    public void index(HttpServletResponse response) throws IOException {
        response.sendRedirect("doc.html");
    }
}
