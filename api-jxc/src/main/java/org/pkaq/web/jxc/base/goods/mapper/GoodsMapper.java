package org.pkaq.web.jxc.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.web.jxc.goods.entity.GoodsEntity;
import org.springframework.stereotype.Repository;

/**
 * 商品管理
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:57
 */
@Mapper
@Repository
public interface GoodsMapper extends BaseMapper<GoodsEntity> {
}
