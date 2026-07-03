package org.pkaq.sys.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.util.CollUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.bo.DictLineBo;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.mapper.DictItemMapper;
import org.pkaq.sys.dict.mapper.DictMapper;
import org.pkaq.sys.dict.vo.DictLineVo;
import org.pkaq.sys.dict.vo.DictViewVo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 单级字典管理服务
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class DictService extends StdService<DictMapper, DictEntity> implements IDictService {
    private final DictCacheHelper dictCacheHelper;
    private final DictItemMapper dictItemMapper;

    /**
     * 初始化字典缓存。
     */
    @Override
    @CacheEvict(cacheNames = CommonConstant.CACHE_DICTDATA, key = CommonConstant.SYS_ALL_DICT_KEY)
    public void init() {
        this.dictCacheHelper.removeAll();
        this.selectDict().forEach(this.dictCacheHelper::cachePut);
    }

    /**
     * 查询全部可用字典缓存数据。
     *
     * @return 字典编码到明细键值的映射
     */
    @Override
    public Map<String, LinkedHashMap<String, String>> selectDict() {
        List<DictEntity> dicts = this.mapper.selectList(new LambdaQueryWrapper<DictEntity>()
                .ne(DictEntity::getFrozen, FrozenEnumm.FROZEN.getCode())
                .orderByAsc(DictEntity::getSort)
                .orderByAsc(DictEntity::getId));
        if (CollUtils.isEmpty(dicts)) {
            return Collections.emptyMap();
        }

        Map<Long, String> dictCodeMap = dicts.stream()
                .collect(Collectors.toMap(DictEntity::getId, DictEntity::getCode, (oldValue, newValue) -> newValue,
                        LinkedHashMap::new));
        List<DictItemEntity> items = this.dictItemMapper.selectList(new LambdaQueryWrapper<DictItemEntity>()
                .in(DictItemEntity::getMainId, dictCodeMap.keySet())
                .ne(DictItemEntity::getFrozen, FrozenEnumm.FROZEN.getCode())
                .orderByAsc(DictItemEntity::getMainId)
                .orderByAsc(DictItemEntity::getSort)
                .orderByAsc(DictItemEntity::getId));

        Map<String, LinkedHashMap<String, String>> result = new LinkedHashMap<>();
        for (DictEntity dict : dicts) {
            result.put(dict.getCode(), new LinkedHashMap<>());
        }
        for (DictItemEntity item : items) {
            String dictCode = dictCodeMap.get(item.getMainId());
            if (StrUtils.isBlank(dictCode)) {
                continue;
            }
            result.get(dictCode).put(item.getDCode(), item.getDValue());
        }
        return result;
    }

    /**
     * 从缓存获取全部字典。
     *
     * @return 字典缓存
     */
    @Override
    public Map<String, LinkedHashMap<String, String>> fetchDicts() {
        Map<?, ?> cached = this.dictCacheHelper.getAll();
        Map<String, LinkedHashMap<String, String>> result = new LinkedHashMap<>();
        if (cached != null && !cached.isEmpty()) {
            for (Object keyObj : cached.keySet()) {
                String key = String.valueOf(keyObj);
                Map<String, String> item = this.dictCacheHelper.get(key);
                if (item != null) {
                    result.put(key, new LinkedHashMap<>(item));
                }
            }
            if (!result.isEmpty()) {
                return result;
            }
        }

        result = this.selectDict();
        result.forEach(this.dictCacheHelper::cachePut);
        return result;
    }

    /**
     * 查询指定类型字典缓存。
     *
     * @param type 字典编码
     * @return 明细提交值和显示文本映射
     */
    @Override
    public Map<String, String> queryDict(String type) {
        if (StrUtils.isBlank(type)) {
            return Collections.emptyMap();
        }

        Map<String, String> cached = this.dictCacheHelper.get(type);
        if (cached != null) {
            return cached;
        }

        LinkedHashMap<String, String> items = this.selectDictByType(type);
        this.dictCacheHelper.cachePut(type, items);
        return items;
    }

    /**
     * 查询字典详情。
     *
     * @param bo 查询参数
     * @return 字典详情
     */
    @Override
    public DictViewVo getDict(DictAoeBo bo) {
        if (bo == null || (bo.getId() == null && StrUtils.isBlank(bo.getCode()))) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return null;
        }

        DictEntity entity;
        if (bo.getId() != null) {
            entity = this.mapper.selectById(bo.getId());
        } else {
            entity = this.mapper.selectOne(new LambdaQueryWrapper<DictEntity>().eq(DictEntity::getCode, bo.getCode()));
        }
        if (entity == null) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return null;
        }

        return this.toVo(entity, this.listItems(entity.getId()));
    }

    /**
     * 查询字典主表列表。
     *
     * @return 字典列表
     */
    @Override
    public List<DictViewVo> listDict() {
        List<DictEntity> entities = this.mapper.selectList(new LambdaQueryWrapper<DictEntity>()
                .orderByAsc(DictEntity::getSort)
                .orderByAsc(DictEntity::getId));
        if (CollUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }

        Map<Long, List<DictItemEntity>> itemMap = this.listItems(entities.stream()
                .map(DictEntity::getId)
                .collect(Collectors.toSet()));
        return entities.stream()
                .map(entity -> this.toVo(entity, itemMap.getOrDefault(entity.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    /**
     * 校验字典值是否属于指定字典。
     *
     * @param type 字典编码
     * @param value 字典提交值
     * @return true 表示存在且可用
     */
    @Override
    public boolean validateItem(String type, String value) {
        if (StrUtils.isBlank(type) || StrUtils.isBlank(value)) {
            return false;
        }
        DictEntity dict = this.mapper.selectOne(new LambdaQueryWrapper<DictEntity>()
                .eq(DictEntity::getCode, type)
                .ne(DictEntity::getFrozen, FrozenEnumm.FROZEN.getCode()));
        if (dict == null) {
            return false;
        }
        return this.dictItemMapper.selectCount(new LambdaQueryWrapper<DictItemEntity>()
                .eq(DictItemEntity::getMainId, dict.getId())
                .eq(DictItemEntity::getDCode, value)
                .ne(DictItemEntity::getFrozen, FrozenEnumm.FROZEN.getCode())) > 0;
    }

    /**
     * 删除字典。
     *
     * @param id 字典 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delDict(Long id) {
        if (id == null || id == 0L) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }

        DictEntity entity = this.mapper.selectById(id);
        if (entity == null) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }

        this.dictItemMapper.delete(new LambdaQueryWrapper<DictItemEntity>().eq(DictItemEntity::getMainId, id));
        this.mapper.deleteById(id);
        this.reloadTypesAfterCommit(Collections.singleton(entity.getCode()));
    }

    /**
     * 新增或编辑字典。
     *
     * @param bo 字典参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(DictAoeBo bo) {
        if (bo == null || StrUtils.isBlank(bo.getCode()) || StrUtils.isBlank(bo.getName())) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }
        this.ensureUniqueDictCode(bo.getId(), bo.getCode());
        this.ensureUniqueLineCode(bo.getLines());

        boolean isInsert = bo.getId() == null || bo.getId() == 0L;
        DictEntity oldEntity = null;
        if (!isInsert) {
            oldEntity = this.mapper.selectById(bo.getId());
            if (oldEntity == null) {
                SysCodes.RECORD_NOT_FOUND.newException();
                return;
            }
        }

        DictEntity entity = this.toEntity(bo, oldEntity);
        if (isInsert) {
            entity.setId(IdWorker.getId());
            this.mapper.insert(entity);
        } else {
            this.mapper.updateById(entity);
        }
        this.diffSaveItems(entity.getId(), bo.getLines());

        Set<String> affectedTypes = new HashSet<>();
        affectedTypes.add(entity.getCode());
        if (oldEntity != null) {
            affectedTypes.add(oldEntity.getCode());
        }
        this.reloadTypesAfterCommit(affectedTypes);
    }

    /**
     * 校验字典编码是否重复。
     *
     * @param bo 字典参数
     * @return true 表示重复
     */
    @Override
    public boolean checkUnique(DictAoeBo bo) {
        if (bo == null || StrUtils.isBlank(bo.getCode())) {
            return false;
        }
        return this.mapper.selectCount(new LambdaQueryWrapper<DictEntity>()
                .eq(DictEntity::getCode, bo.getCode())
                .ne(bo.getId() != null && bo.getId() != 0L, DictEntity::getId, bo.getId())) > 0;
    }

    /**
     * 初始化指定字典缓存。
     *
     * @param dictMap 字典缓存
     */
    @Override
    public void init(Map<String, LinkedHashMap<String, String>> dictMap) {
        dictMap.forEach(this.dictCacheHelper::cachePut);
    }

    /**
     * 重载全部字典缓存。
     */
    @Override
    public void reload() {
        this.init();
    }

    /**
     * 重载指定字典缓存。
     *
     * @param dictMap 字典缓存
     */
    @Override
    public void reload(Map<String, LinkedHashMap<String, String>> dictMap) {
        this.dictCacheHelper.removeAll();
        this.init(dictMap);
    }

    /**
     * 切换字典锁定状态。
     *
     * @param ids 字典 ID 集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchFrozen(SingleArray<Long> ids) {
        if (ids == null || CollUtils.isEmpty(ids.getParam())) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }

        Set<String> affectedTypes = new HashSet<>();
        for (Long id : this.sanitizeIds(ids.getParam())) {
            DictEntity entity = this.mapper.selectById(id);
            if (entity == null || entity.getFrozen() == FrozenEnumm.READ_ONLY) {
                continue;
            }
            FrozenEnumm target = entity.getFrozen() == FrozenEnumm.FROZEN
                    ? FrozenEnumm.UN_FROZEN
                    : FrozenEnumm.FROZEN;
            DictEntity update = new DictEntity();
            update.setId(id);
            update.setFrozen(target);
            this.mapper.updateById(update);
            affectedTypes.add(entity.getCode());
        }
        this.reloadTypesAfterCommit(affectedTypes);
    }

    private DictEntity toEntity(DictAoeBo bo, DictEntity oldEntity) {
        DictEntity entity = new DictEntity();
        entity.setId(bo.getId());
        entity.setRevision(bo.getRevision());
        entity.setFrozen(this.toFrozen(bo.getFrozen()));
        entity.setSort(bo.getSort());
        entity.setRemark(bo.getRemark());
        entity.setType(StrUtils.isBlank(bo.getType()) ? bo.getCode() : bo.getType());
        entity.setCode(bo.getCode());
        entity.setName(bo.getName());
        if (oldEntity != null && oldEntity.getFrozen() == FrozenEnumm.READ_ONLY) {
            entity.setFrozen(FrozenEnumm.READ_ONLY);
        }
        return entity;
    }

    private void diffSaveItems(Long mainId, List<DictLineBo> lines) {
        List<DictLineBo> safeLines = lines == null ? Collections.emptyList() : lines;
        Map<Long, DictItemEntity> oldItemMap = this.listItems(mainId).stream()
                .collect(Collectors.toMap(DictItemEntity::getId, Function.identity()));
        Set<Long> keepIds = new HashSet<>();
        double defaultSort = 1D;
        for (DictLineBo line : safeLines) {
            if (line == null || StrUtils.isBlank(line.getKeyName()) || StrUtils.isBlank(line.getKeyValue())) {
                continue;
            }
            DictItemEntity item = new DictItemEntity();
            item.setMainId(mainId);
            item.setDCode(line.getKeyName());
            item.setDValue(line.getKeyValue());
            item.setSort(line.getOrders() == null ? defaultSort : line.getOrders());
            item.setFrozen(this.toFrozen(line.getFrozen()));
            item.setRemark(line.getRemark());
            defaultSort++;
            if (line.getId() != null && oldItemMap.containsKey(line.getId())) {
                item.setId(line.getId());
                DictItemEntity oldItem = oldItemMap.get(line.getId());
                if (oldItem.getFrozen() == FrozenEnumm.READ_ONLY) {
                    item.setFrozen(FrozenEnumm.READ_ONLY);
                }
                this.dictItemMapper.updateById(item);
                keepIds.add(line.getId());
                continue;
            }
            item.setId(IdWorker.getId());
            this.dictItemMapper.insert(item);
            keepIds.add(item.getId());
        }

        for (Long oldId : oldItemMap.keySet()) {
            if (!keepIds.contains(oldId)) {
                this.dictItemMapper.deleteById(oldId);
            }
        }
    }

    private void ensureUniqueDictCode(Long id, String code) {
        if (this.checkUnique(this.buildUniqueBo(id, code))) {
            SysCodes.DICT_CODE_EXISTS.newException();
        }
    }

    private DictAoeBo buildUniqueBo(Long id, String code) {
        DictAoeBo bo = new DictAoeBo();
        bo.setId(id);
        bo.setCode(code);
        return bo;
    }

    private void ensureUniqueLineCode(List<DictLineBo> lines) {
        if (CollUtils.isEmpty(lines)) {
            return;
        }
        Set<String> codes = new HashSet<>();
        for (DictLineBo line : lines) {
            if (line == null || StrUtils.isBlank(line.getKeyName())) {
                continue;
            }
            if (!codes.add(line.getKeyName())) {
                SysCodes.DICT_CODE_EXISTS.newException();
            }
        }
    }

    private List<DictItemEntity> listItems(Long mainId) {
        if (mainId == null) {
            return Collections.emptyList();
        }
        return this.dictItemMapper.selectList(new LambdaQueryWrapper<DictItemEntity>()
                .eq(DictItemEntity::getMainId, mainId)
                .orderByAsc(DictItemEntity::getSort)
                .orderByAsc(DictItemEntity::getId));
    }

    private Map<Long, List<DictItemEntity>> listItems(Set<Long> mainIds) {
        if (CollUtils.isEmpty(mainIds)) {
            return Collections.emptyMap();
        }
        return this.dictItemMapper.selectList(new LambdaQueryWrapper<DictItemEntity>()
                        .in(DictItemEntity::getMainId, mainIds)
                        .orderByAsc(DictItemEntity::getMainId)
                        .orderByAsc(DictItemEntity::getSort)
                        .orderByAsc(DictItemEntity::getId))
                .stream()
                .collect(Collectors.groupingBy(DictItemEntity::getMainId, LinkedHashMap::new, Collectors.toList()));
    }

    private DictViewVo toVo(DictEntity entity, List<DictItemEntity> items) {
        DictViewVo vo = new DictViewVo();
        vo.setId(entity.getId());
        vo.setType(entity.getType());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setFrozen(entity.getFrozen() == null ? null : entity.getFrozen().getCode());
        vo.setSort(entity.getSort() == null ? 0D : entity.getSort());
        vo.setLines(items.stream().map(this::toLineVo).collect(Collectors.toCollection(ArrayList::new)));
        return vo;
    }

    private DictLineVo toLineVo(DictItemEntity entity) {
        DictLineVo vo = new DictLineVo();
        vo.setId(entity.getId());
        vo.setKeyName(entity.getDCode());
        vo.setKeyValue(entity.getDValue());
        vo.setOrders(entity.getSort());
        vo.setFrozen(entity.getFrozen() == null ? null : entity.getFrozen().getCode());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private LinkedHashMap<String, String> selectDictByType(String type) {
        DictEntity dict = this.mapper.selectOne(new LambdaQueryWrapper<DictEntity>()
                .eq(DictEntity::getCode, type)
                .ne(DictEntity::getFrozen, FrozenEnumm.FROZEN.getCode()));
        if (dict == null) {
            return new LinkedHashMap<>();
        }
        return this.listItems(dict.getId()).stream()
                .filter(item -> !Objects.equals(item.getFrozen(), FrozenEnumm.FROZEN))
                .collect(Collectors.toMap(DictItemEntity::getDCode,
                        DictItemEntity::getDValue,
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new));
    }

    private FrozenEnumm toFrozen(Integer frozen) {
        if (frozen == null) {
            return FrozenEnumm.UN_FROZEN;
        }
        if (FrozenEnumm.FROZEN.getCode().equals(frozen)) {
            return FrozenEnumm.FROZEN;
        }
        if (FrozenEnumm.READ_ONLY.getCode().equals(frozen)) {
            return FrozenEnumm.READ_ONLY;
        }
        return FrozenEnumm.UN_FROZEN;
    }

    private Set<Long> sanitizeIds(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return Collections.emptySet();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0L)
                .collect(Collectors.toSet());
    }

    private void reloadTypesAfterCommit(Set<String> types) {
        Set<String> safeTypes = types.stream()
                .filter(StrUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (safeTypes.isEmpty()) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            this.reloadTypes(safeTypes);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                DictService.this.reloadTypes(safeTypes);
            }
        });
    }

    private void reloadTypes(Set<String> types) {
        for (String type : types) {
            this.dictCacheHelper.remove(type);
            this.dictCacheHelper.cachePut(type, this.selectDictByType(type));
        }
    }
}
