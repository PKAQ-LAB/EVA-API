package io.nerv.sys.dict.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.nerv.sys.dict.cache.DictCacheHelper;
import io.nerv.sys.dict.entity.DictEntity;
import io.nerv.sys.dict.entity.DictItemEntity;
import io.nerv.sys.dict.entity.DictViewEntity;
import io.nerv.sys.dict.mapper.DictItemMapper;
import io.nerv.sys.dict.mapper.DictMapper;
import io.nerv.sys.dict.mapper.DictViewMapper;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.BizException;
import io.nerv.core.mybatis.mvc.service.mybatis.StdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典管理service
 *
 * @author: S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class DictService extends StdService<DictMapper, DictEntity> {
    private final DictCacheHelper dictCacheHelper;

    private final DictViewMapper dictViewMapper;

    private final DictItemMapper dictItemMapper;

    /**
     * 初始化字典数据缓存
     */

    public void init() {
        var dictMap = this.selectDict();
        dictMap.forEach((k, v) ->
                dictCacheHelper.cachePut(k, v)
        );
    }

    /**
     * 查询字典
     *
     * @return
     */
    public Map<String, LinkedHashMap<String, String>> selectDict() {
        List<DictViewEntity> dictList = this.dictViewMapper.selectList(null);

        LinkedHashMap<String, LinkedHashMap<String, String>> cacheMap =
                dictList.stream()
                        .collect(Collectors.groupingBy(DictViewEntity::getCode,
                                LinkedHashMap::new,
                                Collectors.toMap(DictViewEntity::getKeyName,
                                        DictViewEntity::getKeyValue,
                                        (o, n) -> n,
                                        LinkedHashMap::new)));

        return cacheMap;
    }

    public Map fetchDicts() {
        var ret = dictCacheHelper.getAll();
        if (null == ret) {
            ret = selectDict();
        }
        return ret;
    }

    /**
     * 根据条件获取一条字典
     *
     * @return DictEntity
     */
    public DictEntity getDict(DictEntity dictEntity) {
        return this.mapper.getDict(dictEntity.getId());
    }

    /**
     * 查询所有字典
     *
     * @return List<DictEntity>
     */
    public List<DictEntity> listDict() {
        return this.mapper.listDict();
    }

    /**
     * 删除一条字典 逻辑删除
     *
     * @param id 字典ID
     */
    public void delDict(String id) {

        // 先删除子表 再删除主表
        QueryWrapper<DictItemEntity> deleteWrapper = new QueryWrapper();
        deleteWrapper.eq("MAIN_ID", id);
        this.dictItemMapper.delete(deleteWrapper);

        this.mapper.deleteById(id);

        //删掉字典之后，移除字典缓存中的相关字典
        DictEntity dictEntity = this.mapper.getDict(id);
        if (dictEntity != null) {
            dictCacheHelper.remove(dictEntity.getCode());
        }
    }

    /**
     * 编辑一条字典
     *
     * @param dictEntity 字典对象
     */
    public void edit(DictEntity dictEntity) {
        String id = dictEntity.getId();
        // 校验code唯一性
        DictEntity conditionEntity = new DictEntity();
        conditionEntity.setCode(dictEntity.getCode());
        conditionEntity = this.mapper.selectOne(new QueryWrapper<>(conditionEntity));

        if (StrUtil.isBlank(id)) {
            BizCodeEnum.CODE_EXIST.assertNotNull("字典");
            // 保存主表
            String mainID = IdWorker.getId() + "";
            dictEntity.setId(mainID);
            this.mapper.insert(dictEntity);
            // 保存子表
            if (CollUtil.isNotEmpty(dictEntity.getLines())) {
                dictEntity.getLines().forEach(item -> {
                    item.setMainId(mainID);
                    dictItemMapper.insert(item);
                });
            }
        } else {

            if (null == conditionEntity || id.equals(conditionEntity.getId())) {
                //可能是修改字典对象的code属性，所以根据id查原始的code
                String code = this.mapper.selectById(id).getCode();

                this.mapper.updateById(dictEntity);

                // 更新子表， 先删除再插入
                QueryWrapper<DictItemEntity> deleteWrapper = new QueryWrapper();
                deleteWrapper.eq("MAIN_ID", id);
                this.dictItemMapper.delete(deleteWrapper);

                if (CollUtil.isNotEmpty(dictEntity.getLines())) {
                    dictEntity.getLines().forEach(item -> {
                        item.setMainId(id);
                        dictItemMapper.insert(item);
                    });
                }

                //修改了字典的code则把原来的删掉加上最新的
                if (!code.equals(dictEntity.getCode())) {
                    dictCacheHelper.add(dictEntity.getCode(), dictCacheHelper.get(code));
                    dictCacheHelper.remove(code);
                }
            } else {
                throw new BizException(BizCodeEnum.CODE_EXIST, "字典");
            }
        }
    }

    /**
     * 校验编码是否存在
     *
     * @param dictEntity
     * @return
     */
    public boolean checkUnique(DictEntity dictEntity) {
        long records = this.mapper.selectCount(new QueryWrapper<>(dictEntity));
        return records > 0;
    }


    /**
     * 根据传入的内容重新初始化字典
     *
     * @param dictMap
     */

    public void init(Map<String, LinkedHashMap<String, String>> dictMap) {
        dictMap.forEach((k, v) ->
                dictCacheHelper.cachePut(k, v)
        );
    }

    public void reload() {
        dictCacheHelper.removeAll();
        this.init();
    }


    public void reload(Map<String, LinkedHashMap<String, String>> dictMap) {
        dictCacheHelper.removeAll();
        this.init(dictMap);
    }

}
