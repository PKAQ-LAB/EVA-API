package org.pkaq.web.jxc.order.ctrl;

import org.pkaq.core.mvc.util.Response;
import org.pkaq.web.jxc.order.entity.OrderEntity;
import org.pkaq.web.jxc.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;

@RestController
@RequestMapping("/order")
public class OrderCtrl {
    @Autowired
    private OrderService orderService;

    @PermitAll
    @RequestMapping("/save")
    public Response save(@RequestBody(required = false) OrderEntity order){
        System.out.println("-xxx --x x -x ");
        this.orderService.save(order);
        return new Response().success("成功");
    }
    @GetMapping("/list")
    public Response queryAll(){
        return new Response().success(this.orderService.list());
    }
}
