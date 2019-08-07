package io.nerv.web.jxc.purchasing.orders.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.nerv.core.mvc.service.mybatis.StdBaseService;
import io.nerv.web.jxc.purchasing.orders.mapper.PurchasingOrderMapper;
import io.nerv.web.jxc.purchasing.orders.entity.PurchasingOrderEntity;
import org.springframework.stereotype.Service;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:59
 */
@Service
public class PurchasingService extends StdBaseService<PurchasingOrderMapper, PurchasingOrderEntity> {

    @Override
    public IPage<PurchasingOrderEntity> listPage(PurchasingOrderEntity purchasingOrderEntity, Integer page){
        Page pagination = new Page();
        pagination.setCurrent(page);
        pagination.setSize(10);

        return this.mapper.listPurchasing(pagination, null);
    }
}
