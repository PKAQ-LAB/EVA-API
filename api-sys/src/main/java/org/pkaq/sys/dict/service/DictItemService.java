package org.pkaq.sys.dict.service;

import org.pkaq.core.mvc.service.BaseActiveRecordService;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.mapper.DictItemMapper;
import org.springframework.stereotype.Service;

/**
 * 字典子表 - 字典项管理service
 * @author: S.PKAQ
 * @Datetime: 2018/3/15 8:09
 */
@Service
public class DictItemService extends BaseActiveRecordService<DictItemMapper, DictItemEntity> {
    /**
     * 删除一条字典
     * @param id 字典ID
     */
    public void delDictItem(String id){
        this.mapper.deleteById(id);
    }

    /**
     * 新增/编辑一条字典项
     * @param dictItemEntity
     * @return
     */
    public void editDictItem(DictItemEntity dictItemEntity) {
        dictItemEntity.insertOrUpdate();
    }
}
