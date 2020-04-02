package io.nerv.web.jxc.base.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.web.jxc.base.goods.entity.GoodsEntity;
import org.apache.ibatis.annotations.Mapper;
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
