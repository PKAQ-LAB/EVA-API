package org.pkaq.web.jxc.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.pkaq.web.jxc.order.entity.OrderEntity;
import org.pkaq.web.jxc.order.mapper.OrderMapper;
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
        this.mapper.insert(order);
    }

    public List<OrderEntity> list(){
        QueryWrapper entityWrapper = new QueryWrapper();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMonth(new Date());

        entityWrapper.setEntity(orderEntity);
        return this.mapper.selectList(entityWrapper);
    }
}
