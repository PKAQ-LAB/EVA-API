package org.pkaq.sys.dict.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.convert.DictConvert;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.entity.DictViewEntity;
import org.pkaq.sys.dict.mapper.DictItemMapper;
import org.pkaq.sys.dict.mapper.DictMapper;
import org.pkaq.sys.dict.mapper.DictViewMapper;
import org.pkaq.sys.dict.vo.DictViewVo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class DictService extends StdService<DictMapper, DictEntity, DictConvert> implements IDictService {
    private final DictCacheHelper dictCacheHelper;

    private final DictViewMapper dictViewMapper;

    private final DictItemMapper dictItemMapper;

    private final DictConvert dictConvert;

    /**
     * 初始化字典数据缓存
     */
    @Override
    @CacheEvict(cacheNames = CommonConstant.CACHE_DICTDATA, key = CommonConstant.SYS_ALL_DICT_KEY)
    public void init() {
        var dictMap = this.selectDict();
        dictMap.forEach(dictCacheHelper::cachePut);
    }

    /**
     * 查询字典
     * @return
     */
    @Override
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询字典")
    public Map<String, LinkedHashMap<String, String>> selectDict() {
        List<DictViewEntity> dictList = this.dictViewMapper.selectList(null);

        return dictList.stream()
                .collect(Collectors.groupingBy(DictViewEntity::getCode,
                        LinkedHashMap::new,
                        Collectors.toMap(DictViewEntity::getDCode,
                                DictViewEntity::getDValue,
                                (o, n) -> n,
                                LinkedHashMap::new)));
    }

    @Override
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询字典")
    public Map<?, ?> fetchDicts() {
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
    @Override
    @BizLog(operateType = BizLogCodes.QUERY, description = "根据条件获取一条字典", args="{#bo}")
    public DictViewVo getDict(DictAoeBo bo) {
         return dictConvert.entityToVo(this.mapper.getDict(bo.getId()));
    }

    /**
     * 查询所有字典
     *
     * @return List<DictViewVo>
     */
    @Override
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询所有字典")
    public List<DictViewVo> listDict() {
         return dictConvert.toVoList(this.mapper.listDict());
    }

    /**
     * 删除一条字典 逻辑删除
     *
     * @param id 字典ID
     */
    @Override
    @BizLog(operateType = BizLogCodes.DELETE, description = "删除字典[{0}]", args = {"param:0"})
    @Transactional
    public void delDict(Long id) {

        // 先删除子表 再删除主表
        QueryWrapper<DictItemEntity> deleteWrapper = new QueryWrapper<>();
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
     *da
     * @param dictAoeBo 字典对象
     */
    @Override
    @BizLog(operateType = BizLogCodes.EDIT, description = "编辑了字典", args = {"#dictAoeBo"})
    @Transactional
    public void edit(DictAoeBo dictAoeBo) {
        Long id = dictAoeBo.getId();
        // 校验code唯一性
        DictEntity conditionEntity =
                new LambdaQueryWrapper<DictEntity>().eq(DictEntity::getCode, dictAoeBo.getCode()).getEntity();

        if (null == id || 0 == id) {
            // 保存主表
            long mainID = IdWorker.getId();
            dictAoeBo.setId(mainID);
            this.mapper.insert(dictConvert.boToEntity(dictAoeBo));
            // 保存子表
            if (CollUtil.isNotEmpty(dictAoeBo.getLines())) {
                dictAoeBo.getLines().forEach(item -> {
                    item.setMainId(mainID);
                    dictItemMapper.insert(dictConvert.boToItemEntity(item));
                });
            }
        } else {

            if (null == conditionEntity || id.equals(conditionEntity.getId())) {
                //可能是修改字典对象的code属性，所以根据id查原始的code
                String code = this.mapper.selectById(id).getCode();

                this.mapper.updateById(dictConvert.boToEntity(dictAoeBo));

                // 更新子表， 先删除再插入
                QueryWrapper<DictItemEntity> deleteWrapper = new QueryWrapper<>();
                deleteWrapper.eq("MAIN_ID", id);
                this.dictItemMapper.delete(deleteWrapper);

                if (CollUtil.isNotEmpty(dictAoeBo.getLines())) {
                    dictAoeBo.getLines().forEach(item -> {
                        item.setMainId(id);
                        dictItemMapper.insert(dictConvert.boToItemEntity(item));
                    });
                }

                //修改了字典的code则把原来的删掉加上最新的
                if (!code.equals(dictAoeBo.getCode())) {
                    dictCacheHelper.add(dictAoeBo.getCode(), dictCacheHelper.get(code));
                    dictCacheHelper.remove(code);
                }
            } else {
                SysCodes.DICT_CODE_EXISTS.newException();
            }
        }
    }

    /**
     * 校验编码是否存在
     *
     * @param bo
     * @return
     */
    @Override
    public boolean checkUnique(DictAoeBo bo) {
        long records = this.mapper.selectCount(new QueryWrapper<>(dictConvert.boToEntity(bo)));
        return records > 0;
    }


    /**
     * 根据传入的内容重新初始化字典
     *
     * @param dictMap
     */

    @Override
    public void init(Map<String, LinkedHashMap<String, String>> dictMap) {
        dictMap.forEach(dictCacheHelper::cachePut);
    }

    @Override
    public void reload() {
        dictCacheHelper.removeAll();
        this.init();
    }


    @Override
    public void reload(Map<String, LinkedHashMap<String, String>> dictMap) {
        dictCacheHelper.removeAll();
        this.init(dictMap);
    }
}
