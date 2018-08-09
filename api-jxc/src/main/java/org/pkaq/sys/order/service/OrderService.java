package org.pkaq.sys.order.service;

import org.pkaq.core.mvc.service.BaseActiveRecordService;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.sys.order.entity.OrderEntity;
import org.pkaq.sys.order.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:59
 */
@Service
public class OrderService{
    @Autowired
    private OrderMapper mapper;

    public void save(OrderEntity order) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setNote(Math.random()+"");
        orderEntity.setMonth(new Date());
        this.mapper.insert(order);
    }

    public List<OrderEntity> list(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        return this.mapper.selectList(null);
    }
}
