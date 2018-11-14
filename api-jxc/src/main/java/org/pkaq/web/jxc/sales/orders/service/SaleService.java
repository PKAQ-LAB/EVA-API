package org.pkaq.web.jxc.sale.service;

import org.pkaq.core.helper.OrderHelper;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.web.jxc.sale.mapper.SaleMapper;
import org.pkaq.web.jxc.sale.entity.SaleEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 零售开单service
 * @author: S.PKAQ
 * @Datetime: 2018/3/26 23:05
 */
@Service
public class SaleService extends BaseService<SaleMapper, SaleEntity> {
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
