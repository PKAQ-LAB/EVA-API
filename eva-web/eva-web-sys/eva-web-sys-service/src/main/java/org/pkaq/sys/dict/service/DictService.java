package org.pkaq.sys.dict.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.mapper.DictMapper;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字典管理服务
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class DictService extends StdService<DictMapper, DictEntity> implements IDictService {
    private static final Long ROOT_PID = 0L;
    private static final String TREE_CACHE_PREFIX = "tree:";

    private final DictCacheHelper dictCacheHelper;

    /**
     * 初始化字典缓存。
     */
    @Override
    @CacheEvict(cacheNames = CommonConstant.CACHE_DICTDATA, key = CommonConstant.SYS_ALL_DICT_KEY)
    public void init() {
        this.dictCacheHelper.removeAll();
        Map<String, LinkedHashMap<String, String>> dictMap = this.selectDict();
        dictMap.forEach(this.dictCacheHelper::cachePut);
        this.preloadTreeCache();
    }

    /**
     * 查询可缓存的叶子字典数据。
     *
     * @return 字典缓存结构
     */
    @Override
    public Map<String, LinkedHashMap<String, String>> selectDict() {
        List<DictEntity> leaves = this.mapper.selectList(new LambdaQueryWrapper<DictEntity>()
                .eq(DictEntity::getIsleaf, true)
                .isNotNull(DictEntity::getType)
                .ne(DictEntity::getFrozen, FrozenEnumm.FROZEN.getCode())
                .orderByAsc(DictEntity::getType)
                .orderByAsc(DictEntity::getSort)
                .orderByAsc(DictEntity::getId));

        return leaves.stream()
                .collect(Collectors.groupingBy(DictEntity::getType,
                        LinkedHashMap::new,
                        Collectors.toMap(this::cacheKey,
                                DictEntity::getName,
                                (oldValue, newValue) -> newValue,
                                LinkedHashMap::new)));
    }

    /**
     * 从缓存获取全部字典。
     *
     * @return 字典缓存
     */
    @Override
    public Map<String, LinkedHashMap<String, String>> fetchDicts() {
        Map<?, ?> cached = this.dictCacheHelper.getAll();
        Map<String, LinkedHashMap<String, String>> ret = new LinkedHashMap<>();
        if (cached != null && !cached.isEmpty()) {
            for (Object keyObj : cached.keySet()) {
                String key = String.valueOf(keyObj);
                if (key.startsWith(TREE_CACHE_PREFIX)) {
                    continue;
                }
                Map<String, String> item = this.dictCacheHelper.get(key);
                if (item != null) {
                    ret.put(key, new LinkedHashMap<>(item));
                }
            }
            if (!ret.isEmpty()) {
                return ret;
            }
        }

        ret = this.selectDict();
        ret.forEach(this.dictCacheHelper::cachePut);
        this.preloadTreeCache();
        return ret;
    }

    /**
     * 查询指定类型的叶子字典缓存。
     *
     * @param type 字典类型
     * @return 叶子节点值和名称映射
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

        this.reloadType(type);
        cached = this.dictCacheHelper.get(type);
        return cached == null ? Collections.emptyMap() : cached;
    }

    /**
     * 查询字典节点详情。
     *
     * @param bo 查询参数
     * @return 字典节点详情
     */
    @Override
    public DictViewVo getDict(DictAoeBo bo) {
        if (bo == null || (bo.getId() == null && StrUtils.isBlank(bo.getCode()))) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return null;
        }

        LambdaQueryWrapper<DictEntity> wrapper = new LambdaQueryWrapper<>();
        if (bo.getId() != null) {
            wrapper.eq(DictEntity::getId, bo.getId());
        } else {
            String type = StrUtils.isBlank(bo.getType()) ? bo.getCode() : bo.getType();
            Long pid = bo.getPid() == null ? ROOT_PID : bo.getPid();
            wrapper.eq(DictEntity::getCode, bo.getCode());
            wrapper.eq(DictEntity::getType, type);
            wrapper.eq(DictEntity::getPid, pid);
        }
        DictEntity entity = this.mapper.selectOne(wrapper);
        if (entity == null) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return null;
        }

        return this.toVo(entity);
    }

    /**
     * 查询字典树。
     *
     * @return 字典树
     */
    @Override
    public List<DictViewVo> listDict() {
        List<DictEntity> entities = this.mapper.selectList(new LambdaQueryWrapper<DictEntity>()
                .orderByAsc(DictEntity::getSort)
                .orderByAsc(DictEntity::getId));
        return this.buildTree(entities);
    }

    /**
     * 查询指定类型的字典树。
     *
     * @param type 字典类型
     * @return 字典树
     */
    @Override
    public List<DictViewVo> listDictByType(String type) {
        if (StrUtils.isBlank(type)) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return Collections.emptyList();
        }

        List<DictViewVo> cached = this.dictCacheHelper.getObject(this.treeCacheKey(type),
                new TypeReference<List<DictViewVo>>() {
                });
        if (cached != null) {
            return cached;
        }

        List<DictEntity> entities = this.mapper.selectList(new LambdaQueryWrapper<DictEntity>()
                .eq(DictEntity::getType, type)
                .orderByAsc(DictEntity::getSort)
                .orderByAsc(DictEntity::getId));
        List<DictViewVo> tree = this.buildTree(entities);
        this.dictCacheHelper.cachePut(this.treeCacheKey(type), tree);
        return tree;
    }

    /**
     * 校验字典值是否为指定类型的可选叶子节点。
     *
     * @param type 字典类型
     * @param value 字典值
     * @return true 表示是可选叶子节点
     */
    @Override
    public boolean validateLeaf(String type, String value) {
        if (StrUtils.isBlank(type) || StrUtils.isBlank(value)) {
            return false;
        }
        return this.mapper.selectCount(new LambdaQueryWrapper<DictEntity>()
                .eq(DictEntity::getType, type)
                .eq(DictEntity::getValue, value)
                .eq(DictEntity::getIsleaf, true)
                .ne(DictEntity::getFrozen, FrozenEnumm.FROZEN.getCode())) > 0;
    }

    /**
     * 删除字典节点。
     *
     * @param id 字典节点ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delDict(Long id) {
        if (id == null || id == 0L) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }

        DictEntity dictEntity = this.mapper.selectById(id);
        if (dictEntity == null) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }

        Long childCount = this.mapper.selectCount(new LambdaQueryWrapper<DictEntity>().eq(DictEntity::getPid, id));
        if (childCount > 0) {
            SysCodes.DELETE_EXISTENCE_CHILD_NODE.newException();
        }

        String affectedType = dictEntity.getType();
        this.mapper.deleteById(id);
        this.refreshParentLeaf(dictEntity.getPid());
        this.reloadTypesAfterCommit(Collections.singleton(affectedType));
    }

    /**
     * 新增或编辑字典节点。
     *
     * @param dictAoeBo 字典节点参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(DictAoeBo dictAoeBo) {
        if (dictAoeBo == null) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }

        this.ensureCodeAndName(dictAoeBo);

        Long id = dictAoeBo.getId();
        boolean isInsert = id == null || id == 0L;
        DictEntity oldEntity = null;
        Set<String> affectedTypes = new HashSet<>();
        if (isInsert) {
            id = IdWorker.getId();
            dictAoeBo.setId(id);
        } else {
            oldEntity = this.mapper.selectById(id);
            if (oldEntity == null) {
                SysCodes.RECORD_NOT_FOUND.newException();
                return;
            }
            affectedTypes.add(oldEntity.getType());
        }

        DictEntity parent = this.ensureParentUsable(id, dictAoeBo.getPid());
        String type = this.resolveType(dictAoeBo, parent);
        Long parentId = parent == null ? ROOT_PID : parent.getId();
        this.ensureUniqueSibling(id, type, parentId, dictAoeBo.getCode());

        DictEntity entity = this.buildEntity(dictAoeBo, parent, type);
        affectedTypes.add(entity.getType());
        if (isInsert) {
            this.mapper.insert(entity);
            this.refreshParentLeaf(parentId);
        } else {
            this.mapper.updateById(entity);
            this.updateChildrenIfNeeded(oldEntity, entity);
            this.refreshParentLeaf(oldEntity.getPid());
            this.refreshParentLeaf(parentId);
        }

        this.reloadTypesAfterCommit(affectedTypes);
    }

    /**
     * 校验同级编码是否重复。
     *
     * @param bo 字典节点参数
     * @return true 表示重复
     */
    @Override
    public boolean checkUnique(DictAoeBo bo) {
        if (bo == null || StrUtils.isBlank(bo.getCode())) {
            return false;
        }

        DictEntity parent = null;
        if (bo.getPid() != null && !ROOT_PID.equals(bo.getPid())) {
            parent = this.mapper.selectById(bo.getPid());
        }
        String type = this.resolveType(bo, parent);
        Long parentId = parent == null ? ROOT_PID : parent.getId();
        return this.isDuplicateSibling(bo.getId(), type, parentId, bo.getCode());
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
     * 切换字典节点冻结状态，并级联到子节点。
     *
     * @param ids 字典节点ID集合
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
            DictEntity self = this.mapper.selectById(id);
            if (self == null || self.getFrozen() == FrozenEnumm.READ_ONLY) {
                continue;
            }
            affectedTypes.add(self.getType());

            FrozenEnumm target = self.getFrozen() == FrozenEnumm.FROZEN
                    ? FrozenEnumm.UN_FROZEN
                    : FrozenEnumm.FROZEN;
            Set<Long> subtreeIds = this.findSubtreeIds(self);
            if (!subtreeIds.isEmpty()) {
                this.mapper.update(null, new LambdaUpdateWrapper<DictEntity>()
                        .in(DictEntity::getId, subtreeIds)
                        .ne(DictEntity::getFrozen, FrozenEnumm.READ_ONLY.getCode())
                        .set(DictEntity::getFrozen, target));
            }
        }

        this.reloadTypesAfterCommit(affectedTypes);
    }

    private void ensureCodeAndName(DictAoeBo bo) {
        if (StrUtils.isBlank(bo.getCode()) || StrUtils.isBlank(bo.getName())) {
            SysCodes.RECORD_NOT_FOUND.newException();
        }
    }

    private void ensureUniqueSibling(Long id, String type, Long pid, String code) {
        if (this.isDuplicateSibling(id, type, pid, code)) {
            SysCodes.DICT_CODE_EXISTS.newException();
        }
    }

    private boolean isDuplicateSibling(Long id, String type, Long pid, String code) {
        LambdaQueryWrapper<DictEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictEntity::getType, type);
        wrapper.eq(DictEntity::getPid, pid);
        wrapper.eq(DictEntity::getCode, code);
        wrapper.ne(id != null && id != 0L, DictEntity::getId, id);
        return this.mapper.selectCount(wrapper) > 0;
    }

    private DictEntity ensureParentUsable(Long selfId, Long pid) {
        Long parentId = pid == null ? ROOT_PID : pid;
        if (ROOT_PID.equals(parentId)) {
            return null;
        }
        if (selfId != null && selfId.equals(parentId)) {
            SysCodes.NO_CHANGE_ORG.newException();
        }

        DictEntity parent = this.mapper.selectById(parentId);
        if (parent == null || parent.getFrozen() == FrozenEnumm.FROZEN) {
            SysCodes.RECORD_NOT_FOUND.newException();
        }
        if (selfId != null && parent.getPath() != null && parent.getPath().contains("/" + selfId + "/")) {
            SysCodes.NO_CHANGE_ORG.newException();
        }
        return parent;
    }

    private String resolveType(DictAoeBo bo, DictEntity parent) {
        if (parent != null) {
            return parent.getType();
        }
        if (StrUtils.isNotBlank(bo.getType())) {
            return bo.getType();
        }
        return bo.getCode();
    }

    private DictEntity buildEntity(DictAoeBo bo, DictEntity parent, String type) {
        DictEntity entity = new DictEntity();
        entity.setId(bo.getId());
        entity.setRevision(bo.getRevision());
        entity.setType(type);
        entity.setCode(bo.getCode());
        entity.setName(bo.getName());
        entity.setValue(StrUtils.isBlank(bo.getValue()) ? bo.getCode() : bo.getValue());
        entity.setRemark(bo.getRemark());
        entity.setSort(bo.getSort());
        entity.setFrozen(this.toFrozen(bo.getFrozen()));
        entity.setPid(parent == null ? ROOT_PID : parent.getId());
        entity.setPath(parent == null ? "/" + bo.getId() : parent.getPath() + "/" + bo.getId());
        entity.setIsleaf(!this.hasChildren(bo.getId()));
        return entity;
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

    private void updateChildrenIfNeeded(DictEntity oldEntity, DictEntity newEntity) {
        boolean pathChanged = StrUtils.isNotBlank(oldEntity.getPath())
                && !oldEntity.getPath().equals(newEntity.getPath());
        boolean typeChanged = StrUtils.isNotBlank(oldEntity.getType())
                && !oldEntity.getType().equals(newEntity.getType());
        if (!pathChanged && !typeChanged) {
            return;
        }

        List<DictEntity> children = this.mapper.selectList(new LambdaQueryWrapper<DictEntity>()
                .likeRight(DictEntity::getPath, oldEntity.getPath() + "/"));
        for (DictEntity child : children) {
            if (pathChanged) {
                child.setPath(newEntity.getPath() + child.getPath().substring(oldEntity.getPath().length()));
            }
            if (typeChanged) {
                child.setType(newEntity.getType());
            }
            this.mapper.updateById(child);
        }
    }

    private void refreshParentLeaf(Long parentId) {
        if (parentId == null || ROOT_PID.equals(parentId)) {
            return;
        }
        boolean hasChildren = this.hasChildren(parentId);
        DictEntity update = new DictEntity();
        update.setId(parentId);
        update.setIsleaf(!hasChildren);
        this.mapper.updateById(update);
    }

    private boolean hasChildren(Long id) {
        if (id == null || id == 0L) {
            return false;
        }
        return this.mapper.selectCount(new LambdaQueryWrapper<DictEntity>().eq(DictEntity::getPid, id)) > 0;
    }

    private List<DictViewVo> buildTree(List<DictEntity> entities) {
        if (CollUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }

        Map<Long, DictViewVo> voMap = new LinkedHashMap<>();
        for (DictEntity entity : entities) {
            voMap.put(entity.getId(), this.toVo(entity));
        }

        List<DictViewVo> roots = new ArrayList<>();
        for (DictViewVo vo : voMap.values()) {
            if (vo.getPid() == null || ROOT_PID.equals(vo.getPid()) || !voMap.containsKey(vo.getPid())) {
                roots.add(vo);
            } else {
                voMap.get(vo.getPid()).getChildren().add(vo);
            }
        }
        return roots;
    }

    private DictViewVo toVo(DictEntity entity) {
        DictViewVo vo = new DictViewVo();
        vo.setId(entity.getId());
        vo.setType(entity.getType());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setValue(entity.getValue());
        vo.setPid(entity.getPid());
        vo.setPath(entity.getPath());
        vo.setIsleaf(entity.getIsleaf());
        vo.setFrozen(entity.getFrozen() == null ? null : entity.getFrozen().getCode());
        vo.setSort(entity.getSort());
        vo.setSelectable(Boolean.TRUE.equals(entity.getIsleaf()) && entity.getFrozen() != FrozenEnumm.FROZEN);
        return vo;
    }

    private Set<Long> findSubtreeIds(DictEntity root) {
        Set<Long> ids = new HashSet<>();
        ids.add(root.getId());
        if (StrUtils.isNotBlank(root.getPath())) {
            this.mapper.selectList(new LambdaQueryWrapper<DictEntity>()
                            .select(DictEntity::getId)
                            .likeRight(DictEntity::getPath, root.getPath() + "/"))
                    .forEach(child -> ids.add(child.getId()));
        }
        return ids;
    }

    private Set<Long> sanitizeIds(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return Collections.emptySet();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0L)
                .collect(Collectors.toSet());
    }

    private String cacheKey(DictEntity entity) {
        return StrUtils.isBlank(entity.getValue()) ? entity.getCode() : entity.getValue();
    }

    private void preloadTreeCache() {
        List<DictEntity> entities = this.mapper.selectList(new LambdaQueryWrapper<DictEntity>()
                .isNotNull(DictEntity::getType)
                .orderByAsc(DictEntity::getType)
                .orderByAsc(DictEntity::getSort)
                .orderByAsc(DictEntity::getId));
        Map<String, List<DictEntity>> typeMap = entities.stream()
                .collect(Collectors.groupingBy(DictEntity::getType, LinkedHashMap::new, Collectors.toList()));
        typeMap.forEach((type, typeEntities) ->
                this.dictCacheHelper.cachePut(this.treeCacheKey(type), this.buildTree(typeEntities)));
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
            this.reloadType(type);
        }
    }

    private void reloadType(String type) {
        if (StrUtils.isBlank(type)) {
            return;
        }

        this.dictCacheHelper.remove(type);
        this.dictCacheHelper.remove(this.treeCacheKey(type));
        this.dictCacheHelper.cachePut(type, this.selectDictByType(type));
        this.dictCacheHelper.cachePut(this.treeCacheKey(type), this.loadTreeByType(type));
    }

    private LinkedHashMap<String, String> selectDictByType(String type) {
        List<DictEntity> leaves = this.mapper.selectList(new LambdaQueryWrapper<DictEntity>()
                .eq(DictEntity::getType, type)
                .eq(DictEntity::getIsleaf, true)
                .ne(DictEntity::getFrozen, FrozenEnumm.FROZEN.getCode())
                .orderByAsc(DictEntity::getSort)
                .orderByAsc(DictEntity::getId));
        return leaves.stream()
                .collect(Collectors.toMap(this::cacheKey,
                        DictEntity::getName,
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new));
    }

    private List<DictViewVo> loadTreeByType(String type) {
        List<DictEntity> entities = this.mapper.selectList(new LambdaQueryWrapper<DictEntity>()
                .eq(DictEntity::getType, type)
                .orderByAsc(DictEntity::getSort)
                .orderByAsc(DictEntity::getId));
        return this.buildTree(entities);
    }

    private String treeCacheKey(String type) {
        return TREE_CACHE_PREFIX + type;
    }
}
