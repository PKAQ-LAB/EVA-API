package org.pkaq.web.sys.dict.service;

import cn.hutool.core.util.StrUtil;
import org.pkaq.core.mvc.BaseService;
import org.pkaq.web.sys.dict.entity.DictEntity;
import org.pkaq.web.sys.dict.mapper.DictMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典管理service
 * @author: S.PKAQ
 * @Datetime: 2018/3/15 8:09
 */
@Service
public class DictService extends BaseService<DictMapper, DictEntity> {

    /**
     * 根据条件获取一条字典
     * @return DictEntity
     */
    public DictEntity getDict(DictEntity dictEntity){
        return this.mapper.getDict(dictEntity.getId());
    }

    /**
     * 查询所有字典
     * @return List<DictEntity>
     */
    public List<DictEntity> listDict(){
        return this.mapper.listDict();
    }

    /**
     * 删除一条字典 逻辑删除
     * @param id 字典ID
     */
    public void delDict(String id){
        this.mapper.deleteDictById(id);
    }

    /**
     * 编辑一条字典
     * @param dictEntity 字典对象
     * @return DictEntity
     */
    public void edit(DictEntity dictEntity){
        String id = dictEntity.getId();
        // 校验code唯一性
        if (StrUtil.isBlank(id)){
            this.mapper.insert(dictEntity);
        } else {
            this.mapper.updateById(dictEntity);
        }
    }
}
