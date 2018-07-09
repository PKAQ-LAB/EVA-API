package org.pkaq.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/7/10 7:15
 */
@Controller
public class Index {
    @RequestMapping({"/",""})
    public String index(){
        return "swagger-ui.html";
    }
}
