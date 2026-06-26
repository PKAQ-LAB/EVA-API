package org.pkaq.sys.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.TreeHelper;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.bo.ModuleQueryBo;
import org.pkaq.sys.module.bo.ModuleResourcesBo;
import org.pkaq.sys.module.bo.ModuleSortBo;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.entity.ModuleResources;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.module.mapper.ModuleResourceMapper;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模块管理service —— 树形结构（CRUD + 冻结 + 同级拖拽）标准范本
 * <p>
 * 数据约定：
 * 1. pid 非空，根节点 pid = 0（DB 层 NOT NULL DEFAULT 0）
 * 2. path 形如 "/{id}"（根）、"/{parentPath}/{id}"（子孙），不可为 null
 * 3. sort 同级递增，跨级移动后自动追加到末尾
 * 4. isleaf：新增子节点时父节点自动置 false，删除/移走最后一个子节点时父节点自动置 true
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class ModuleService extends StdService<ModuleMapper, ModuleEntity> {
    /** 根节点 pid 哨兵值，与 DB 默认值保持一致 */
    private static final long ROOT_PID = 0L;

    private final ModuleResourceMapper moduleResourceMapper;

    private final RoleResourceMapper roleResourceMapper;

    private final ModuleConvert convert;


    /**
     * 根据ID批量删除模块
     * 删除规则：
     * 1. 存在子节点不允许删除
     * 2. 同步删除模块下的资源以及角色对该资源的授权
     * 3. 删除后若原父节点已无其它子，置 isleaf = true
     *
     * @param ids 模块 id 集合
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void deleteModule(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return;
        }

        // 子节点存在性检查
        List<ModuleEntity> leafList = this.mapper.selectList(new LambdaQueryWrapper<ModuleEntity>()
                .in(ModuleEntity::getPid, ids));

        if (CollUtils.isNotEmpty(leafList)) {
            String nameStr = leafList.stream()
                    .map(ModuleEntity::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            CommonCodes.CHILD_EXIST.newException(nameStr);
            return;
        }

        // 收集原父节点 id（删除后需要刷 isleaf；ROOT_PID 不需要维护 isleaf）
        Set<Long> originPids = this.mapper.selectList(
                        new LambdaQueryWrapper<ModuleEntity>()
                                .select(ModuleEntity::getPid)
                                .in(ModuleEntity::getId, ids))
                .stream()
                .map(ModuleEntity::getPid)
                .filter(pid -> pid != ROOT_PID)
                .collect(Collectors.toSet());

        try {
            // 先删除授权关系；deleteByModuleIds 依赖资源表反查，不能放在资源删除之后
            this.roleResourceMapper.deleteByModuleIds(ids);
            // 删除模块下的资源定义
            this.moduleResourceMapper.delete(new LambdaUpdateWrapper<ModuleResources>()
                    .in(ModuleResources::getMainId, ids));
            // 删除模块本体
            this.mapper.deleteByIds(ids);
        } catch (Exception e) {
            throw new BizException(SysCodes.MODULE_RESOURCE_USED);
        }

        // 刷新原父节点的 isleaf
        refreshParentLeaf(originPids);
    }

    /**
     * 新增/编辑模块
     * - 新增：生成 id、计算 path、设置 isleaf=true，同步把父节点（非根）置为 isleaf=false
     * - 编辑：未换父节点仅更新基本字段；换了父节点则重算 path 并级联刷新所有子孙
     * - 冻结状态变更走独立的 {@link #switchFrozen(SingleArray)} 端点，editModule 不处理 frozen
     *
     * @param bo 模块对象
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void editModule(ModuleAoeBo bo) {
        ModuleEntity module = this.convert.boToEntity(bo);
        // 入口规范化：前端未传 pid 时统一为根节点哨兵 0，后续判断不再考虑 null
        if (module.getPid() == null) {
            module.setPid(ROOT_PID);
        }

        Long moduleId = module.getId();
        long pid = module.getPid();
        boolean isNew = moduleId == null || moduleId == 0L;
        boolean isRoot = pid == ROOT_PID;

        if (isNew) {
            // 新增：先生成 id 以便计算 path
            moduleId = IdWorker.getId();
            module.setId(moduleId);
            module.setIsleaf(true);
            module.setPath(buildPath(pid, moduleId, isRoot));
            module.setSort(nextSort(pid));
            this.mapper.insert(module);

            // 父节点（非根）变为非叶子
            if (!isRoot) {
                setParentLeaf(pid, false);
            }
        } else {
            ModuleEntity origin = this.mapper.selectById(moduleId);
            if (origin == null) {
                CommonCodes.CAN_NOT_FIND_RECORD.newException(moduleId);
                return;
            }

            if (origin.getPid() != pid) {
                // 换了父节点：重算 path、刷新子孙、维护两边 isleaf
                handleParentChange(module, origin, isRoot);
            } else {
                // 未换父节点：仅更新基本字段（path/sort 保持不变）
                module.setPath(origin.getPath());
                module.setSort(origin.getSort());
                this.mapper.updateById(module);
            }
        }

        // 更新资源（diff 模式，避免误删被引用的资源）
        Set<Long> deletedResourceIds = handleResources(moduleId, bo.getResources());
        if (!deletedResourceIds.isEmpty()) {
            this.roleResourceMapper.deleteByResourceIds(deletedResourceIds);
        }
    }

    /**
     * 根据ID获取一条模块信息
     *
     * @param id 模块ID
     * @return 模块信息
     */
    public ModuleDetailVo getModule(Long id) {
        ModuleDetailVo md = this.convert.entityToDetailVo(this.get(id));

        List<ModuleResources> resources = this.moduleResourceMapper.selectList(
                new LambdaQueryWrapper<ModuleResources>().eq(ModuleResources::getMainId, id));
        md.setResources(this.convert.resourceEntityToVo(resources));

        return md;
    }

    /**
     * 根据属性查询模块树列表（可选含资源）
     */
    public Collection<ModuleDetailVo> list(ModuleQueryBo queryBo, boolean withResource) {
        Map<Long, ModuleDetailVo> moduleMap = this.mapper.selectModuleMapList(queryBo);

        if (CollUtils.isEmpty(moduleMap)) {
            return Collections.emptyList();
        }
        if (withResource) {
            this.handleFetchResource(moduleMap);
        }

        return TreeHelper.buildTree(moduleMap.values());
    }

    /**
     * 根据用户ID查询用户拥有权限的模块树（含资源）
     */
    public Collection<ModuleDetailVo> fetchUserModules(Long uid) {
        Map<Long, ModuleDetailVo> moduleMap = this.mapper.listGrantedModules(uid);

        if (CollUtils.isEmpty(moduleMap)) {
            return Collections.emptyList();
        }

        this.handleFetchResource(moduleMap);
        return TreeHelper.buildTree(moduleMap.values());
    }

    /**
     * 同级拖拽排序
     * 仅在同一父节点下生效，跨父节点请走 editModule。
     *
     * @param bo 包含 id、oldSort、newSort
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void sortModule(ModuleSortBo bo) {
        if (bo == null || bo.getId() == null) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        ModuleEntity self = this.mapper.selectById(bo.getId());
        if (self == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(bo.getId());
            return;
        }
        if (bo.getOldSort() == bo.getNewSort()) {
            return;
        }
        // 以服务端读取的真实 pid 为准，避免前端越权指定
        this.mapper.updateSort(bo.getId(), self.getPid(), bo.getOldSort(), bo.getNewSort());
    }

    /**
     * 校验同级节点中 code 是否唯一
     */
    public boolean checkUnique(ModuleEntity module) {
        if (module == null) {
            return false;
        }
        long pid = module.getPid() == null ? ROOT_PID : module.getPid();
        LambdaQueryWrapper<ModuleEntity> wrapper = new LambdaQueryWrapper<ModuleEntity>()
                .eq(ModuleEntity::getCode, module.getCode())
                .eq(ModuleEntity::getPid, pid);
        // 编辑场景排除自身
        if (module.getId() != null && module.getId() != 0L) {
            wrapper.ne(ModuleEntity::getId, module.getId());
        }
        return this.mapper.selectCount(wrapper) > 0;
    }

    /**
     * 批量切换冻结状态（逐个处理，子节点跳过判断）
     * 行为：
     * - 按当前节点的 frozen 翻转：FROZEN → UN_FROZEN，否则 → FROZEN
     * - 冻结：自身 + 所有 path 前缀以本节点为根的子孙
     * - 解锁：若父节点为 FROZEN 则跳过当前节点（不能在父被冻结时单独解锁子节点）
     *
     * @param ids 节点 id 集合
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void switchFrozen(SingleArray<Long> ids) {
        if (ids == null || CollUtils.isEmpty(ids.getParam())) {
            return;
        }
        for (Long id : ids.getParam()) {
            ModuleEntity self = this.mapper.selectById(id);
            if (self == null) {
                continue;
            }
            FrozenEnumm target = self.getFrozen() == FrozenEnumm.FROZEN
                    ? FrozenEnumm.UN_FROZEN
                    : FrozenEnumm.FROZEN;

            // 解锁时若父节点为冻结，禁止解锁子节点
            if (target == FrozenEnumm.UN_FROZEN && self.getPid() != ROOT_PID) {
                ModuleEntity parent = this.mapper.selectById(self.getPid());
                if (parent != null && parent.getFrozen() == FrozenEnumm.FROZEN) {
                    continue;
                }
            }
            this.mapper.cascadeFrozen(id, self.getPath(), target.getCode());
        }
    }

    // ------------------------------------------------------------------
    // 私有辅助方法
    // ------------------------------------------------------------------

    /**
     * 计算节点 path：根节点为 "/{id}"，非根为 "{parentPath}/{id}"
     */
    private String buildPath(long pid, long id, boolean isRoot) {
        if (isRoot) {
            return "/" + id;
        }
        ModuleEntity parent = this.mapper.selectById(pid);
        if (parent == null || parent.getPath() == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(pid);
            return null;
        }
        return parent.getPath() + "/" + id;
    }

    /**
     * 取指定父节点下的下一个 sort 值
     */
    private double nextSort(long pid) {
        Integer maxSort = this.mapper.listOrder(pid);
        return (maxSort == null ? 0 : maxSort) + 1;
    }

    /**
     * 设置指定节点的 isleaf
     */
    private void setParentLeaf(long pid, boolean isleaf) {
        this.mapper.update(null, new LambdaUpdateWrapper<ModuleEntity>()
                .eq(ModuleEntity::getId, pid)
                .set(ModuleEntity::getIsleaf, isleaf));
    }

    /**
     * 对一批父节点 id，若已无子节点则置 isleaf=true
     */
    private void refreshParentLeaf(Set<Long> parentIds) {
        if (CollUtils.isEmpty(parentIds)) {
            return;
        }
        for (Long pid : parentIds) {
            Long childCount = this.mapper.selectCount(new LambdaQueryWrapper<ModuleEntity>()
                    .eq(ModuleEntity::getPid, pid));
            if (childCount == null || childCount == 0L) {
                setParentLeaf(pid, true);
            }
        }
    }

    /**
     * 处理父节点变更：重算 path、刷新所有子孙 path、维护两边 isleaf
     */
    private void handleParentChange(ModuleEntity module, ModuleEntity origin, boolean isRoot) {
        long moduleId = module.getId();
        long newPid = module.getPid();
        long oldPid = origin.getPid();
        String oldPath = origin.getPath();

        String newPath = buildPath(newPid, moduleId, isRoot);
        module.setPath(newPath);
        module.setSort(nextSort(newPid));
        this.mapper.updateById(module);

        // 级联刷新所有子孙的 path 前缀
        if (oldPath != null && !oldPath.isEmpty()) {
            this.mapper.refreshPath(oldPath, oldPath.length(), newPath);
        }

        // 原父节点（非根）若已无其它子，置 isleaf=true
        if (oldPid != ROOT_PID) {
            refreshParentLeaf(Set.of(oldPid));
        }
        // 新父节点（非根）置 isleaf=false
        if (!isRoot) {
            setParentLeaf(newPid, false);
        }
    }

    /**
     * 模块详情查询：批量回填资源
     */
    private void handleFetchResource(Map<Long, ModuleDetailVo> moduleMap) {
        List<ModuleResources> resourceList = this.moduleResourceMapper.selectList(
                new LambdaQueryWrapper<ModuleResources>().in(ModuleResources::getMainId, moduleMap.keySet()));

        Map<Long, List<ModuleResources>> grouped = resourceList.stream()
                .collect(Collectors.groupingBy(ModuleResources::getMainId));

        grouped.forEach((moduleId, resources) -> {
            ModuleDetailVo moduleVo = moduleMap.get(moduleId);
            if (moduleVo != null) {
                moduleVo.setResources(this.convert.resourceEntityToVo(resources));
            }
        });
    }

    /**
     * 资源 diff 保存：
     * - 入参资源带 id 且已存在 → 更新
     * - 入参资源不带 id 或 id 不存在 → 新增
     * - 现有资源未出现在入参 → 删除
     * 避免"先全删再插入"导致已被授权引用的资源 id 变更。
     */
    private Set<Long> handleResources(long moduleId, List<ModuleResourcesBo> resources) {
        if (resources == null) {
            return Collections.emptySet();
        }
        List<ModuleResources> existingList = this.moduleResourceMapper.selectList(
                new LambdaQueryWrapper<ModuleResources>().eq(ModuleResources::getMainId, moduleId));
        Map<Long, ModuleResources> existingMap = existingList.stream()
                .collect(Collectors.toMap(ModuleResources::getId, item -> item));
        Set<Long> existingIds = existingMap.keySet();

        Set<Long> retainedIds = new HashSet<>();

        if (CollUtils.isNotEmpty(resources)) {
            List<ModuleResources> incoming = this.convert.resourceBoToEntity(distinctResources(resources));
            for (ModuleResources r : incoming) {
                if (r.getId() != null && r.getId() != 0L && existingIds.contains(r.getId())) {
                    ModuleResources existing = existingMap.get(r.getId());
                    if (!Objects.equals(existing.getMainId(), moduleId)) {
                        CommonCodes.PARAM_ERROR.newException();
                        continue;
                    }
                    r.setMainId(moduleId);
                    this.moduleResourceMapper.updateById(r);
                    retainedIds.add(r.getId());
                } else {
                    r.setId(null);
                    r.setMainId(moduleId);
                    this.moduleResourceMapper.insert(r);
                }
            }
        }

        // 删除已不在入参中的资源
        Set<Long> toDelete = new HashSet<>(existingIds);
        toDelete.removeAll(retainedIds);
        if (!toDelete.isEmpty()) {
            this.moduleResourceMapper.delete(new LambdaUpdateWrapper<ModuleResources>()
                    .in(ModuleResources::getId, toDelete));
        }
        return toDelete;
    }

    /**
     * 对入参资源去重，避免同一次请求重复 update/insert。
     * 有 id 时按 id 去重；无 id 时按 type + url 去重。
     */
    private List<ModuleResourcesBo> distinctResources(List<ModuleResourcesBo> resources) {
        Map<String, ModuleResourcesBo> resourceMap = new LinkedHashMap<>();
        for (ModuleResourcesBo resource : resources) {
            if (resource == null) {
                continue;
            }
            String key = resource.getId() != null && resource.getId() != 0L
                    ? "ID:" + resource.getId()
                    : "NEW:" + resource.getResourceType() + ":" + resource.getResourceUrl();
            resourceMap.putIfAbsent(key, resource);
        }
        return new ArrayList<>(resourceMap.values());
    }
}
