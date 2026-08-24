package org.pkaq.sys.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.event.ModuleResourceChangedEvent;
import org.pkaq.core.event.ModuleResourceChangedEvent.ChangeReason;
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
import org.pkaq.sys.module.enums.ResourceTypeEnum;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.module.mapper.ModuleResourceMapper;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.module.vo.ModuleMenuVo;
import org.pkaq.sys.module.vo.ModuleResourcesVo;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.springframework.context.ApplicationEventPublisher;
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
 * 模块管理服务。
 * <p>
 * 维护模块树、模块资源子表、角色资源引用以及资源缓存重建事件。
 * 树形字段规则：根节点 pid 为 0，path 记录节点 id 路径，sort 仅在同级内排序，isleaf 由服务端维护。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class ModuleService extends StdService<ModuleMapper, ModuleEntity> {
    /** 根节点 pid，与数据库默认值保持一致。 */
    private static final long ROOT_PID = 0L;

    private final ModuleResourceMapper moduleResourceMapper;

    private final RoleResourceMapper roleResourceMapper;

    private final ModuleConvert convert;

    private final ApplicationEventPublisher eventPublisher;


    /**
     * 删除模块，并清理模块资源、角色资源关系以及刷新父节点叶子状态。
     *
     * @param ids 模块 id 集合
     */
    public void deleteModule(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return;
        }


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


        Set<Long> originPids = this.mapper.selectList(
                        new LambdaQueryWrapper<ModuleEntity>()
                                .select(ModuleEntity::getPid)
                                .in(ModuleEntity::getId, ids))
                .stream()
                .map(ModuleEntity::getPid)
                .filter(pid -> pid != ROOT_PID)
                .collect(Collectors.toSet());

        Set<Long> affectedRoleIds = this.roleResourceMapper.selectRoleIdsByModuleIds(ids);

        try {
            // 业务处理
            this.roleResourceMapper.deleteByModuleIds(ids);
            // 业务处理
            this.moduleResourceMapper.delete(new LambdaUpdateWrapper<ModuleResources>()
                    .in(ModuleResources::getMainId, ids));
            // 业务处理
            this.mapper.deleteByIds(ids);
        } catch (Exception e) {
            throw new BizException(SysCodes.MODULE_RESOURCE_USED);
        }

        refreshParentLeaf(originPids);
        publishModuleResourceChanged(affectedRoleIds, ChangeReason.MODULE_DELETED);
    }

    /**
     * 新增或编辑模块，并同步资源子表。
     * <p>
     * 新增时服务端生成 id、path、sort、isleaf；编辑更换父节点时重算自身和子孙 path。
     * 冻结状态通过 {@link #switchFrozen(SingleArray)} 维护，编辑接口不处理 frozen。
     *
     * @param bo 模块新增编辑参数
     */
    public void editModule(ModuleAoeBo bo) {
        ModuleEntity module = this.convert.boToEntity(bo);
        if (module.getPid() == null) {
            module.setPid(ROOT_PID);
        }

        Long moduleId = module.getId();
        long pid = module.getPid();
        boolean isNew = moduleId == null || moduleId == 0L;
        boolean isRoot = pid == ROOT_PID;

        if (isNew) {
            moduleId = IdWorker.getId();
            module.setId(moduleId);
            module.setIsleaf(true);
            module.setPath(buildPath(pid, moduleId, isRoot));
            module.setSort(nextSort(pid));
            this.mapper.insert(module);

            // 业务处理
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
                handleParentChange(module, origin, isRoot);
            } else {
                // 未更换父节点时保留原路径和排序
                module.setPath(origin.getPath());
                module.setSort(origin.getSort());
                this.mapper.updateById(module);
            }
        }

        // 更新资源定义
        ResourceDiffResult diffResult = handleResources(moduleId, bo.getResources());
        if (!diffResult.changedIds().isEmpty()) {
            Set<Long> affectedRoleIds = this.roleResourceMapper.selectRoleIdsByResourceIds(diffResult.changedIds());
            if (!diffResult.deletedIds().isEmpty()) {
                this.roleResourceMapper.deleteByResourceIds(diffResult.deletedIds());
            }
            publishModuleResourceChanged(affectedRoleIds, ChangeReason.MODULE_RESOURCE_CHANGED);
        }
    }

    /**
     * 查询模块详情，并附带资源列表。
     *
     * @param id 模块 id
     * @return 模块详情
     */
    public ModuleDetailVo getModule(Long id) {
        ModuleEntity entity = this.get(id);
        ModuleDetailVo md = this.convert.entityToDetailVo(entity);

        List<ModuleResources> resources = this.moduleResourceMapper.selectList(
                new LambdaQueryWrapper<ModuleResources>().eq(ModuleResources::getMainId, id));
        md.setResources(this.convert.resourceEntityToVo(resources));

        return md;
    }

    /**
     * 查询模块树，可按需附带资源列表。
     */
    public Collection<ModuleDetailVo> list(ModuleQueryBo queryBo, boolean withResource) {
        Map<Long, ModuleDetailVo> moduleMap = toDetailMap(this.mapper.selectModuleMapList(queryBo));

        if (CollUtils.isEmpty(moduleMap)) {
            return Collections.emptyList();
        }
        if (withResource) {
            this.handleFetchResource(moduleMap);
        }

        return TreeHelper.buildTree(moduleMap.values());
    }

    /**
     * 查询用户已授权的模块树，并附带资源列表。
     */
    public Collection<ModuleDetailVo> fetchUserModules(Long uid) {
        Map<Long, ModuleDetailVo> moduleMap = toDetailMap(this.mapper.listGrantedModules(uid));

        if (CollUtils.isEmpty(moduleMap)) {
            return Collections.emptyList();
        }

        this.handleFetchResource(moduleMap);
        return TreeHelper.buildTree(moduleMap.values());
    }

    /**
     * 查询前端菜单树，仅返回菜单渲染和权限判断所需字段。
     *
     * @param uid 用户ID
     * @return 前端菜单树
     */
    public Collection<ModuleMenuVo> fetchUserMenus(Long uid) {
        Collection<ModuleDetailVo> modules = this.fetchUserModules(uid);
        if (CollUtils.isEmpty(modules)) {
            return Collections.emptyList();
        }
        return modules.stream()
                .map(this.convert::detailToMenuVo)
                .collect(Collectors.toList());
    }

    /**
     * 调整模块同级排序。
     *
     * @param bo 排序参数，包含模块 id、原排序值和目标排序值
     */
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
        this.mapper.updateSort(bo.getId(), self.getPid(), bo.getOldSort(), bo.getNewSort());
    }

    /**
     * 校验同级模块 code 是否重复。
     */
    public boolean checkUnique(ModuleEntity module) {
        if (module == null) {
            return false;
        }
        long pid = module.getPid() == null ? ROOT_PID : module.getPid();
        LambdaQueryWrapper<ModuleEntity> wrapper = new LambdaQueryWrapper<ModuleEntity>()
                .eq(ModuleEntity::getCode, module.getCode())
                .eq(ModuleEntity::getPid, pid);
        // 业务处理
        if (module.getId() != null && module.getId() != 0L) {
            wrapper.ne(ModuleEntity::getId, module.getId());
        }
        return this.mapper.selectCount(wrapper) > 0;
    }

    /**
     * 切换模块冻结状态，并级联更新子节点。
     * <p>
     * 父节点冻结后子节点全部冻结；父节点未解冻时，子节点不允许单独解冻。
     *
     * @param ids 模块 id 集合
     */
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

            // 业务处理
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

    /**
     * 构建节点 path，根节点格式为 /{id}，子节点格式为 {parentPath}/{id}。
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
     * 获取同级节点下一个排序值。
     */
    private double nextSort(long pid) {
        Integer maxSort = this.mapper.listOrder(pid);
        return (maxSort == null ? 0 : maxSort) + 1;
    }

    /**
     * 设置父节点叶子状态。
     */
    private void setParentLeaf(long pid, boolean isleaf) {
        this.mapper.update(null, new LambdaUpdateWrapper<ModuleEntity>()
                .eq(ModuleEntity::getId, pid)
                .set(ModuleEntity::getIsleaf, isleaf));
    }

    /**
     * 删除或迁移节点后，刷新旧父节点的叶子状态。
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
     * 更换父节点时刷新当前节点、子孙节点路径以及新旧父节点叶子状态。
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

        // 业务处理
        if (oldPath != null && !oldPath.isEmpty()) {
            this.mapper.refreshPath(oldPath, oldPath.length(), newPath);
        }

        if (oldPid != ROOT_PID) {
            refreshParentLeaf(Set.of(oldPid));
        }
        if (!isRoot) {
            setParentLeaf(newPid, false);
        }
    }

    /**
     * 批量装载模块资源列表。
     */
    private void handleFetchResource(Map<Long, ModuleDetailVo> moduleMap) {
        List<ModuleResources> resourceList = this.moduleResourceMapper.selectList(
                new LambdaQueryWrapper<ModuleResources>()
                        .select(ModuleResources::getId,
                                ModuleResources::getMainId,
                                ModuleResources::getCode,
                                ModuleResources::getResourceDesc,
                                ModuleResources::getResourceUrl,
                                ModuleResources::getResourceType,
                                ModuleResources::getSort)
                        .in(ModuleResources::getMainId, moduleMap.keySet())
                        .orderByAsc(ModuleResources::getMainId, ModuleResources::getSort, ModuleResources::getId));

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
     * 将 Mapper 返回的实体映射转换为 Service 输出视图映射。
     */
    private Map<Long, ModuleDetailVo> toDetailMap(Map<Long, ModuleEntity> entityMap) {
        if (CollUtils.isEmpty(entityMap)) {
            return Collections.emptyMap();
        }
        return this.convert.entityToDetailVo(new ArrayList<>(entityMap.values())).stream()
                .collect(Collectors.toMap(ModuleDetailVo::getId, item -> item,
                        (left, right) -> left, LinkedHashMap::new));
    }

    /**
     * 差异同步资源子表。
     * <p>
     * 入参存在 id 时更新原记录；未传 id 时按资源唯一键去重后新增；数据库中未出现在入参中的资源会被删除。
     */
    private ResourceDiffResult handleResources(long moduleId, List<ModuleResourcesBo> resources) {
        if (resources == null) {
            return ResourceDiffResult.empty();
        }
        List<ModuleResources> existingList = this.moduleResourceMapper.selectList(
                new LambdaQueryWrapper<ModuleResources>().eq(ModuleResources::getMainId, moduleId));
        Map<Long, ModuleResources> existingMap = existingList.stream()
                .collect(Collectors.toMap(ModuleResources::getId, item -> item));
        Set<Long> existingIds = new HashSet<>(existingMap.keySet());

        Set<Long> retainedIds = new HashSet<>();
        Set<Long> changedIds = new HashSet<>();
        double nextSort = existingList.stream()
                .map(ModuleResources::getSort)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0D) + 1D;

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
                    normalizeResourceForSave(r, existing.getSort(), nextSort);
                    this.moduleResourceMapper.updateById(r);
                    retainedIds.add(r.getId());
                    changedIds.add(r.getId());
                } else {
                    r.setId(null);
                    r.setMainId(moduleId);
                    normalizeResourceForSave(r, null, nextSort);
                    nextSort = r.getSort() + 1D;
                    this.moduleResourceMapper.insert(r);
                }
            }
        }


        Set<Long> toDelete = new HashSet<>(existingIds);
        toDelete.removeAll(retainedIds);
        if (!toDelete.isEmpty()) {
            this.moduleResourceMapper.delete(new LambdaUpdateWrapper<ModuleResources>()
                    .in(ModuleResources::getId, toDelete));
            changedIds.addAll(toDelete);
        }
        return new ResourceDiffResult(toDelete, changedIds);
    }

    /**
     * 按 id 或新资源唯一键去重，避免同一次提交重复更新或插入。
     */
    private List<ModuleResourcesBo> distinctResources(List<ModuleResourcesBo> resources) {
        Map<String, ModuleResourcesBo> resourceMap = new LinkedHashMap<>();
        for (ModuleResourcesBo resource : resources) {
            if (resource == null) {
                continue;
            }
            String key = resource.getId() != null && resource.getId() != 0L
                    ? "ID:" + resource.getId()
                    : buildNewResourceKey(resource);
            resourceMap.putIfAbsent(key, resource);
        }
        return new ArrayList<>(resourceMap.values());
    }

    private String buildNewResourceKey(ModuleResourcesBo resource) {
        if (hasText(resource.getCode())) {
            return "NEW_CODE:" + resource.getCode().trim();
        }
        return "NEW:" + resource.getResourceType() + ":" + resource.getResourceUrl();
    }

    private void normalizeResourceForSave(ModuleResources resource, Double oldSort, double nextSort) {
        resource.setResourceType(ResourceTypeEnum.normalize(resource.getResourceType()));
        if (resource.getSort() == null || resource.getSort() <= 0D) {
            resource.setSort(oldSort == null || oldSort <= 0D ? nextSort : oldSort);
        }
        if (hasText(resource.getCode())) {
            resource.setCode(resource.getCode().trim());
        }
        if (hasText(resource.getResourceUrl())) {
            resource.setResourceUrl(resource.getResourceUrl().trim());
        }
    }

    private void publishModuleResourceChanged(Set<Long> roleIds, ChangeReason reason) {
        if (CollUtils.isEmpty(roleIds)) {
            return;
        }
        this.eventPublisher.publishEvent(new ModuleResourceChangedEvent(this, roleIds, reason));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private record ResourceDiffResult(Set<Long> deletedIds, Set<Long> changedIds) {

        private static ResourceDiffResult empty() {
            return new ResourceDiffResult(Collections.emptySet(), Collections.emptySet());
        }
    }
}
