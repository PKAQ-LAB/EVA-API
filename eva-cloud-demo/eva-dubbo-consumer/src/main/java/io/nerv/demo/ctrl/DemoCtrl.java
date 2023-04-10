package io.nerv.demo.ctrl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import io.nerv.dubbo.DemoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/dubbo")
@RestController
public class DemoCtrl {
    @DubboReference
    private DemoService demoService;

    @GetMapping("/call")
    public String call(){
       var s =  demoService.print();
       return s;
    }

    @GetMapping("/ex")
    @SentinelResource(value = "fallback", fallback = "handleFallback")
    public String ex() throws Exception {
        if (true){
            throw new Exception("exception");
        }
        return "ex method";
    }
    public String handleFallback(){
        return "sentinel fallback called";
    }
}