package org.pkaq.web.sys.dict.service;

import org.pkaq.core.mvc.BaseService;
import org.pkaq.web.sys.dict.entity.DictEntity;
import org.pkaq.web.sys.dict.mapper.DictMapper;

import java.util.List;

/**
 * 字典管理service
 * @author: S.PKAQ
 * @Datetime: 2018/3/15 8:09
 */
public class DictService extends BaseService<DictMapper, DictEntity> {
    /**
     * 根据ID获取一条字典
     * @return DictEntity
     */
    public DictEntity getDict(String id){
        return null;
    }

    /**
     * 查询所有字典
     * @return List<DictEntity>
     */
    public List<DictEntity> listDict(){
        return null;
    }

    /**
     * 删除一条字典
     * @param id 字典ID
     */
    public void delDict(String id){
        // 级联删除
    }

    /**
     * 删除一条字典项
     */
    public void delDictItem() {

    }

    /**
     * 编辑一条字典
     * @param dictEntity 字典对象
     * @return DictEntity
     */
    public DictEntity edit(DictEntity dictEntity){
        return null;
    }
}
