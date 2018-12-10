package org.pkaq.web.jxc.base.goods.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.web.jxc.base.goods.entity.GoodsEntity;
import org.pkaq.web.jxc.base.goods.mapper.GoodsMapper;
import org.springframework.stereotype.Service;

/**
 * 商品管理
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:59
 */
@Service
public class GoodsService extends BaseService<GoodsMapper, GoodsEntity> {
    public boolean checkUnique(GoodsEntity goods) {
        QueryWrapper<GoodsEntity> entityWrapper = new QueryWrapper<>();
        entityWrapper.eq("barcode", goods.getBarcode());

        int records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }
}
