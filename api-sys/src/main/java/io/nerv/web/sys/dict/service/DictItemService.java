package io.nerv.web.sys.dict.service;

import io.nerv.core.mvc.service.BaseService;
import io.nerv.web.sys.dict.entity.DictItemEntity;
import io.nerv.web.sys.dict.mapper.DictItemMapper;
import org.springframework.stereotype.Service;

/**
 * 字典子表 - 字典项管理service
 * @author: S.PKAQ
 * @Datetime: 2018/3/15 8:09
 */
@Service
public class DictItemService extends BaseService<DictItemMapper, DictItemEntity> {
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
        this.merge(dictItemEntity);
    }
}
