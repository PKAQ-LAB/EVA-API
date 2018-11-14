package org.pkaq.web.jxc.stock.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.web.jxc.stock.entity.StockEntity;
import org.pkaq.web.jxc.stock.mapper.StockMapper;
import org.springframework.stereotype.Service;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/10/16 10:06
 */
@Service
public class StockService extends BaseService<StockMapper, StockEntity> {
    private final String LOCK_STATE = "0001";
    private final String NORMAL_STATE = "0000";
    /**
     * 更改库存状态
     * 盘点期间: 锁定库存
     */
    public void lock(){
        StockEntity stockEntity = new StockEntity();
        stockEntity.setState(LOCK_STATE);

        this.mapper.update(stockEntity, null);
    }

    /**
     * 更改库存状态
     * 解锁
     */
    public void unlock(){
        StockEntity stockEntity = new StockEntity();
        stockEntity.setState(NORMAL_STATE);

        this.mapper.update(stockEntity, null);
    }
    /**
     * 更改库存数量
     */
    public void changeOnhandNum(StockEntity stockEntity){
        synchronized (this){
            // 判断是否存在相同价格的商品
            StockEntity queryEntity = new StockEntity();
            queryEntity.setGoodsId(stockEntity.getGoodsId());

            Wrapper<StockEntity> wrapper = new QueryWrapper<>(queryEntity);
            int count = this.mapper.selectCount(wrapper);
            // 1. 不存在， 直接insert
            // 2. 存在， update数量
            if (count < 1){
                this.mapper.insert(stockEntity);
            } else {
                this.mapper.changeOnhandsNum(stockEntity.getOnhand(), stockEntity.getAmount(), stockEntity.getGoodsId());
            }
        }
    }
}
