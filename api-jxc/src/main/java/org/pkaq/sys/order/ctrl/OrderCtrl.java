package org.pkaq.sys.order.ctrl;

import io.swagger.annotations.Api;
import org.pkaq.core.mvc.ctrl.BaseCtrl;
import org.pkaq.core.mvc.util.Response;
import org.pkaq.sys.order.entity.OrderEntity;
import org.pkaq.sys.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/order")
public class OrderCtrl {
    @Autowired
    private OrderService orderService;

    @PostMapping("/save")
    public Response save(@RequestBody OrderEntity order){
        int[] a = {1,2};
        Arrays.asList(a);
        this.orderService.save(order);
        return new Response();
    }
    @GetMapping("/list")
    public Response queryAll(){
        return new Response().success(this.orderService.list());
    }
}
