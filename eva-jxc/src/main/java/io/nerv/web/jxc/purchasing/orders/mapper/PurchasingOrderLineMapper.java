package io.nerv.web.jxc.purchasing.orders.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.nerv.web.jxc.purchasing.orders.entity.PurchasingOrderLineEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 采购入库单明细
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:57
 */
@Mapper
@Repository
public interface PurchasingOrderLineMapper extends BaseMapper<PurchasingOrderLineEntity> {
    List<PurchasingOrderLineEntity> listByMainId(String mainId);
}
