package org.pkaq.web.jxc.purchasing.orders.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.web.jxc.purchasing.orders.entity.PurchasingOrderEntity;
import org.pkaq.web.jxc.purchasing.orders.vo.PurchasingOrderVO;
import org.springframework.stereotype.Repository;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:57
 */
@Mapper
@Repository
public interface PurchasingOrderMapper extends BaseMapper<PurchasingOrderEntity> {
    IPage<PurchasingOrderEntity> listInstock(Page pagination, PurchasingOrderVO purchasingOrderVO);
}
