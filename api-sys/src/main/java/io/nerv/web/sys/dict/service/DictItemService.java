package io.nerv.web.sys.dict.service;

import cn.hutool.core.lang.Dict;
import io.nerv.core.mvc.service.BaseService;
import io.nerv.web.sys.dict.entity.DictEntity;
import io.nerv.web.sys.dict.entity.DictItemEntity;
import io.nerv.web.sys.dict.helper.DictHelperProvider;
import io.nerv.web.sys.dict.mapper.DictItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 字典子表 - 字典项管理service
 * @author: S.PKAQ
 * @Datetime: 2018/3/15 8:09
 */
@Service
public class DictItemService extends BaseService<DictItemMapper, DictItemEntity> {
    @Autowired
    private DictHelperProvider dictHelper;
    @Autowired
    private DictService dictService;
    /**
     * 删除一条字典
     * @param id 字典ID
     */
    public void delDictItem(String id){
        this.mapper.deleteById(id);

        //删掉字典项之后，移除字典缓存中的相关字典项
        DictItemEntity dictItemEntity=this.mapper.selectById(id);
        if( dictItemEntity != null ){
            DictEntity dict=dictService.getById(dictItemEntity.getMainId());
            if(dict != null){
                dictHelper.remove(dict.getCode(),dictItemEntity.getKeyName());
            }
        }
    }

    /**
     * 新增/编辑一条字典项
     * @param dictItemEntity
     * @return
     */
    public void editDictItem(DictItemEntity dictItemEntity) {
        //根据id获取数据库中的字典项
        DictItemEntity dictItem=this.mapper.selectById(dictItemEntity.getId());

        this.merge(dictItemEntity);

        // 修改/保存 字典项后，更新字典缓存中的相关字典项
            DictEntity dict=dictService.getById(dictItem.getMainId());
           if(dict != null){
                //如果字典缓存中没有该字典code对应的字典项，则更新进去
                if(dictHelper.get(dict.getCode()) == null){
                    Map<String,String> itemMap=new HashMap<>(1);
                    itemMap.put(dictItemEntity.getKeyName(),dictItemEntity.getKeyValue());
                    dictHelper.add(dict.getCode(),itemMap);
                }else{
                    dictHelper.update(dict.getCode(),dictItemEntity.getKeyName(),dictItemEntity.getKeyValue());
                }
            }

            if( dictItem != null ){
                //如果修改了keyname 则把原来的keyname删掉
                if(!dictItem.getKeyName().equals(dictItemEntity.getKeyName())){
                    dictHelper.remove(dict.getCode(),dictItem.getKeyName());
                }
        }

    }
}
