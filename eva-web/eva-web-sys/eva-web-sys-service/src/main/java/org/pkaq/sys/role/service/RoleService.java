package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.event.ModuleResourceChangedEvent;
import org.pkaq.core.event.ModuleResourceChangedEvent.ChangeReason;
import org.pkaq.core.mvc.bo.IdCodeBo;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchema;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.mybatis.util.TreeHelper;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.entity.ModuleResources;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.module.vo.ModuleResourcesVo;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.sys.role.bo.RoleQueryBo;
import org.pkaq.sys.role.bo.RoleResourceRefBo;
import org.pkaq.sys.role.bo.RoleUserRefBo;
import org.pkaq.sys.role.convert.RoleConvert;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.entity.RoleResourceEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.role.vo.RoleDetailVo;
import org.pkaq.sys.role.vo.RoleGrantedModuleVo;
import org.pkaq.sys.role.vo.RoleGrantedUserVo;
import org.pkaq.sys.tenant.mapper.TenantResourceMapper;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色服务。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class RoleService extends StdService<RoleMapper, RoleEntity> implements IRoleService {
    /** 默认数据权限：全部权限。 */
    private static final String DEFAULT_DATA_SCOPE = "0000";

    private final RoleResourceMapper roleResourceMapper;

    private final RoleUserMapper roleUserMapper;

    private final ModuleMapper moduleMapper;

    private final UserMapper userMapper;

    private final TenantResourceMapper tenantResourceMapper;

    private final UserConvert userConvert;

    private final RoleConvert roleConvert;

    private final ModuleConvert moduleConvert;

    private final ApplicationEventPublisher eventPublisher;

    private final CoreSchemaExecutor coreSchemaExecutor;

    private final EvaConfig evaConfig;

    /**
     * 根据ID查询角色详情。
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @Transactional(readOnly = true)
    @TenantSchema
    public RoleDetailVo getRole(Long id) {
        CommonCodes.NULL_ID.assertNotNull(id);
        RoleEntity entity = this.mapper.selectById(id);
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(id);
            return null;
        }
        return this.roleConvert.toVo(entity);
    }

    /**
     * 分页查询角色列表，角色名称和角色编码使用模糊查询。
     *
     * @param page 查询条件
     * @return 角色分页列表
     */
    @Transactional(readOnly = true)
    @TenantSchema
    public PageVo listPage(RoleQueryBo page) {
        if (page == null) {
            CommonCodes.PARAM_ERROR.newException();
            return null;
        }
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(hasText(page.getName()), RoleEntity::getName,
                page.getName() == null ? null : page.getName().trim());
        wrapper.like(hasText(page.getCode()), RoleEntity::getCode,
                page.getCode() == null ? null : page.getCode().trim().toUpperCase());
        wrapper.orderByDesc(RoleEntity::getUtcModify);

        PageResult<RoleEntity> pagination = new PageResult<>(page.getPageNo(), page.getPageSize());
        return this.mapper.selectPage(pagination, wrapper).map(this.roleConvert::toVo);
    }

    /**
     * 批量删除角色，同步清理角色-用户、角色-资源关系，并刷新受影响用户权限版本号。
     *
     * @param ids 角色ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @TenantSchema
    public void delete(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return;
        }
        Set<Long> roleIds = sanitizeIds(ids);
        if (CollUtils.isEmpty(roleIds)) {
            return;
        }
        ensureRolesEditable(roleIds);

        Set<Long> affectedUsers = fetchUsersByRoleIds(roleIds);
        this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>().in(RoleUserEntity::getRoleId, roleIds));
        this.roleResourceMapper.delete(new LambdaQueryWrapper<RoleResourceEntity>().in(RoleResourceEntity::getRoleId, roleIds));
        this.mapper.deleteByIds(roleIds);

        this.eventPublisher.publishEvent(new ModuleResourceChangedEvent(this, roleIds, ChangeReason.ROLE_RESOURCE_CHANGED));
        incrementPermVer(affectedUsers);
    }

    /**
     * 新增或编辑角色。
     *
     * @param bo 角色保存参数
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @TenantSchema
    public void editUniqueCode(RoleAoeBo bo) {
        if (bo == null || !hasText(bo.getCode()) || !hasText(bo.getName())) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        if (bo.getId() != null && bo.getId() != 0L) {
            ensureRoleEditable(bo.getId());
        }
        if (!hasText(bo.getDataScope())) {
            bo.setDataScope(DEFAULT_DATA_SCOPE);
        }
        IdCodeBo codeCheck = new IdCodeBo();
        codeCheck.setId(bo.getId());
        codeCheck.setCode(bo.getCode());
        if (this.isUnique(codeCheck) || isDuplicateName(bo)) {
            CommonCodes.DUPLICATE_CODE_ERROR.newException();
            return;
        }
        RoleEntity entity = this.roleConvert.fromBo(bo);
        this.mapper.insertOrUpdate(entity);
        if (bo.getId() != null && bo.getId() != 0L) {
            incrementPermVer(fetchUsersByRoleIds(Set.of(bo.getId())));
        }
    }

    /**
     * 切换角色冻结状态，并刷新受影响用户权限版本号。
     *
     * @param ids 角色ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @TenantSchema
    public void switchFrozen(SingleArray<Long> ids) {
        if (ids == null || CollUtils.isEmpty(ids.getParam())) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        Set<Long> roleIds = sanitizeIds(ids.getParam());
        Set<Long> affectedUsers = fetchUsersByRoleIds(roleIds);

        for (Long roleId : roleIds) {
            RoleEntity role = this.mapper.selectById(roleId);
            if (role == null || role.getFrozen() == FrozenEnumm.READ_ONLY) {
                continue;
            }
            FrozenEnumm target = role.getFrozen() == FrozenEnumm.FROZEN
                    ? FrozenEnumm.UN_FROZEN
                    : FrozenEnumm.FROZEN;
            this.mapper.update(null, new LambdaUpdateWrapper<RoleEntity>()
                    .eq(RoleEntity::getId, roleId)
                    .set(RoleEntity::getFrozen, target));
        }
        incrementPermVer(affectedUsers);
    }

    /**
     * 校验角色编码是否已存在。
     *
     * @param idCodeBo 编码校验参数
     * @return true 表示已存在
     */
    @Override
    @Transactional(readOnly = true)
    @TenantSchema
    public boolean isUnique(IdCodeBo idCodeBo) {
        if (idCodeBo == null || idCodeBo.getCode() == null) {
            return false;
        }
        String code = idCodeBo.getCode().trim();
        if (!code.toUpperCase().startsWith(CommonConstant.AUTH_PREFIX)) {
            code = CommonConstant.AUTH_PREFIX + code;
        }
        code = code.toUpperCase();

        var wrapper = Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getCode, code)
                .ne(idCodeBo.getId() != null && idCodeBo.getId() != 0L, RoleEntity::getId, idCodeBo.getId());

        return this.mapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取角色可授权的模块资源树，并标记当前角色已授权资源。
     *
     * @param roleModule 角色授权查询参数
     * @return 角色授权模块资源
     */
    @Override
    @Transactional(readOnly = true)
    @TenantSchema
    public RoleGrantedModuleVo fetchResource(RoleResourceRefBo roleModule) {
        if (roleModule == null || roleModule.getRoleId() == null) {
            CommonCodes.PARAM_ERROR.newException();
            return null;
        }
        Long roleId = roleModule.getRoleId();
        ensureRoleExists(roleId);

        if (this.evaConfig.getTenant().isSchemaMode()) {
            return fetchSchemaModeResources(roleId);
        }

        Long curUid = ThreadUserHelper.getUserId();
        Map<Long, ModuleEntity> moduleEntities = this.moduleMapper.listGrantedModules(curUid);
        Map<Long, ModuleDetailVo> moduleMap = this.moduleConvert
                .entityToDetailVo(new java.util.ArrayList<>(moduleEntities.values()))
                .stream()
                .collect(Collectors.toMap(ModuleDetailVo::getId, item -> item,
                        (left, right) -> left, LinkedHashMap::new));
        Map<Long, ModuleDetailVo> grantedModuleMap = moduleMap == null ? new LinkedHashMap<>() : moduleMap;
        List<ModuleResourcesVo> resources = this.moduleConvert.resourceEntityToVo(
                this.roleResourceMapper.listGrantedResource(roleId));
        Map<Long, List<ModuleResourcesVo>> resourceMap = resources.stream()
                .collect(Collectors.groupingBy(ModuleResourcesVo::getMainId, LinkedHashMap::new, Collectors.toList()));

        Set<Long> moduleChecked = new HashSet<>();
        resourceMap.forEach((moduleId, moduleResources) -> {
            ModuleDetailVo module = grantedModuleMap.get(moduleId);
            if (module != null) {
                module.setResources(moduleResources);
                if (moduleResources.stream().anyMatch(this::isCheckedResource)) {
                    moduleChecked.add(moduleId);
                }
            }
        });

        return this.roleConvert.toGrantedModuleVo(TreeHelper.buildTree(grantedModuleMap.values()), moduleChecked);
    }

    /**
     * 保存角色-资源关系，采用 diff 模式避免无意义删除插入。
     *
     * @param role 角色资源授权参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @TenantSchema
    public void grantResource(RoleResourceRefBo role) {
        if (role == null || role.getRoleId() == null) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        Long roleId = role.getRoleId();
        ensureRoleEditable(roleId);

        Set<Long> existing = this.roleResourceMapper.selectList(
                        new LambdaQueryWrapper<RoleResourceEntity>().eq(RoleResourceEntity::getRoleId, roleId))
                .stream()
                .map(RoleResourceEntity::getResourceId)
                .collect(Collectors.toSet());

        Set<Long> incoming = sanitizeIds(role.getResourceId());
        if (this.evaConfig.getTenant().isSchemaMode()) {
            ensureSchemaResourcesAuthorized(incoming);
        } else {
            ensureResourcesValid(incoming);
            ensureTenantResourcesAuthorized(incoming);
        }

        Set<Long> toInsert = new HashSet<>(incoming);
        toInsert.removeAll(existing);
        Set<Long> toDelete = new HashSet<>(existing);
        toDelete.removeAll(incoming);

        if (!toDelete.isEmpty()) {
            this.roleResourceMapper.delete(new LambdaQueryWrapper<RoleResourceEntity>()
                    .eq(RoleResourceEntity::getRoleId, roleId)
                    .in(RoleResourceEntity::getResourceId, toDelete));
        }
        if (!toInsert.isEmpty()) {
            for (Long resourceId : toInsert) {
                RoleResourceEntity ref = new RoleResourceEntity();
                ref.setRoleId(roleId);
                ref.setResourceId(resourceId);
                this.roleResourceMapper.insert(ref);
            }
        }

        if (!toDelete.isEmpty() || !toInsert.isEmpty()) {
            this.eventPublisher.publishEvent(new ModuleResourceChangedEvent(this, roleId, ChangeReason.ROLE_RESOURCE_CHANGED));
            incrementPermVer(fetchUsersByRoleIds(Set.of(roleId)));
        }
    }

    /**
     * 获取角色绑定用户列表。
     *
     * @param roleId 角色ID
     * @param deptId 部门ID
     * @return 角色授权用户
     */
    @Override
    @Transactional(readOnly = true)
    @TenantSchema
    public RoleGrantedUserVo listUser(Long roleId, Long deptId) {
        if (roleId == null) {
            CommonCodes.PARAM_ERROR.newException();
            return null;
        }
        ensureRoleExists(roleId);

        LambdaQueryWrapper<UserEntity> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(deptId != null && deptId != 0L, UserEntity::getDeptId, deptId);
        userWrapper.eq(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN);

        List<UserEntity> users = this.userMapper.selectList(userWrapper);
        Set<Long> visibleUserIds = users.stream().map(UserEntity::getId).collect(Collectors.toSet());

        LambdaQueryWrapper<RoleUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleUserEntity::getRoleId, roleId);
        wrapper.select(RoleUserEntity::getUserId);

        Set<Long> checkedUser = this.roleUserMapper.selectObjs(wrapper)
                .stream()
                .map(o -> (Long) o)
                .filter(visibleUserIds::contains)
                .collect(Collectors.toSet());

        return this.roleConvert.toGrantedUserVo(this.userConvert.entityToSimpleVo(users), checkedUser);
    }

    /**
     * 保存角色-用户关系，采用 diff 模式避免无意义删除插入。
     *
     * @param role 角色用户授权参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @TenantSchema
    public void grantUser(RoleUserRefBo role) {
        if (role == null || role.getRoleId() == null) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        Long roleId = role.getRoleId();
        ensureRoleEditable(roleId);

        Set<Long> oldUsers = fetchUsersByRoleIds(Set.of(roleId));
        Set<Long> incoming = sanitizeIds(role.getUserId());
        ensureUsersValid(incoming);

        // 只允许撤销当前数据权限范围内的用户，范围外既有授权必须保持不变。
        Set<Long> visibleOldUsers = fetchVisibleUserIds(oldUsers);

        Set<Long> toInsert = new HashSet<>(incoming);
        toInsert.removeAll(oldUsers);
        Set<Long> toDelete = new HashSet<>(visibleOldUsers);
        toDelete.removeAll(incoming);

        Set<Long> affectedUsers = new HashSet<>(toDelete);
        affectedUsers.addAll(toInsert);

        if (!toDelete.isEmpty()) {
            this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>()
                    .eq(RoleUserEntity::getRoleId, roleId)
                    .in(RoleUserEntity::getUserId, toDelete));
        }
        if (!toInsert.isEmpty()) {
            for (Long userId : toInsert) {
                RoleUserEntity ref = new RoleUserEntity();
                ref.setRoleId(roleId);
                ref.setUserId(userId);
                this.roleUserMapper.insert(ref);
            }
        }

        if (!toDelete.isEmpty() || !toInsert.isEmpty()) {
            incrementPermVer(affectedUsers);
        }
    }

    private void ensureRoleExists(Long roleId) {
        if (roleId == null || this.mapper.selectById(roleId) == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(roleId);
        }
    }

    private void ensureRoleEditable(Long roleId) {
        RoleEntity role = this.mapper.selectById(roleId);
        if (role == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(roleId);
            return;
        }
        if (role.getFrozen() == FrozenEnumm.READ_ONLY) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private void ensureRolesEditable(Set<Long> roleIds) {
        if (CollUtils.isEmpty(roleIds)) {
            return;
        }
        Long readOnlyCount = this.mapper.selectCount(new LambdaQueryWrapper<RoleEntity>()
                .in(RoleEntity::getId, roleIds)
                .eq(RoleEntity::getFrozen, FrozenEnumm.READ_ONLY));
        if (readOnlyCount != null && readOnlyCount > 0L) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private void ensureResourcesValid(Set<Long> resourceIds) {
        if (CollUtils.isEmpty(resourceIds)) {
            return;
        }
        Set<Long> validIds = this.roleResourceMapper.selectValidResourceIds(resourceIds);
        if (validIds == null || validIds.size() != resourceIds.size()) {
            CommonCodes.PARAM_ERROR.newException();
        }
    }

    private void ensureTenantResourcesAuthorized(Set<Long> resourceIds) {
        if (CollUtils.isEmpty(resourceIds)) {
            return;
        }
        long tenantId = ThreadUserHelper.getTenantId();
        if (tenantId <= 0L) {
            return;
        }
        Set<Long> authorizedIds = this.tenantResourceMapper.selectAuthorizedResourceIds(tenantId);
        if (authorizedIds == null || !authorizedIds.containsAll(resourceIds)) {
            CommonCodes.PARAM_ERROR.newException();
        }
    }

    private void ensureUsersValid(Set<Long> userIds) {
        if (CollUtils.isEmpty(userIds)) {
            return;
        }
        Set<Long> validIds = this.userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                        .select(UserEntity::getId)
                        .in(UserEntity::getId, userIds)
                        .eq(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN))
                .stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());
        if (validIds.size() != userIds.size()) {
            CommonCodes.PARAM_ERROR.newException();
        }
    }

    private RoleGrantedModuleVo fetchSchemaModeResources(Long roleId) {
        Set<Long> selectedIds = this.roleResourceMapper.selectList(
                        new LambdaQueryWrapper<RoleResourceEntity>()
                                .select(RoleResourceEntity::getResourceId)
                                .eq(RoleResourceEntity::getRoleId, roleId))
                .stream()
                .map(RoleResourceEntity::getResourceId)
                .collect(Collectors.toSet());
        long tenantId = ThreadUserHelper.getTenantId();
        CoreGrantOptions options = this.coreSchemaExecutor.execute(jdbcTemplate -> {
            Set<Long> authorizedIds = safeSet(this.tenantResourceMapper.selectAuthorizedResourceIds(tenantId));
            List<ModuleResources> resources = authorizedIds.isEmpty()
                    ? List.of()
                    : this.roleResourceMapper.selectResourcesByIds(authorizedIds);
            List<ModuleEntity> modules = resources.isEmpty()
                    ? List.of()
                    : this.moduleMapper.selectList(new LambdaQueryWrapper<ModuleEntity>()
                            .eq(ModuleEntity::getFrozen, FrozenEnumm.UN_FROZEN));
            return new CoreGrantOptions(modules, resources, authorizedIds);
        });

        Set<Long> effectiveSelectedIds = new HashSet<>(selectedIds);
        effectiveSelectedIds.retainAll(options.authorizedIds);
        options.resources.forEach(resource -> resource.setChecked(
                effectiveSelectedIds.contains(resource.getId()) ? 1 : 0));
        return buildGrantedModuleVo(options.modules, options.resources);
    }

    private RoleGrantedModuleVo buildGrantedModuleVo(List<ModuleEntity> modules, List<ModuleResources> resources) {
        Set<Long> visibleModuleIds = resources.stream()
                .map(ModuleResources::getMainId)
                .collect(Collectors.toSet());
        Map<Long, ModuleEntity> allModules = modules.stream()
                .collect(Collectors.toMap(ModuleEntity::getId, item -> item, (left, right) -> left));
        Set<Long> pending = new HashSet<>(visibleModuleIds);
        while (!pending.isEmpty()) {
            Long moduleId = pending.iterator().next();
            pending.remove(moduleId);
            ModuleEntity module = allModules.get(moduleId);
            if (module != null && module.getPid() != null && module.getPid() != 0L
                    && visibleModuleIds.add(module.getPid())) {
                pending.add(module.getPid());
            }
        }
        Map<Long, ModuleDetailVo> moduleMap = this.moduleConvert.entityToDetailVo(modules.stream()
                        .filter(module -> visibleModuleIds.contains(module.getId()))
                        .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(ModuleDetailVo::getId, item -> item,
                        (left, right) -> left, LinkedHashMap::new));
        List<ModuleResourcesVo> resourceVos = this.moduleConvert.resourceEntityToVo(resources);
        Map<Long, List<ModuleResourcesVo>> resourceMap = resourceVos.stream()
                .collect(Collectors.groupingBy(ModuleResourcesVo::getMainId, LinkedHashMap::new, Collectors.toList()));
        Set<Long> moduleChecked = new HashSet<>();
        resourceMap.forEach((moduleId, moduleResources) -> {
            ModuleDetailVo module = moduleMap.get(moduleId);
            if (module != null) {
                module.setResources(moduleResources);
                if (moduleResources.stream().anyMatch(this::isCheckedResource)) {
                    moduleChecked.add(moduleId);
                }
            }
        });
        return this.roleConvert.toGrantedModuleVo(TreeHelper.buildTree(moduleMap.values()), moduleChecked);
    }

    private void ensureSchemaResourcesAuthorized(Set<Long> resourceIds) {
        if (CollUtils.isEmpty(resourceIds)) {
            return;
        }
        long tenantId = ThreadUserHelper.getTenantId();
        this.coreSchemaExecutor.execute(jdbcTemplate -> {
            Set<Long> authorizedIds = safeSet(this.tenantResourceMapper.selectAuthorizedResourceIds(tenantId));
            if (!authorizedIds.containsAll(resourceIds)) {
                CommonCodes.PARAM_ERROR.newException();
            }
            Set<Long> validIds = safeSet(this.roleResourceMapper.selectValidResourceIds(resourceIds));
            if (validIds.size() != resourceIds.size()) {
                CommonCodes.PARAM_ERROR.newException();
            }
            return null;
        });
    }

    private Set<Long> safeSet(Set<Long> ids) {
        return ids == null ? Set.of() : ids;
    }

    private Set<Long> fetchVisibleUserIds(Set<Long> userIds) {
        if (CollUtils.isEmpty(userIds)) {
            return new HashSet<>();
        }
        return this.userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                        .select(UserEntity::getId)
                        .in(UserEntity::getId, userIds))
                .stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());
    }

    private boolean isDuplicateName(RoleAoeBo bo) {
        return this.mapper.selectCount(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getName, bo.getName().trim())
                .ne(bo.getId() != null && bo.getId() != 0L, RoleEntity::getId, bo.getId())) > 0;
    }

    private boolean isCheckedResource(ModuleResourcesVo resource) {
        return resource != null && resource.getChecked() != null && resource.getChecked() == 1;
    }

    private Set<Long> sanitizeIds(Collection<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return new HashSet<>();
        }
        return ids.stream()
                .filter(id -> id != null && id != 0L)
                .collect(Collectors.toSet());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Set<Long> fetchUsersByRoleIds(Set<Long> roleIds) {
        if (CollUtils.isEmpty(roleIds)) {
            return new HashSet<>();
        }
        LambdaQueryWrapper<RoleUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RoleUserEntity::getRoleId, roleIds);
        wrapper.select(RoleUserEntity::getUserId);
        return this.roleUserMapper.selectObjs(wrapper)
                .stream()
                .map(o -> (Long) o)
                .collect(Collectors.toSet());
    }

    private void incrementPermVer(Set<Long> userIds) {
        if (CollUtils.isEmpty(userIds)) {
            return;
        }
        for (Long userId : userIds) {
            this.userMapper.incrementPermVer(userId);
        }
    }

    private static final class CoreGrantOptions {
        private final List<ModuleEntity> modules;
        private final List<ModuleResources> resources;
        private final Set<Long> authorizedIds;

        private CoreGrantOptions(List<ModuleEntity> modules, List<ModuleResources> resources,
                                 Set<Long> authorizedIds) {
            this.modules = modules;
            this.resources = resources;
            this.authorizedIds = authorizedIds;
        }
    }
}
