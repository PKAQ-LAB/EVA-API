package org.pkaq.sys.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.event.ModuleResourceChangedEvent;
import org.pkaq.core.event.ModuleResourceChangedEvent.ChangeReason;
import org.pkaq.core.event.UserOfflineEvent;
import org.pkaq.core.event.UserOfflineEvent.OfflineReason;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.util.CollUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.bo.TenantCheckBo;
import org.pkaq.sys.tenant.bo.TenantQueryBo;
import org.pkaq.sys.tenant.convert.TenantConvert;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.entity.TenantResourceEntity;
import org.pkaq.sys.tenant.mapper.TenantResourceMapper;
import org.pkaq.sys.tenant.mapper.TenantAuthorizationMapper;
import org.pkaq.sys.tenant.vo.TenantDetailVo;
import org.pkaq.sys.tenant.vo.TenantListVo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.bo.UserTenantAdminBo;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 租户管理服务。
 * <p>
 * 维护租户基础信息、租户管理员、授权用户数、授权时间、授权资源和用户下线事件。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class TenantService extends StdService<org.pkaq.sys.tenant.mapper.TenantMapper, TenantEntity> implements ITenantService {

    private final UserMapper userMapper;
    private final UserService userService;
    private final TenantResourceMapper tenantResourceMapper;
    private final TenantAuthorizationMapper tenantAuthorizationMapper;
    private final RoleResourceMapper roleResourceMapper;
    private final TenantConvert convert;
    private final ApplicationEventPublisher eventPublisher;
    private final TenantSchemaProvisioner tenantSchemaProvisioner;
    private final TenantLocalAdminService tenantLocalAdminService;
    private final TenantPrivateAccountService tenantPrivateAccountService;
    private final EvaConfig evaConfig;

    /**
     * 切换租户冻结状态，并级联冻结或解冻租户下普通用户。
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void switchFrozen(SingleArray<Long> ids) {
        if (ids == null || CollUtils.isEmpty(ids.getParam())) {
            return;
        }
        for (Long id : ids.getParam()) {
            TenantEntity self = this.mapper.selectById(id);
            if (self == null || self.getFrozen() == FrozenEnumm.READ_ONLY) {
                continue;
            }
            FrozenEnumm target = self.getFrozen() == FrozenEnumm.FROZEN
                    ? FrozenEnumm.UN_FROZEN
                    : FrozenEnumm.FROZEN;

            // 业务处理
            this.mapper.update(null, new LambdaUpdateWrapper<TenantEntity>()
                    .eq(TenantEntity::getId, id)
                    .set(TenantEntity::getFrozen, target));

            // schema 模式仅使旧令牌失效，避免解冻租户时误解冻人工冻结的用户。
            if (isSchemaTenantMode()) {
                Set<Long> affectedUsers = tenantPrivateAccountService.incrementAllPermVersions(id);
                if (target == FrozenEnumm.FROZEN) {
                    eventPublisher.publishEvent(new UserOfflineEvent(
                            this, id, affectedUsers, OfflineReason.TENANT_FROZEN));
                }
            } else {
                cascadeUserFrozen(id, target);
            }
        }
    }

    /**
     * 删除租户，并清理租户下用户后发布用户下线事件。
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return;
        }

        ensureTenantsEditable(ids);

        for (Long tenantId : ids) {
            Set<Long> affectedUsers;
            if (isSchemaTenantMode()) {
                // 必须在 core 租户记录删除前解析并进入目标 schema。
                affectedUsers = tenantPrivateAccountService.softDeleteTenantAccounts(tenantId);
            } else {
                affectedUsers = this.userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                                .select(UserEntity::getId)
                                .eq(UserEntity::getTenantId, tenantId))
                        .stream()
                        .map(UserEntity::getId)
                        .collect(Collectors.toSet());
                this.userMapper.delete(Wrappers.<UserEntity>lambdaUpdate()
                        .eq(UserEntity::getTenantId, tenantId));
            }
            this.mapper.deleteById(tenantId);
            eventPublisher.publishEvent(new UserOfflineEvent(
                    this, tenantId, affectedUsers, OfflineReason.TENANT_DELETED));
        }
    }

    /**
     * 新增或编辑租户，并同步租户授权资源。
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(TenantAoeBo editBo) {
        if (editBo == null) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }

        boolean isNew = editBo.getId() == null || editBo.getId() == 0L;
        if (isNew && StrUtils.isBlank(editBo.getAdminPass())) {
            CommonCodes.PARAM_ERROR.newException();
        }
        long tenantId = isNew ? IdWorker.getId() : editBo.getId();
        editBo.setId(tenantId);
        if (!isNew) {
            ensureTenantEditable(this.mapper.selectById(tenantId));
        }
        validateTenantAuth(editBo);

        TenantEntity entity = this.convert.boToEntity(editBo);
        if (isNew) {
            long adminId = IdWorker.getId();
            entity.setAdminId(adminId);
            this.mapper.insert(entity);
            this.tenantSchemaProvisioner.provision(tenantId);

            if (isSchemaTenantMode()) {
                tenantPrivateAccountService.createAdministrator(
                        tenantId, adminId, editBo.getAdminAccount(), editBo.getAdminPass());
            } else {
                UserTenantAdminBo admin = new UserTenantAdminBo();
                admin.setId(adminId);
                admin.setAccount(editBo.getAdminAccount());
                admin.setPassword(editBo.getAdminPass());
                admin.setTenantId(tenantId);
                this.userService.createTenantAdmin(admin);
            }
            syncTenantAuthorization(tenantId, editBo);
            return;
        }

        entity.setCode(null);
        entity.setAdminId(null);

        TenantEntity origin = this.mapper.selectOne(new LambdaQueryWrapper<TenantEntity>()
                .select(TenantEntity::getAuthUserCount)
                .eq(TenantEntity::getId, tenantId));
        int originAuthUserCount = origin == null ? 0 : origin.getAuthUserCount();

        this.mapper.updateById(entity);
        if (editBo.getAuthUserCount() != originAuthUserCount) {
            handleAuthUserCountChanged(tenantId, editBo.getAuthUserCount());
        }
        if (editBo.getResourceIds() != null || editBo.getRoleIds() != null || editBo.getPackageIds() != null) {
            syncTenantAuthorization(tenantId, editBo);
        }
        if (isExpired(editBo.getExpirationDate())) {
            offlineTenantUsers(tenantId, OfflineReason.TENANT_FROZEN);
        }
    }

    /**
     * 查询租户详情。
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public TenantDetailVo get(Long id) {
        TenantEntity entity = this.mapper.selectById(id);
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(id);
            return null;
        }
        TenantDetailVo vo = this.convert.entityToVo(entity);
        if (isSchemaTenantMode()) {
            vo.setAdminAccount(tenantPrivateAccountService.findAdministratorAccount(id, entity.getAdminId()));
        } else {
            Optional.ofNullable(this.userMapper.selectById(entity.getAdminId()))
                    .ifPresent(admin -> vo.setAdminAccount(admin.getAccount()));
        }
        vo.setResourceIds(this.tenantResourceMapper.selectAuthorizedResourceIds(id));
        vo.setRoleIds(this.tenantAuthorizationMapper.selectGrantedRoleIds(id));
        vo.setPackageIds(this.tenantAuthorizationMapper.selectGrantedPackageIds(id));
        return vo;
    }

    /**
     * 分页查询租户列表。
     */
    public PageVo<TenantListVo> listPage(TenantQueryBo queryBo) {
        LambdaQueryWrapper<TenantEntity> wrapper = new LambdaQueryWrapper<>();
        if (queryBo != null) {
            wrapper.like(queryBo.getName() != null && !queryBo.getName().isEmpty(),
                    TenantEntity::getName, queryBo.getName());
            wrapper.like(queryBo.getCode() != null && !queryBo.getCode().isEmpty(),
                    TenantEntity::getCode, queryBo.getCode());
            wrapper.like(queryBo.getCardNo() != null && !queryBo.getCardNo().isEmpty(),
                    TenantEntity::getCardNo, queryBo.getCardNo());
            wrapper.like(queryBo.getContactName() != null && !queryBo.getContactName().isEmpty(),
                    TenantEntity::getContactName, queryBo.getContactName());
        }
        wrapper.orderByDesc(TenantEntity::getUtcModify);

        int pageNo = queryBo == null ? 1 : queryBo.getPageNo();
        int pageSize = queryBo == null ? 10 : queryBo.getPageSize();

        PageResult<TenantEntity> pagination = new PageResult<>(pageNo, pageSize);
        return this.mapper.selectPage(pagination, wrapper).map(this.convert::entityToListVo);
    }

    /**
     * 校验租户 code 或 name 是否重复。
     */
    public boolean checkUnique(TenantCheckBo checkBo) {
        if (checkBo == null) {
            return false;
        }
        LambdaQueryWrapper<TenantEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.nested(w -> w
                .eq(checkBo.getName() != null && !checkBo.getName().isEmpty(), TenantEntity::getName, checkBo.getName())
                .or()
                .eq(checkBo.getCode() != null && !checkBo.getCode().isEmpty(), TenantEntity::getCode, checkBo.getCode()));

        if (checkBo.getId() != null && checkBo.getId() != 0L) {
            wrapper.ne(TenantEntity::getId, checkBo.getId());
        }
        return this.mapper.selectCount(wrapper) > 0;
    }

    // ------------------------------------------------------------------
    // 私有辅助方法
    // ------------------------------------------------------------------

    /**
     * 级联冻结或解冻租户下的普通用户。
     */
    private void cascadeUserFrozen(Long tenantId, FrozenEnumm target) {
        if (target == FrozenEnumm.FROZEN) {
            List<UserEntity> toFrozen = this.userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                    .select(UserEntity::getId)
                    .eq(UserEntity::getTenantId, tenantId)
                    .eq(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN));

            this.userMapper.update(null, new LambdaUpdateWrapper<UserEntity>()
                    .eq(UserEntity::getTenantId, tenantId)
                    .eq(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN)
                    .set(UserEntity::getFrozen, FrozenEnumm.FROZEN));

            Set<Long> uids = toFrozen.stream().map(UserEntity::getId).collect(Collectors.toSet());
            eventPublisher.publishEvent(new UserOfflineEvent(this, tenantId, uids, OfflineReason.TENANT_FROZEN));
        } else if (target == FrozenEnumm.UN_FROZEN) {
            this.userMapper.update(null, new LambdaUpdateWrapper<UserEntity>()
                    .eq(UserEntity::getTenantId, tenantId)
                    .eq(UserEntity::getFrozen, FrozenEnumm.FROZEN)
                    .set(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN));
        }
    }

    /**
     * 租户授权用户数下调时冻结超额普通用户。
     */
    private void handleAuthUserCountChanged(Long tenantId, int authUserCount) {
        if (isSchemaTenantMode()) {
            TenantEntity tenant = this.mapper.selectById(tenantId);
            if (tenant == null || tenant.getAdminId() == null) {
                throw new IllegalStateException("租户管理员不存在");
            }
            Set<Long> frozenUserIds = tenantPrivateAccountService.enforceUserLimit(
                    tenantId, tenant.getAdminId(), authUserCount);
            if (!frozenUserIds.isEmpty()) {
                eventPublisher.publishEvent(new UserOfflineEvent(
                        this, tenantId, frozenUserIds, OfflineReason.TENANT_FROZEN));
            }
            return;
        }
        int maxActiveCount = Math.max(authUserCount, 0);
        List<UserEntity> users = this.userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                .select(UserEntity::getId, UserEntity::getFrozen)
                .eq(UserEntity::getTenantId, tenantId)
                .orderByDesc(UserEntity::getUtcCreate)
                .orderByDesc(UserEntity::getId));

        long activeCount = users.stream()
                .filter(user -> user.getFrozen() != FrozenEnumm.FROZEN)
                .count();
        int overflowCount = (int) (activeCount - maxActiveCount);
        if (overflowCount <= 0) {
            return;
        }

        Set<Long> frozenUserIds = users.stream()
                .filter(user -> user.getFrozen() == FrozenEnumm.UN_FROZEN)
                .limit(overflowCount)
                .map(UserEntity::getId)
                .collect(Collectors.toSet());
        if (frozenUserIds.isEmpty()) {
            return;
        }

        this.userMapper.update(null, new LambdaUpdateWrapper<UserEntity>()
                .in(UserEntity::getId, frozenUserIds)
                .set(UserEntity::getFrozen, FrozenEnumm.FROZEN));
        eventPublisher.publishEvent(new UserOfflineEvent(
                this, tenantId, frozenUserIds, OfflineReason.TENANT_FROZEN));
    }

    /**
     * 校验租户授权参数。
     */
    private void validateTenantAuth(TenantAoeBo bo) {
        if (bo.getAuthUserCount() < 1 || bo.getExpirationDate() == null) {
            CommonCodes.PARAM_ERROR.newException();
        }
        Set<Long> resourceIds = sanitizeIds(bo.getResourceIds());
        if (!resourceIds.isEmpty()) {
            Set<Long> validIds = this.roleResourceMapper.selectValidResourceIds(resourceIds);
            if (validIds == null || validIds.size() != resourceIds.size()) {
                CommonCodes.PARAM_ERROR.newException();
            }
        }
    }

    /**
     * 同步租户授权资源，并清理租户角色中越界的资源授权。
     */
    private void syncTenantResources(Long tenantId, Set<Long> resourceIds) {
        this.tenantResourceMapper.delete(new LambdaQueryWrapper<TenantResourceEntity>()
                .eq(TenantResourceEntity::getTenantId, tenantId));
        for (Long resourceId : resourceIds) {
            TenantResourceEntity entity = new TenantResourceEntity();
            entity.setTenantId(tenantId);
            entity.setResourceId(resourceId);
            this.tenantResourceMapper.insert(entity);
        }

        if (isSchemaTenantMode()) {
            tenantPrivateAccountService.pruneRoleResources(tenantId, resourceIds);
        } else {
            this.tenantResourceMapper.deleteUnauthorizedRoleResources(tenantId, resourceIds);
        }
        notifyTenantPermissionChanged(tenantId);
    }

    /**
     * 同步角色模板与套餐授权，并重新物化租户能力。
     */
    private void syncTenantAuthorization(Long tenantId, TenantAoeBo bo) {
        if (usesLegacyResourceMode(bo)) {
            syncTenantResources(tenantId, sanitizeIds(bo.getResourceIds()));
            synchronizeTenantAdministrator(tenantId);
            return;
        }
        Set<Long> roleIds = bo.getRoleIds() == null
                ? tenantAuthorizationMapper.selectGrantedRoleIds(tenantId)
                : sanitizeIds(bo.getRoleIds());
        Set<Long> packageIds = bo.getPackageIds() == null
                ? tenantAuthorizationMapper.selectGrantedPackageIds(tenantId)
                : sanitizeIds(bo.getPackageIds());
        validateAuthorizationSources(roleIds, packageIds);

        tenantAuthorizationMapper.revokeRoleGrants(tenantId, roleIds);
        if (!roleIds.isEmpty()) {
            tenantAuthorizationMapper.grantRoles(tenantId, roleIds);
        }
        tenantAuthorizationMapper.revokePackageGrants(tenantId, packageIds);
        if (!packageIds.isEmpty()) {
            tenantAuthorizationMapper.grantPackages(tenantId, packageIds);
        }
        tenantAuthorizationMapper.clearTenantResources(tenantId);
        tenantAuthorizationMapper.insertDerivedTenantResources(tenantId);
        if (!isSchemaTenantMode()) {
            tenantAuthorizationMapper.ensureTenantAdminRole(tenantId);
            tenantAuthorizationMapper.bindTenantAdminRole(tenantId);
        }

        // resourceIds 是管理员对角色与套餐能力并集的裁剪，不能用于扩权。
        if (bo.getResourceIds() != null) {
            Set<Long> requested = sanitizeIds(bo.getResourceIds());
            Set<Long> derived = tenantResourceMapper.selectAuthorizedResourceIds(tenantId);
            if (!derived.containsAll(requested)) {
                CommonCodes.PARAM_ERROR.newException();
            }
            syncTenantResources(tenantId, requested);
            if (isSchemaTenantMode()) {
                synchronizeTenantAdministrator(tenantId);
            } else {
                tenantAuthorizationMapper.clearTenantAdminResources(tenantId);
                tenantAuthorizationMapper.refreshTenantAdminResources(tenantId);
            }
            return;
        }
        synchronizeTenantAdministrator(tenantId);
        notifyTenantPermissionChanged(tenantId);
    }

    private void synchronizeTenantAdministrator(Long tenantId) {
        if (isSchemaTenantMode()) {
            TenantEntity tenant = this.mapper.selectById(tenantId);
            if (tenant == null || tenant.getAdminId() == null) {
                throw new IllegalStateException("租户管理员不存在");
            }
            tenantLocalAdminService.synchronize(tenantId, tenant.getAdminId());
            return;
        }
        tenantAuthorizationMapper.ensureTenantAdminRole(tenantId);
        tenantAuthorizationMapper.bindTenantAdminRole(tenantId);
        tenantAuthorizationMapper.clearTenantAdminResources(tenantId);
        tenantAuthorizationMapper.refreshTenantAdminResources(tenantId);
    }

    private boolean isSchemaTenantMode() {
        return evaConfig.getTenant().isSchemaMode();
    }

    private void ensureTenantsEditable(Set<Long> ids) {
        long readOnlyCount = this.mapper.selectCount(new LambdaQueryWrapper<TenantEntity>()
                .in(TenantEntity::getId, ids)
                .eq(TenantEntity::getFrozen, FrozenEnumm.READ_ONLY));
        if (readOnlyCount > 0) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private void ensureTenantEditable(TenantEntity entity) {
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException();
            return;
        }
        if (entity.getFrozen() == FrozenEnumm.READ_ONLY) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    /**
     * 旧客户端只提交 resourceIds 时继续使用直接授权真源。
     */
    static boolean usesLegacyResourceMode(TenantAoeBo bo) {
        return bo != null && bo.getRoleIds() == null && bo.getPackageIds() == null;
    }

    private void validateAuthorizationSources(Set<Long> roleIds, Set<Long> packageIds) {
        if (!roleIds.isEmpty() && !tenantAuthorizationMapper.selectInvalidTemplateRoleIds(roleIds).isEmpty()) {
            CommonCodes.PARAM_ERROR.newException();
        }
        if (!packageIds.isEmpty() && !tenantAuthorizationMapper.selectInvalidPackageIds(packageIds).isEmpty()) {
            CommonCodes.PARAM_ERROR.newException();
        }
    }

    private void notifyTenantPermissionChanged(Long tenantId) {
        if (isSchemaTenantMode()) {
            Set<Long> roleIds = tenantPrivateAccountService.pruneRoleResources(
                    tenantId, tenantResourceMapper.selectAuthorizedResourceIds(tenantId));
            if (!roleIds.isEmpty()) {
                eventPublisher.publishEvent(new ModuleResourceChangedEvent(
                        this, roleIds, ChangeReason.ROLE_RESOURCE_CHANGED));
            }
            tenantPrivateAccountService.incrementAllPermVersions(tenantId);
            return;
        }
        Set<Long> roleIds = tenantResourceMapper.selectTenantRoleIds(tenantId);
        if (!CollUtils.isEmpty(roleIds)) {
            eventPublisher.publishEvent(new ModuleResourceChangedEvent(
                    this, roleIds, ChangeReason.ROLE_RESOURCE_CHANGED));
        }
        incrementTenantUserPermVer(tenantId);
    }

    private Set<Long> sanitizeIds(List<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return new HashSet<>();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0L)
                .collect(Collectors.toSet());
    }

    private boolean isExpired(java.util.Date expirationDate) {
        return expirationDate != null && expirationDate.before(new java.util.Date());
    }

    private void incrementTenantUserPermVer(Long tenantId) {
        if (isSchemaTenantMode()) {
            tenantPrivateAccountService.incrementAllPermVersions(tenantId);
            return;
        }
        Set<Long> userIds = this.tenantResourceMapper.selectTenantUserIds(tenantId);
        if (CollUtils.isEmpty(userIds)) {
            return;
        }
        for (Long userId : userIds) {
            this.userMapper.incrementPermVer(userId);
        }
    }

    private void offlineTenantUsers(Long tenantId, OfflineReason reason) {
        Set<Long> userIds = isSchemaTenantMode()
                ? tenantPrivateAccountService.listActiveUserIds(tenantId)
                : this.tenantResourceMapper.selectTenantUserIds(tenantId);
        if (!CollUtils.isEmpty(userIds)) {
            eventPublisher.publishEvent(new UserOfflineEvent(this, tenantId, userIds, reason));
        }
    }
}
