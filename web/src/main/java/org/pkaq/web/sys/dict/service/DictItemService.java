package org.pkaq.web.sys.dict.service;

import org.pkaq.core.mvc.BaseService;
import org.pkaq.web.sys.dict.entity.DictEntity;
import org.pkaq.web.sys.dict.entity.DictItemEntity;
import org.pkaq.web.sys.dict.mapper.DictItemMapper;
import org.pkaq.web.sys.dict.mapper.DictMapper;

import java.util.List;

/**
 * 字典子表 - 字典项管理service
 * @author: S.PKAQ
 * @Datetime: 2018/3/15 8:09
 */
public class DictItemService extends BaseService<DictItemMapper, DictItemEntity> {
    /**
     * 根据ID获取一条字典
     * @return DictItemEntity
     */
    public DictItemEntity getDictItem(String id){
        return null;
    }

    /**
     * 查询所有字典
     * @return List<DictItemEntity>
     */
    public List<DictItemEntity> listDictItem(){
        return null;
    }

    /**
     * 删除一条字典
     * @param id 字典ID
     */
    public void delDictItem(String id){
        // 级联删除
    }


    /**
     * 编辑一条字典
     * @param dictItemEntity 字典对象
     * @return DictItemEntity
     */
    public DictItemEntity edit(DictItemEntity dictItemEntity){
        return null;
    }
}
