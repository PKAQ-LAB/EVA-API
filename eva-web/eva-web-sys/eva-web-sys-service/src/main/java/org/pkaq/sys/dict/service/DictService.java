package org.pkaq.sys.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.tenant.TenantSchema;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;
import org.pkaq.sys.dict.cache.DictCacheHelper.Owner;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.CollUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.bo.DictLineBo;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.convert.DictConvert;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.mapper.DictItemMapper;
import org.pkaq.sys.dict.mapper.DictMapper;
import org.pkaq.sys.dict.vo.DictViewVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
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
    private final DictConvert convert;
    private final EvaConfig evaConfig;
    private final DictItemSnapshot itemSnapshot;
    private final DictBusinessAccess businessAccess;

    /**
     * 初始化字典缓存。
     */
    @Override
    @TenantSchema
    @Transactional(readOnly = true)
    public void init() {
        // 启动没有可信请求主体时保持冷缓存，不扫描租户或清理旧键。
        if (ThreadUserHelper.getCurrentUserOrNull() == null) {
            return;
        }
        this.warmFromDatabase();
    }

    /**
     * 查询全部可用字典缓存数据。
     *
     * @return 字典编码到明细键值的映射
     */
    @Override
    @TenantSchema
    @Transactional(readOnly = true)
    public Map<String, LinkedHashMap<String, String>> selectDict() {
        Map<String, LinkedHashMap<String, String>> result = new LinkedHashMap<>();
        inventory(null).forEach((owner, dicts) -> dicts.forEach(dict ->
                result.put(dict.getCode(), businessAccess.completeItems(owner, dict.getId()))));
        return result;
    }

    /**
     * 从缓存获取全部字典。
     *
     * @return 字典缓存
     */
    @Override
    @TenantSchema
    @Transactional(readOnly = true)
    public Map<String, LinkedHashMap<String, String>> fetchDicts() {
        Map<String, LinkedHashMap<String, String>> result = new LinkedHashMap<>();
        // 数据库清单先于任何缓存命中，部分预热不能冒充完整集合。
        inventory(null).forEach((owner, dicts) -> dicts.forEach(dict ->
                result.put(dict.getCode(), cachedValues(owner, dict))));
        return result;
    }

    /**
     * 查询指定类型字典缓存。
     *
     * @param type 字典编码
     * @return 明细提交值和显示文本映射
     */
    @Override
    @TenantSchema
    @Transactional(readOnly = true)
    public Map<String, String> queryDict(String type) {
        if (StrUtils.isBlank(type)) {
            return Collections.emptyMap();
        }
        Map<Owner, List<DictEntity>> matches = inventory(type);
        for (Map.Entry<Owner, List<DictEntity>> entry : matches.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                return cachedValues(entry.getKey(), entry.getValue().getFirst());
            }
        }
        return Collections.emptyMap();
    }

    /**
     * 查询字典详情。
     *
     * @param bo 查询参数
     * @return 字典详情
     */
    @Override
    @TenantSchema
    @Transactional(readOnly = true)
    public DictViewVo getDict(DictAoeBo bo) {
        boolean writable = businessAccess.businessWritable();
        businessAccess.validateCategory(bo == null ? null : bo.getCategory());
        if (bo == null || (bo.getId() == null && StrUtils.isBlank(bo.getCode()))) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return null;
        }
        List<DictViewVo> matches = new ArrayList<>();
        for (Owner owner : businessAccess.sources()) {
            if (bo.getCategory() != null && !owner.category().equals(bo.getCategory())) {
                continue;
            }
            businessAccess.inSource(owner, () -> {
                List<DictEntity> entities = mapper.selectVisible(owner.schema(), new LambdaQueryWrapper<DictEntity>()
                        .eq(DictEntity::getDeleted, 0).eq(DictEntity::getCategory, owner.category())
                        .eq(bo.getId() != null, DictEntity::getId, bo.getId())
                        .eq(bo.getId() == null, DictEntity::getCode, bo.getCode()));
                for (DictEntity entity : entities) {
                    DictViewVo view = toVo(entity, listItems(entity.getId(), owner.schema()));
                    view.setReadOnly(businessAccess.readOnly(entity, writable));
                    matches.add(view);
                }
                return null;
            });
        }
        if (matches.isEmpty()) {
            SysCodes.RECORD_NOT_FOUND.newException();
        }
        if (matches.size() != 1) {
            throw new IllegalArgumentException("字典来源不明确，必须指定category");
        }
        return matches.getFirst();
    }

    /**
     * 查询字典主表列表。
     *
     * @return 字典列表
     */
    @Override
    @TenantSchema
    @Transactional(readOnly = true)
    public List<DictViewVo> listDict() {
        return this.listWithCapability().data();
    }

    /** 同一次只读事务中构造空集合亦有效的维护能力及原平数组。 */
    @Transactional(readOnly = true)
    @TenantSchema
    public Listing listWithCapability() {
        boolean writable = businessAccess.businessWritable();
        businessAccess.requireClassifiedList();
        List<DictViewVo> views = new ArrayList<>();
        for (Owner owner : businessAccess.sources()) {
            businessAccess.inSource(owner, () -> {
                List<DictEntity> entities = mapper.selectVisible(owner.schema(), new LambdaQueryWrapper<DictEntity>()
                        .eq(DictEntity::getDeleted, 0).eq(DictEntity::getCategory, owner.category())
                        .orderByAsc(DictEntity::getSort).orderByAsc(DictEntity::getId));
                for (DictEntity entity : entities) {
                    DictViewVo view = toVo(entity, listItems(entity.getId(), owner.schema()));
                    view.setReadOnly(businessAccess.readOnly(entity, writable));
                    views.add(view);
                }
                return null;
            });
        }
        return new Listing(views, writable);
    }

    public record Listing(List<DictViewVo> data, boolean businessWritable) {
    }

    /**
     * 校验字典值是否属于指定字典。
     *
     * @param type 字典编码
     * @param value 字典提交值
     * @return true 表示存在且可用
     */
    @Override
    @TenantSchema
    @Transactional(readOnly = true)
    public boolean validateItem(String type, String value) {
        return StrUtils.isNotBlank(type) && StrUtils.isNotBlank(value) && queryDict(type).containsKey(value);
    }

    /**
     * 删除字典。
     *
     * @param id 字典 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @TenantSchema
    public void delDict(Long id) {
        this.delDict(id, "POST");
    }

    /** 方法由固定controller传入，不接收客户端method参数。 */
    @Transactional(rollbackFor = Exception.class)
    @TenantSchema
    public void delDict(Long id, String method) {
        if (!"GET".equals(method) && !"POST".equals(method)) {
            org.pkaq.core.codes.CommonCodes.PARAM_ERROR.newException();
        }
        businessAccess.requireWrite(method, "/sys/dictionary/del/" + id);
        if (id == null || id == 0L) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }

        DictItemSnapshot.Snapshot snapshot = this.itemSnapshot.load(id);
        DictEntity entity = snapshot.main();
        if (entity == null) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }
        ensureDictEditable(entity);

        if (snapshot.items().stream().anyMatch(item -> item.getFrozen() == FrozenEnumm.READ_ONLY)) {
            SysCodes.READ_ONLY_RECORD.newException();
        }

        for (DictItemEntity item : listItems(id)) {
            audit(item, false);
            requireChanged(dictItemMapper.deletePrivate(businessAccess.businessOwner().schema(), item, revision(item)));
        }
        audit(entity, false);
        requireChanged(mapper.deletePrivate(businessAccess.businessOwner().schema(),
                    entity, revision(entity)));
        this.reloadTypesAfterCommit(Collections.singleton(entity.getCode()));
    }

    /**
     * 新增或编辑字典。
     *
     * @param bo 字典参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @TenantSchema
    public void edit(DictAoeBo bo) {
        businessAccess.requireWrite("POST", "/sys/dictionary/edit");
        businessAccess.validateCategory(bo == null ? null : bo.getCategory());
        if (bo != null && bo.getCategory() != null && !DictBusinessAccess.BUSINESS.equals(bo.getCategory())) {
            org.pkaq.core.codes.CommonCodes.PERMISSION_DENY.newException();
        }
        if (bo == null || StrUtils.isBlank(bo.getCode()) || StrUtils.isBlank(bo.getName())) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }
        this.ensureUniqueDictCode(bo.getId(), bo.getCode());
        businessAccess.ensureCodeAvailable(bo.getId(), bo.getCode());
        this.ensureUniqueLineCode(bo.getLines());

        boolean isInsert = bo.getId() == null || bo.getId() == 0L;
        DictEntity oldEntity = null;
        List<DictItemEntity> oldItems = Collections.emptyList();
        if (!isInsert) {
            DictItemSnapshot.Snapshot snapshot = this.itemSnapshot.load(bo.getId());
            oldEntity = snapshot.main();
            // 普通行仍取原可见范围；完整性旁路只补入保护行，不能接纳外scope普通行ID。
            Map<Long, DictItemEntity> visibleItems = this.listItems(bo.getId()).stream()
                    .collect(Collectors.toMap(DictItemEntity::getId, Function.identity()));
            snapshot.items().forEach(item -> visibleItems.put(item.getId(), item));
            oldItems = List.copyOf(visibleItems.values());
            if (oldEntity == null) {
                SysCodes.RECORD_NOT_FOUND.newException();
                return;
            }
            ensureDictEditable(oldEntity);
            if (bo.getRevision() != null && bo.getRevision() != revision(oldEntity)) {
                throw new IllegalStateException("字典版本冲突");
            }
        }

        ensureWritableFrozen(bo.getFrozen());
        validateItems(bo.getLines(), oldItems);
        DictEntity entity = this.toEntity(bo, oldEntity);
        entity.setCategory(DictBusinessAccess.BUSINESS);
        audit(entity, isInsert);
        if (isInsert) {
            entity.setId(IdWorker.getId());
            requireChanged(this.mapper.insertPrivate(businessAccess.businessOwner().schema(), entity));
        } else {
            requireChanged(this.mapper.updatePrivate(businessAccess.businessOwner().schema(),
                    entity, revision(oldEntity)));
        }
        this.diffSaveItems(entity.getId(), bo.getLines(), oldItems);

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
    @TenantSchema
    @Transactional(readOnly = true)
    public boolean checkUnique(DictAoeBo bo) {
        if (bo == null || StrUtils.isBlank(bo.getCode())) {
            return false;
        }
        businessAccess.validateCategory(bo.getCategory());
        return businessAccess.codeExists(bo.getId(), bo.getCode());
    }

    /**
     * 初始化指定字典缓存。
     *
     * @param dictMap 字典缓存
     */
    @Override
    @TenantSchema
    @Transactional(readOnly = true)
    public void init(Map<String, LinkedHashMap<String, String>> dictMap) {
        // 外部Map不能绕过实际来源核验或写入共享标签缓存。
        this.warmFromDatabase();
    }

    /**
     * 重载全部字典缓存。
     */
    @Override
    @TenantSchema
    @Transactional(readOnly = true)
    public void reload() {
        this.init();
    }

    /**
     * 重载指定字典缓存。
     *
     * @param dictMap 字典缓存
     */
    @Override
    @TenantSchema
    @Transactional(readOnly = true)
    public void reload(Map<String, LinkedHashMap<String, String>> dictMap) {
        this.warmFromDatabase();
    }

    /**
     * 切换字典锁定状态。
     *
     * @param ids 字典 ID 集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @TenantSchema
    public void switchFrozen(SingleArray<Long> ids) {
        businessAccess.requireWrite("POST", "/sys/dictionary/switch");
        if (ids == null || CollUtils.isEmpty(ids.getParam())) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return;
        }

        if (ids.getParam().stream().anyMatch(id -> id == null || id <= 0L)) {
            SysCodes.RECORD_NOT_FOUND.newException();
        }
        Set<String> affectedTypes = new HashSet<>();
        List<DictEntity> targets = this.sanitizeIds(ids.getParam()).stream().map(id -> {
            List<DictEntity> found = this.mapper.selectVisible(businessAccess.businessOwner().schema(),
                    new LambdaQueryWrapper<DictEntity>()
                    .eq(DictEntity::getId, id).eq(DictEntity::getDeleted, 0)
                    .eq(DictEntity::getCategory, DictBusinessAccess.BUSINESS).last("FOR UPDATE"));
            DictEntity entity = found.isEmpty() ? null : found.getFirst();
            if (entity == null) {
                SysCodes.RECORD_NOT_FOUND.newException();
            }
            ensureDictEditable(entity);
            return entity;
        }).collect(Collectors.toList());
        for (DictEntity entity : targets) {
            Long id = entity.getId();
            FrozenEnumm target = entity.getFrozen() == FrozenEnumm.FROZEN
                    ? FrozenEnumm.UN_FROZEN
                    : FrozenEnumm.FROZEN;
            int expectedRevision = revision(entity);
            entity.setFrozen(target);
            audit(entity, false);
            requireChanged(this.mapper.updatePrivate(businessAccess.businessOwner().schema(),
                    entity, expectedRevision));
            affectedTypes.add(entity.getCode());
        }
        this.reloadTypesAfterCommit(affectedTypes);
    }

    private void ensureDictEditable(DictEntity entity) {
        businessAccess.requireBusiness(entity);
        if (entity.getFrozen() == FrozenEnumm.READ_ONLY) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private DictEntity toEntity(DictAoeBo bo, DictEntity oldEntity) {
        DictEntity entity = this.convert.boToEntity(bo);
        entity.setType(StrUtils.isBlank(bo.getType()) ? bo.getCode() : bo.getType());
        if (oldEntity != null && oldEntity.getFrozen() == FrozenEnumm.READ_ONLY) {
            entity.setFrozen(FrozenEnumm.READ_ONLY);
        }
        return entity;
    }

    private void diffSaveItems(Long mainId, List<DictLineBo> lines, List<DictItemEntity> oldItems) {
        List<DictLineBo> safeLines = lines == null ? Collections.emptyList() : lines;
        Map<Long, DictItemEntity> oldItemMap = oldItems.stream()
                .collect(Collectors.toMap(DictItemEntity::getId, Function.identity()));
        Set<Long> keepIds = new HashSet<>();
        double defaultSort = 1D;
        for (DictLineBo line : safeLines) {
            if (line.getId() != null && oldItemMap.get(line.getId()).getFrozen() == FrozenEnumm.READ_ONLY) {
                keepIds.add(line.getId());
                defaultSort++;
                continue;
            }
            if (line == null || StrUtils.isBlank(line.getKeyName()) || StrUtils.isBlank(line.getKeyValue())) {
                continue;
            }
            DictItemEntity item = this.convert.lineBoToEntity(line);
            item.setMainId(mainId);
            item.setSort(line.getOrders() == null ? defaultSort : line.getOrders());
            defaultSort++;
            if (line.getId() != null && oldItemMap.containsKey(line.getId())) {
                item.setId(line.getId());
                DictItemEntity oldItem = oldItemMap.get(line.getId());
                if (oldItem.getFrozen() == FrozenEnumm.READ_ONLY) {
                    item.setFrozen(FrozenEnumm.READ_ONLY);
                }
                audit(item, false);
                requireChanged(this.dictItemMapper.updatePrivate(businessAccess.businessOwner().schema(),
                        item, revision(oldItem)));
                keepIds.add(line.getId());
                continue;
            }
            item.setId(IdWorker.getId());
            audit(item, true);
            requireChanged(this.dictItemMapper.insertPrivate(businessAccess.businessOwner().schema(), item));
            keepIds.add(item.getId());
        }

        for (Long oldId : oldItemMap.keySet()) {
            if (!keepIds.contains(oldId)) {
                DictItemEntity oldItem = oldItemMap.get(oldId);
                audit(oldItem, false);
                requireChanged(this.dictItemMapper.deletePrivate(businessAccess.businessOwner().schema(),
                        oldItem, revision(oldItem)));
            }
        }
    }

    /** 完整预检先于主表写入，保护行遗漏或晚出现的非法行均拒绝整批。 */
    private void validateItems(List<DictLineBo> lines, List<DictItemEntity> oldItems) {
        Map<Long, DictItemEntity> oldMap = oldItems.stream()
                .collect(Collectors.toMap(DictItemEntity::getId, Function.identity()));
        Set<Long> seen = new HashSet<>();
        for (DictLineBo line : lines == null ? Collections.<DictLineBo>emptyList() : lines) {
            if (line == null || StrUtils.isBlank(line.getKeyName()) || StrUtils.isBlank(line.getKeyValue())) {
                SysCodes.RECORD_NOT_FOUND.newException();
            }
            DictItemEntity old = line.getId() == null ? null : oldMap.get(line.getId());
            if (line.getId() != null && (old == null || !seen.add(line.getId()))) {
                SysCodes.RECORD_NOT_FOUND.newException();
            }
            if (old != null && old.getFrozen() == FrozenEnumm.READ_ONLY) {
                if (!Objects.equals(line.getKeyName(), old.getDCode())
                        || !Objects.equals(line.getKeyValue(), old.getDValue())
                        || !Objects.equals(line.getRemark(), old.getRemark())
                        || line.getOrders() != null && Double.compare(line.getOrders(), old.getSort()) != 0
                        || line.getFrozen() != null && !FrozenEnumm.READ_ONLY.getCode().equals(line.getFrozen())) {
                    SysCodes.READ_ONLY_RECORD.newException();
                }
            } else {
                ensureWritableFrozen(line.getFrozen());
            }
        }
        if (oldItems.stream().anyMatch(item -> item.getFrozen() == FrozenEnumm.READ_ONLY
                && !seen.contains(item.getId()))) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private void ensureWritableFrozen(Integer frozen) {
        if (frozen != null && !FrozenEnumm.UN_FROZEN.getCode().equals(frozen)
                && !FrozenEnumm.FROZEN.getCode().equals(frozen)) {
            SysCodes.READ_ONLY_RECORD.newException();
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
        return listItems(mainId, businessAccess.businessOwner().schema());
    }

    private List<DictItemEntity> listItems(Long mainId, String schema) {
        if (mainId == null) {
            return Collections.emptyList();
        }
        return this.dictItemMapper.selectVisible(schema, new LambdaQueryWrapper<DictItemEntity>()
                .eq(DictItemEntity::getDeleted, 0)
                .eq(DictItemEntity::getMainId, mainId)
                .orderByAsc(DictItemEntity::getSort)
                .orderByAsc(DictItemEntity::getId));
    }

    private DictViewVo toVo(DictEntity entity, List<DictItemEntity> items) {
        DictViewVo vo = this.convert.entityToVo(entity);
        vo.setLines(this.convert.lineEntityToVo(items));
        return vo;
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
        Set<String> safeTypes = types.stream().filter(StrUtils::isNotBlank).collect(Collectors.toSet());
        Owner owner = businessAccess.businessOwner();
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("字典写入缺少提交同步事务");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 仅失效，避免旧事务快照晚回调覆盖较新提交的数据。
                safeTypes.forEach(code -> dictCacheHelper.remove(owner, code));
            }
        });
    }



    /** 完整清单冲突先拒绝，再接触缓存；关联主明细始终在同一来源内完成。 */
    private Map<Owner, List<DictEntity>> inventory(String code) {
        businessAccess.requireClassifiedList();
        Map<Owner, List<DictEntity>> result = new LinkedHashMap<>();
        Set<String> codes = new HashSet<>();
        for (Owner owner : businessAccess.sources()) {
            List<DictEntity> dicts = businessAccess.completeDicts(owner, code);
            for (DictEntity dict : dicts) {
                if (StrUtils.isBlank(dict.getCode()) || !codes.add(dict.getCode())) {
                    throw new IllegalStateException("字典编码来源冲突");
                }
            }
            result.put(owner, dicts.stream().filter(dict -> dict.getFrozen() != FrozenEnumm.FROZEN).toList());
        }
        return result;
    }

    private LinkedHashMap<String, String> cachedValues(Owner owner, DictEntity dict) {
        // 参与外层写事务时必须读取自己的修改，且不得把未提交值发布到共享缓存。
        if (!TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            return businessAccess.completeItems(owner, dict.getId());
        }
        Map<String, String> cached = dictCacheHelper.get(owner, dict.getCode());
        if (cached != null) {
            return new LinkedHashMap<>(cached);
        }
        LinkedHashMap<String, String> items = businessAccess.completeItems(owner, dict.getId());
        dictCacheHelper.cachePut(owner, dict.getCode(), items);
        return items;
    }

    /** 重载使用数据库权威清单与值；外层写事务不发布未提交内容。 */
    private void warmFromDatabase() {
        inventory(null).forEach((owner, dicts) -> dicts.forEach(dict -> {
            LinkedHashMap<String, String> items = businessAccess.completeItems(owner, dict.getId());
            if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                dictCacheHelper.cachePut(owner, dict.getCode(), items);
            }
        }));
    }

    private int revision(StdEntity entity) {
        return entity.getRevision() == null ? 0 : entity.getRevision();
    }

    private void requireChanged(int count) {
        if (count != 1) {
            throw new IllegalStateException("字典版本或数据权限冲突");
        }
    }

    /** 自定义列SQL显式保留服务端审计，不接受前端审计值。 */
    private void audit(StdEntity entity, boolean insert) {
        long userId = ThreadUserHelper.getUserId();
        String name = ThreadUserHelper.getUserName();
        LocalDateTime now = LocalDateTime.now();
        if (insert) {
            entity.setCreateId(userId);
            entity.setCreateBy(name);
            entity.setUtcCreate(now);
            entity.setRevision(0);
            entity.setDeleted(0);
        }
        entity.setModifyId(userId);
        entity.setModifyBy(name);
        entity.setUtcModify(now);
        if (entity.getFrozen() == null) {
            entity.setFrozen(FrozenEnumm.UN_FROZEN);
        }
        if (entity.getSort() == null) {
            entity.setSort(0D);
        }
    }
}
