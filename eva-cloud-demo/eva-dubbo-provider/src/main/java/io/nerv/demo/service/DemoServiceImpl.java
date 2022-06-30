package io.nerv.demo.service;

import io.nerv.dubbo.DemoService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class DemoServiceImpl implements DemoService {

    @Override
    public String print(){
        var s = "Dubbo service called";
        System.out.println("Dubbo service called");
        return s;
    }
}
