package org.pkaq.web.jxc.instock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.web.jxc.instock.entity.InstockEntity;
import org.pkaq.web.jxc.instock.vo.InstockVO;
import org.springframework.stereotype.Repository;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:57
 */
@Mapper
@Repository
public interface InstockMapper extends BaseMapper<InstockEntity> {
    IPage<InstockEntity> listInstock(Page pagination, InstockVO instockVO);
}
