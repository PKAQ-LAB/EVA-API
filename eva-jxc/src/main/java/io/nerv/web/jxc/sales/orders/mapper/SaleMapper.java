package io.nerv.web.jxc.sales.orders.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.nerv.web.jxc.sale.entity.SaleEntity;
import org.springframework.stereotype.Repository;

/**
 * 零售开单mapper
 * @author: S.PKAQ
 * @Datetime: 2018/3/26 23:03
 */
@Mapper
@Repository
public interface SaleMapper extends BaseMapper<SaleEntity> {
}
