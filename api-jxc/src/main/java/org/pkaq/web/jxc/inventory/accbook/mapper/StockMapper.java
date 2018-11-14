package org.pkaq.web.jxc.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.web.jxc.stock.entity.StockEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/10/16 10:05
 */
@Mapper
@Repository
public interface StockMapper extends BaseMapper<StockEntity> {
   void changeOnhandsNum(@Param("num") Integer num,
                         @Param("amount") BigDecimal amount,
                         @Param("goodsId") String goodsId);
}
