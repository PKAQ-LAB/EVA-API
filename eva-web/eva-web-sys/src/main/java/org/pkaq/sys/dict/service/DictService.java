package org.pkaq.sys.dict.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.enums.BizCodeEnum;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.covernt.DictConvert;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.mapper.DictItemMapper;
import org.pkaq.sys.dict.mapper.DictMapper;
import org.pkaq.sys.dict.mapper.DictViewMapper;
import org.pkaq.sys.dict.vo.DictViewVo;
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

    private final DictConvert dictConvert;

    /**
     * 初始化字典数据缓存
     */

    public void init() {
        var dictMap = this.selectDict();
        dictMap.forEach(dictCacheHelper::cachePut);
    }

    /**
     * 查询字典
     *
     * @return
     */
    public Map<String, LinkedHashMap<String, String>> selectDict() {
        List<DictViewVo> dictList = this.dictViewMapper.selectList(null);

        return dictList.stream()
                .collect(Collectors.groupingBy(DictViewVo::getCode,
                        LinkedHashMap::new,
                        Collectors.toMap(DictViewVo::getDCode,
                                DictViewVo::getDValue,
                                (o, n) -> n,
                                LinkedHashMap::new)));
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
     * @param dictAoeBo 字典对象
     */
    public void edit(DictAoeBo dictAoeBo) {
        String id = dictAoeBo.getId();
        // 校验code唯一性
        DictEntity conditionEntity =
                new LambdaQueryWrapper<DictEntity>().eq(DictEntity::getCode, dictAoeBo.getCode()).getEntity();

        if (CharSequenceUtil.isBlank(id)) {
            BizCodeEnum.CODE_EXIST.assertNotNull("字典");
            // 保存主表
            String mainID = IdWorker.getId() + "";
            dictAoeBo.setId(mainID);
            this.mapper.insert(dictConvert.boToEntity(dictAoeBo));
            // 保存子表
            if (CollUtil.isNotEmpty(dictAoeBo.getLines())) {
                dictAoeBo.getLines().forEach(item -> {
                    item.setMainId(mainID);
                    dictItemMapper.insert(item);
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
                        dictItemMapper.insert(item);
                    });
                }

                //修改了字典的code则把原来的删掉加上最新的
                if (!code.equals(dictAoeBo.getCode())) {
                    dictCacheHelper.add(dictAoeBo.getCode(), dictCacheHelper.get(code));
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
        dictMap.forEach(dictCacheHelper::cachePut);
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
