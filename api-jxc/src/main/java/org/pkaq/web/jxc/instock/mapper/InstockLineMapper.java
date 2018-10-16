package org.pkaq.web.jxc.instock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.web.jxc.instock.entity.InstockLineEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 采购入库单明细
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:57
 */
@Mapper
@Repository
public interface InstockLineMapper extends BaseMapper<InstockLineEntity> {
    List<InstockLineEntity> listByMainId(String mainId);
}
