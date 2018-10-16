package org.pkaq.web.jxc.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.web.jxc.goods.entity.GoodsEntity;
import org.pkaq.web.jxc.goods.mapper.GoodsMapper;
import org.springframework.stereotype.Service;

/**
 * 商品管理
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:59
 */
@Service
public class GoodsService extends BaseService<GoodsMapper, GoodsEntity> {
    public void save(GoodsEntity goodsEntity){
        this.merge(goodsEntity);
    }

    public IPage<GoodsEntity> list(GoodsEntity goodsEntity, Integer pageNo){
       return this.listPage(goodsEntity, pageNo);
    }
}
