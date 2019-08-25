package io.nerv.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class RestC {
    @RequestMapping("/yi/list")
    public String list(){
        return "fuck";
    }
}
