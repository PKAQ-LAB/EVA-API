package io.nerv.web.jxc.purchasing.orders.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.nerv.web.jxc.purchasing.orders.entity.PurchasingOrderEntity;
import io.nerv.web.jxc.purchasing.orders.vo.PurchasingOrderVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:57
 */
@Mapper
public interface PurchasingOrderMapper extends BaseMapper<PurchasingOrderEntity> {
    IPage<PurchasingOrderEntity> listPurchasing(Page pagination, PurchasingOrderVO purchasingOrderVO);
}
