package org.pkaq.web.jxc.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.web.jxc.order.entity.OrderEntity;
import org.springframework.stereotype.Repository;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:57
 */
@Mapper
@Repository
public interface OrderMapper extends BaseMapper<OrderEntity> {
}
