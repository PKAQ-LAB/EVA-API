package io.nerv.web.sys.dict.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.service.BaseService;
import io.nerv.web.sys.dict.entity.DictEntity;
import io.nerv.web.sys.dict.entity.DictViewEntity;
import io.nerv.web.sys.dict.helper.DictHelperProvider;
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
    private DictHelperProvider dictHelper;

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

        //删掉字典之后，移除字典缓存中的相关字典
        DictEntity dictEntity=this.mapper.getDict(id);
        if(dictEntity != null) {
            dictHelper.remove(dictEntity.getCode());
        }
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
                //可能是修改字典对象的code属性，所以根据id查原始的code
                String code=this.mapper.selectById(id).getCode();

                this.mapper.updateById(dictEntity);


                //修改了字典的code则把原来的删掉加上最新的
                if(!code.equals(dictEntity.getCode())){
                    dictHelper.add(dictEntity.getCode(),dictHelper.get(code));
                    dictHelper.remove(code);
                }
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
