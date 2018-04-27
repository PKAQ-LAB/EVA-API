package org.pkaq.sys.dict.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.pkaq.core.exception.ParamException;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.mapper.DictMapper;
import org.springframework.stereotype.Service;

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
        DictEntity conditionEntity = new DictEntity();
        conditionEntity.setCode(dictEntity.getCode());
        conditionEntity = this.mapper.selectOne(conditionEntity);

        if (StrUtil.isBlank(id)){
            if (null != conditionEntity){
                throw new ParamException("编码已存在");
            } else {
                this.mapper.insert(dictEntity);
            }
        } else {
            if (id.equals(conditionEntity.getId())){
                this.mapper.updateById(dictEntity);
            } else {
                throw new ParamException("编码已存在");
            }
        }
    }

    /**
     * 校验编码是否存在
     * @param dictEntity
     * @return
     */
    public boolean checkUnique(DictEntity dictEntity) {
        int records = this.mapper.selectCount(new EntityWrapper<>(dictEntity));
        return records>0;
    }
}
