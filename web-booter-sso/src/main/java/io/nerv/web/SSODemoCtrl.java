package io.nerv.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SSODemoCtrl {
    @RequestMapping("/keytest")
    public String keyTest(){
        return "hello, keyCloak";
    }
}
