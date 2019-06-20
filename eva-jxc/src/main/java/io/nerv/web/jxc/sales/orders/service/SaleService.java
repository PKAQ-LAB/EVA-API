package io.nerv.web.jxc.sales.orders.service;

import io.nerv.core.helper.OrderHelper;
import io.nerv.core.mvc.service.StdBaseService;
import io.nerv.web.jxc.sales.orders.mapper.SaleMapper;
import io.nerv.web.jxc.sale.entity.SaleEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 零售开单service
 * @author: S.PKAQ
 * @Datetime: 2018/3/26 23:05
 */
@Service
public class SaleService extends StdBaseService<SaleMapper, SaleEntity> {
    /**
     * 保存销售单
     * @param invoices 销售单商品列表
     */
    public void checkOut(List<SaleEntity> invoices){
        String orderCode = OrderHelper.getOrderNumber("10001");
        for (SaleEntity saleEntity: invoices) {
            saleEntity.setId(null);
            saleEntity.setOrderCode(orderCode);
            this.mapper.insert(saleEntity);
        }
    }
}
