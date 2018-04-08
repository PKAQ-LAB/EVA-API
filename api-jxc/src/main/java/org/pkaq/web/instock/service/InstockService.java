package org.pkaq.web.instock.service;

import org.pkaq.core.mvc.service.BaseActiveRecordService;
import org.pkaq.web.instock.entity.InstockEntity;
import org.pkaq.web.instock.mapper.InstockMapper;
import org.springframework.stereotype.Service;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:59
 */
@Service
public class InstockService extends BaseActiveRecordService<InstockMapper, InstockEntity>{
    /**
     * 新增/编辑采购入库单
     * @param instock
     */
    public void save(InstockEntity instock) {
        this.merge(instock, instock.getLine());
    }
}
