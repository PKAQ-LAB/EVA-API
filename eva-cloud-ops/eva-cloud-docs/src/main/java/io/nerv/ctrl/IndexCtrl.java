package io.nerv.ctrl;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
