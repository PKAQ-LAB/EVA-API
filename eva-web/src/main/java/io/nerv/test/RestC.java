package io.nerv.test;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/test")
public class RestC {
    @GetMapping("/yi/list")
    public String list(){
        System.out.println("123");
        return "fuck";
    }

    @GetMapping("/yi/get")
    public String get(){
        System.out.println(456);
        return "fuck get";
    }

    @GetMapping("/yi/del")
    public String del(){
        System.out.println(789);
        return "fuck 456";
    }
}
