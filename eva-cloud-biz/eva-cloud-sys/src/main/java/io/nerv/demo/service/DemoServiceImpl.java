package io.nerv.demo.service;

import io.nerv.biz.sys.DemoService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class DemoServiceImpl implements DemoService {

    @Override
    public void print(){
        System.out.println("Dubbo service called");
    }
}
