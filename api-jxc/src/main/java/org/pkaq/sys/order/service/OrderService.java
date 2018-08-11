package org.pkaq.sys.order.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
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
        this.mapper.insert(order);
    }

    public List<OrderEntity> list(){
        EntityWrapper entityWrapper = new EntityWrapper();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMonth(new Date());

        entityWrapper.setEntity(orderEntity);
        return this.mapper.selectList(entityWrapper);
    }
}
