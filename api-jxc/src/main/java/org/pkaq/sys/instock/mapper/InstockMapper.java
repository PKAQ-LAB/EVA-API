package org.pkaq.sys.instock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.sys.instock.entity.InstockEntity;
import org.springframework.stereotype.Repository;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:57
 */
@Mapper
@Repository
public interface InstockMapper extends BaseMapper<InstockEntity> {
}
