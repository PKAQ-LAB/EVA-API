package io.nerv.web.sys.dict.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.service.BaseService;
import io.nerv.web.sys.dict.entity.DictEntity;
import io.nerv.web.sys.dict.entity.DictViewEntity;
import io.nerv.web.sys.dict.mapper.DictMapper;
import io.nerv.web.sys.dict.mapper.DictViewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典管理service
 * @author: S.PKAQ
 * @Datetime: 2018/3/15 8:09
 */
@Service
public class DictService extends BaseService<DictMapper, DictEntity> {

    @Autowired
    private DictViewMapper dictViewMapper;

    /**
     * 查询字典缓存
     * @return
     */
    public Map<String, Map<String, String>> initDictCache(){
        List<DictViewEntity> dictList = this.dictViewMapper.selectList(null);

        Map<String, Map<String, String>> cacheMap = new HashMap<>();

        dictList.forEach( item -> {
            String code = item.getCode();

            String key = item.getKeyName();
            String value = item.getKeyValue();

            Map<String, String> itemMap = cacheMap.get(code);

            if(null == itemMap){
                Map<String, String> tempMap = new HashMap<>(1);
                tempMap.put(key, value);
                cacheMap.put(code, tempMap);
            } else {
                itemMap.put(key, value);
            }
        });
        return cacheMap;
    }
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
        conditionEntity = this.mapper.selectOne(new QueryWrapper<>(conditionEntity));

        if (StrUtil.isBlank(id)){
            if (null != conditionEntity){
                throw new ParamException("编码已存在");
            } else {
                this.mapper.insert(dictEntity);
            }
        } else {
            if (null == conditionEntity || id.equals(conditionEntity.getId())){
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
        int records = this.mapper.selectCount(new QueryWrapper<>(dictEntity));
        return records>0;
    }
}
