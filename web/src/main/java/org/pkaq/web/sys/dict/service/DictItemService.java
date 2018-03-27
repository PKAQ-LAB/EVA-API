package org.pkaq.web.sys.dict.service;

import cn.hutool.core.util.StrUtil;
import org.pkaq.core.mvc.BaseService;
import org.pkaq.web.sys.dict.entity.DictEntity;
import org.pkaq.web.sys.dict.entity.DictItemEntity;
import org.pkaq.web.sys.dict.mapper.DictItemMapper;
import org.pkaq.web.sys.dict.mapper.DictMapper;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;

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
