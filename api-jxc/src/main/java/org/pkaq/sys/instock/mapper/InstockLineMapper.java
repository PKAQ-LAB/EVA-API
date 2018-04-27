package org.pkaq.sys.instock.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.sys.instock.entity.InstockLineEntity;
import org.springframework.stereotype.Repository;

/**
 * 采购入库单明细
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:57
 */
@Mapper
@Repository
public interface InstockLineMapper extends BaseMapper<InstockLineEntity> {
}
