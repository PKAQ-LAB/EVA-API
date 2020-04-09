package io.nerv.web.jxc.sale.slip.service;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.nerv.core.mvc.service.mybatis.StdBaseService;
import io.nerv.web.jxc.sale.slip.entity.SlipEntity;
import io.nerv.web.jxc.sale.slip.mapper.SlipMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 零售开单service
 * @author: S.PKAQ
 * @Datetime: 2018/3/26 23:05
 */
@Service
public class SlipService extends StdBaseService<SlipMapper, SlipEntity> {

    @Override
    public void merge(SlipEntity entity) {
        // 总成本价
        BigDecimal totalCost = NumberUtil.add(entity.getShipPrice(), NumberUtil.mul(entity.getNummer(), entity.getCostPrice()));
        entity.setTotalCost(totalCost);
        // 总零售
        BigDecimal totalPrice = NumberUtil.mul(entity.getNummer(), entity.getPrice());
        entity.setTotalPrice(totalPrice);
        // 利润
        entity.setProfit(NumberUtil.sub(totalPrice, totalCost));

        super.merge(entity);
    }

    @Override
    public void merge(SlipEntity entity, Wrapper<SlipEntity> wrapper) {
        // 总成本价
        BigDecimal totalCost = NumberUtil.add(entity.getShipPrice(), NumberUtil.mul(entity.getNummer(), entity.getCostPrice()));
        entity.setTotalCost(totalCost);
        // 总零售
        BigDecimal totalPrice = NumberUtil.mul(entity.getNummer(), entity.getPrice());
        entity.setTotalPrice(totalPrice);
        // 利润
        entity.setProfit(NumberUtil.sub(totalPrice, totalCost));


        super.merge(entity, wrapper);
    }
}
