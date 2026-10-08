package org.pkaq.sys.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.account.IAccountCreation;
import org.pkaq.core.account.AccountCreationCommand;
import org.pkaq.core.account.AccountCreationException;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.event.UserOfflineEvent;
import org.pkaq.core.event.UserOfflineEvent.OfflineReason;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mvc.entity.Entity;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mvc.vo.Vo;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.tenant.TenantSchema;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.upload.provider.FileProvider;
import org.pkaq.core.util.BCryptUtils;
import org.pkaq.core.util.CollUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.module.service.ModuleService;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.post.entity.PostUserEntity;
import org.pkaq.sys.post.mapper.PostUserMapper;
import org.pkaq.sys.post.service.UserPostRefSerivce;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.role.service.UserRoleRefSerivce;
import org.pkaq.sys.user.bo.RePwdBo;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.bo.UserCheckBo;
import org.pkaq.sys.user.bo.UserGrantBo;
import org.pkaq.sys.user.bo.UserPostBo;
import org.pkaq.sys.user.bo.UserQueryBo;
import org.pkaq.sys.user.bo.UserTenantAdminBo;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.entity.AccountProfileEntity;
import org.pkaq.sys.user.mapper.AccountProfileMapper;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.pkaq.sys.user.vo.UserListVo;
import org.pkaq.sys.user.vo.UserResourceVo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户管理服务
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserService extends StdService<UserMapper, UserEntity> implements IUserService {

    /** 系统内置管理员账号编码，前端按 frozen = 9999 展示为灰色不可编辑行。 */
    private static final String SYSTEM_ADMIN_CODE = "9999";

    private final UserRoleRefSerivce userRoleRefSerivce;
    private final UserPostRefSerivce userPostRefSerivce;
    private final FileProvider fileProvider;
    private final RoleUserMapper roleUserMapper;
    private final PostUserMapper postUserMapper;
    private final OrganizationMapper organizationMapper;
    private final ModuleService moduleService;
    private final ModuleConvert moduleConvert;
    private final UserConvert convert;
    private final EvaConfig evaConfig;
    private final ApplicationEventPublisher eventPublisher;
    private final CoreSchemaExecutor coreSchemaExecutor;
    private final RoleResourceMapper roleResourceMapper;
    private final IAccountCreation accountCreation;
    private final AccountProfileMapper accountProfileMapper;

    /**
     * 校验账号合法性。
     *
     * @param username 用户账号
     */
    public void validateUsername(String username) {
        try {
            accountCreation.validateAccount(username);
        } catch (AccountCreationException exception) {
            throw new BizException(SysCodes.USER_ACCOUNT_ILLEGAL);
        }
    }

    /**
     * 修改当前用户密码。
     *
     * @param rePwdBo 修改密码参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @TenantSchema
    public void repwd(RePwdBo rePwdBo) {
        Long uid = ThreadUserHelper.getUserId();

        UserEntity userEntity = this.mapper.selectById(uid);
        if (userEntity == null) {
            SysCodes.CANNOT_FIND_USER.newException();
            return;
        }

        if (!BCryptUtils.checkpw(rePwdBo.getOriginPassword(), userEntity.getPassword())) {
            SysCodes.BAD_ORG_PASSWORD.newException();
        }

        UserEntity updateE = new UserEntity();
        updateE.setId(userEntity.getId());
        updateE.setPassword(BCryptUtils.hashpw(rePwdBo.getNewPassword()));
        updateE.setRevision(rePwdBo.getRevision());
        this.mapper.updateById(updateE);

        this.eventPublisher.publishEvent(new UserOfflineEvent(
                this, ThreadUserHelper.getTenantId(), uid, OfflineReason.PASSWORD_CHANGED));
    }

    /**
     * 批量删除用户，并同步清理用户-角色、用户-岗位关系。
     *
     * @param param 用户ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @TenantSchema
    public void delete(Set<Long> param) {
        Set<Long> userIds = this.sanitizeIds(param);
        if (userIds.isEmpty()) {
            return;
        }
        ensureUsersEditable(userIds);

        this.mapper.deleteByIds(userIds);
        this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>().in(RoleUserEntity::getUserId, userIds));
        this.postUserMapper.delete(new LambdaQueryWrapper<PostUserEntity>().in(PostUserEntity::getUserId, userIds));
        this.eventPublisher.publishEvent(new UserOfflineEvent(
                this, ThreadUserHelper.getTenantId(), userIds, OfflineReason.USER_DELETED));
    }

    /**
     * 查询用户列表。
     *
     * @param queryBo 查询条件
     * @return 用户列表
     */
    @Override
    @Transactional(readOnly = true)
    @TenantSchema
    public List<UserListVo> listUser(UserQueryBo queryBo) {
        return this.convert.entityToListVo(this.mapper.selectManagedList(queryBo,
                sanitizeListIds(queryBo == null ? null : queryBo.getPostId())));
    }

    /**
     * 查询用户列表。
     *
     * @param queryBo 查询条件
     * @param convertFn 转换函数
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    @TenantSchema
    public List<? extends Vo> listUser(UserQueryBo queryBo,
                                       Function<List<? extends Entity>, List<? extends Vo>> convertFn) {
        return convertFn.apply(this.mapper.selectManagedList(queryBo,
                sanitizeListIds(queryBo == null ? null : queryBo.getPostId())));
    }

    /**
     * 分页查询用户列表。
     *
     * @param queryBo 查询条件
     * @return 分页数据
     */
    @Override
    @Transactional(readOnly = true)
    @TenantSchema
    public PageVo<UserListVo> listPage(UserQueryBo queryBo) {
        UserQueryBo safeQuery = queryBo == null ? new UserQueryBo() : queryBo;
        PageResult<UserEntity> pagination = new PageResult<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        return this.mapper.selectManagedPage(pagination, safeQuery, sanitizeListIds(safeQuery.getPostId()))
                .map(this.convert::entityToListVo);
    }

    /**
     * 切换用户冻结状态。
     *
     * @param ids 用户ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @TenantSchema
    public void updateUser(Set<Long> ids) {
        Set<Long> userIds = this.sanitizeIds(ids);
        if (userIds.isEmpty()) {
            CommonCodes.NULL_ID.newException();
            return;
        }
        ensureUsersEditable(userIds);

        Set<Long> willBeFrozen = this.mapper.selectList(new LambdaQueryWrapper<UserEntity>()
                        .select(UserEntity::getId)
                        .in(UserEntity::getId, userIds)
                        .eq(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN))
                .stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());

        this.mapper.change(userIds);
        for (Long userId : userIds) {
            this.mapper.incrementPermVer(userId);
        }

        if (!willBeFrozen.isEmpty()) {
            this.eventPublisher.publishEvent(new UserOfflineEvent(
                    this, ThreadUserHelper.getTenantId(), willBeFrozen, OfflineReason.USER_FROZEN));
        }
    }

    /**
     * 查询用户详情。
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Override
    @Transactional(readOnly = true)
    @TenantSchema
    public UserDetailVo getUser(Long id) {
        UserEntity user = this.mapper.selectById(id);
        if (user == null) {
            SysCodes.CANNOT_FIND_USER.newException();
            return null;
        }

        UserDetailVo vo = this.convert.entityToDetailVo(user);
        vo.setRoleIds(this.roleUserMapper.selectRoleIds(id));
        vo.setPostId(this.userPostRefSerivce.listPostIdsByUserId(id));
        return vo;
    }

    /**
     * 新增或编辑用户。
     *
     * @param user 用户参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @TenantSchema
    public void saveUser(UserAoeBo user) {
        if (user == null) {
            SysCodes.CANNOT_FIND_USER.newException();
            return;
        }

        this.validateUsername(user.getAccount());
        ensureSystemAdminCodeAvailable(user);
        this.ensureUniqueUser(user);
        this.ensureDepartmentUsable(user.getDeptId());

        Long userId = user.getId();
        boolean isInsert = userId == null || userId == 0L;

        if (isInsert) {
            this.ensureTenantUserQuota();
            this.ensurePasswordPresent(user.getPassword());
        } else {
            UserEntity oldUser = this.mapper.selectById(userId);
            if (oldUser == null) {
                SysCodes.CANNOT_FIND_USER.newException();
                return;
            }
            ensureUserEditable(oldUser);
            this.handleEditPasswordAndAvatar(user, oldUser);
        }

        this.storageAvatarThumbnail(user.getAvatar());

        UserEntity entity = this.convert.boToEntity(user);
        if (isInsert) {
            userId = createAccount(entity, user.getPassword());
            user.setId(userId);
            entity.setId(userId);
        } else {
            // 乐观锁或数据范围导致账号更新失败时，不允许单独更新档案及权限版本。
            if (1 != this.mapper.updateById(entity)) {
                throw new IllegalStateException("账号更新失败，请刷新后重试");
            }
            this.mapper.incrementPermVer(userId);
        }
        saveProfile(entity);

        if (user.getRoleIds() != null) {
            UserGrantBo grantBo = new UserGrantBo();
            grantBo.setUserId(userId);
            grantBo.setRoleIds(user.getRoleIds());
            this.userRoleRefSerivce.saveRoles(grantBo);
        }

        if (user.getPostId() != null) {
            UserPostBo postBo = new UserPostBo();
            postBo.setUserId(userId);
            postBo.setPostIds(user.getPostId());
            this.userPostRefSerivce.savePosts(postBo);
        }
    }

    /**
     * 查询用户菜单和资源。
     *
     * @param uid 用户ID
     * @return 用户资源列表
     */
    @Transactional(readOnly = true)
    @TenantSchema
    public List<UserResourceVo> fetchModuleByUid(Long uid) {
        Collection<ModuleDetailVo> modules;
        if (this.evaConfig.getTenant().isSchemaMode()) {
            Set<Long> resourceIds = this.roleResourceMapper.selectResourceIdsByUserId(uid);
            modules = this.coreSchemaExecutor.execute(
                    template -> this.moduleService.fetchModulesByResourceIds(resourceIds));
        } else {
            modules = this.moduleService.fetchUserModules(uid);
        }
        if (modules == null || modules.isEmpty()) {
            return Collections.emptyList();
        }
        return modules.stream()
                .map(this.moduleConvert::detailToUserResourceVo)
                .collect(Collectors.toList());
    }

    /**
     * 校验账号或编码是否重复。
     *
     * @param user 校验参数
     * @return true 表示重复
     */
    @Override
    @Transactional(readOnly = true)
    @TenantSchema
    public boolean checkUnique(UserCheckBo user) {
        if (user == null || (StrUtils.isBlank(user.getAccount()) && StrUtils.isBlank(user.getCode()))) {
            return false;
        }
        return this.mapper.countDuplicate(user.getAccount(), user.getCode(), user.getId()) > 0;
    }

    /**
     * 创建租户管理员。
     *
     * @param user 租户管理员参数
     * @deprecated 租户初始化由可信 Schema 预置服务完成；此兼容入口仅支持已路由的 Schema 租户。
     * @see #saveUser(UserAoeBo)
     */
    @Deprecated
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @TenantSchema
    public void createTenantAdmin(UserTenantAdminBo user) {
        if (!this.evaConfig.isSaasMode() || !this.evaConfig.getTenant().isSchemaMode()) {
            throw new IllegalStateException("租户管理员创建仅支持 Schema 租户模式");
        }
        this.validateUsername(user.getAccount());

        if (StrUtils.isBlank(user.getPassword())) {
            SysCodes.BAD_ORG_PASSWORD.newException();
        }
        UserEntity entity = this.convert.boToEntity(user);
        entity.setTenantId(null);
        entity.setName(user.getAccount());
        entity.setCode(user.getAccount());
        entity.setFrozen(FrozenEnumm.READ_ONLY);
        entity.setId(createAccount(entity, user.getPassword()));
        saveProfile(entity);
    }

    private void ensureUniqueUser(UserAoeBo user) {
        UserCheckBo checkBo = new UserCheckBo();
        checkBo.setId(user.getId());
        checkBo.setAccount(user.getAccount());
        checkBo.setCode(user.getCode());
        if (this.checkUnique(checkBo)) {
            SysCodes.ACCOUNT_OR_CODE_ALREADY_EXIST.newException();
        }
    }

    private void ensureSystemAdminCodeAvailable(UserAoeBo user) {
        if (user == null || !SYSTEM_ADMIN_CODE.equals(user.getCode())) {
            return;
        }
        Long userId = user.getId();
        if (userId == null || userId == 0L) {
            SysCodes.READ_ONLY_RECORD.newException();
            return;
        }
        UserEntity oldUser = this.mapper.selectById(userId);
        if (oldUser == null || !SYSTEM_ADMIN_CODE.equals(oldUser.getCode())) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private void ensureUsersEditable(Set<Long> userIds) {
        if (CollUtils.isEmpty(userIds)) {
            return;
        }
        Long readOnlyCount = this.mapper.countReadOnlyAccounts(userIds);
        if (readOnlyCount != null && readOnlyCount > 0L) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private void ensureUserEditable(UserEntity user) {
        if (user == null) {
            SysCodes.CANNOT_FIND_USER.newException();
            return;
        }
        if (user.getFrozen() == FrozenEnumm.READ_ONLY || SYSTEM_ADMIN_CODE.equals(user.getCode())) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private void ensureDepartmentUsable(Long deptId) {
        if (deptId == null || deptId == 0L) {
            return;
        }

        OrganizationEntity department = this.organizationMapper.selectById(deptId);
        if (department == null || FrozenEnumm.FROZEN == department.getFrozen()) {
            SysCodes.RECORD_NOT_FOUND.newException();
        }
    }

    private void ensureTenantUserQuota() {
        if (!this.evaConfig.isSaasMode()) {
            return;
        }

        if (!this.evaConfig.getTenant().isSchemaMode()) {
            throw new IllegalStateException("租户模式必须启用 Schema 隔离");
        }
        Long tenantId = ThreadUserHelper.getTenantId();
        Long userCount = this.mapper.selectCount(new LambdaQueryWrapper<UserEntity>());
        Integer limit = this.coreSchemaExecutor.execute(template -> template.queryForObject(
                "SELECT AUTH_USER_COUNT FROM SYS_TENANT WHERE ID = ? AND COALESCE(DELETED, 0) = 0",
                Integer.class, tenantId));
        if (limit == null || userCount >= limit) {
            SysCodes.USER_ACCOUNT_LIMIT.newException();
        }
    }

    private void ensurePasswordPresent(String password) {
        if (StrUtils.isBlank(password)) {
            SysCodes.BAD_ORG_PASSWORD.newException();
        }
    }

    private void handleEditPasswordAndAvatar(UserAoeBo user, UserEntity oldUser) {
        String oldAvatar = oldUser.getAvatar();
        if (StrUtils.isNotBlank(oldAvatar) && !oldAvatar.equals(user.getAvatar())) {
            this.fileProvider.delFromStorage(oldAvatar);
        }

        if (StrUtils.isNotBlank(user.getPassword())) {
            user.setPassword(BCryptUtils.hashpw(user.getPassword()));
        } else {
            user.setPassword(null);
        }
    }

    private void storageAvatarThumbnail(String avatar) {
        if (StrUtils.isBlank(avatar)) {
            return;
        }

        try {
            this.fileProvider.storageWithThumbnail(0.3f, avatar);
        } catch (IOException e) {
            throw new BizException(CommonCodes.SERVER_ERROR);
        }
    }

    private Set<Long> sanitizeIds(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return new HashSet<>();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0L)
                .collect(Collectors.toSet());
    }

    private List<Long> sanitizeListIds(List<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0L)
                .distinct()
                .collect(Collectors.toList());
    }

    /** 管理端与注册统一复用账号校验、唯一性和密码哈希。 */
    private Long createAccount(UserEntity entity, String password) {
        AccountCreationCommand command = new AccountCreationCommand();
        command.setId(entity.getId());
        command.setAccount(entity.getAccount());
        command.setPassword(password);
        command.setAvatar(entity.getAvatar());
        command.setNickName(entity.getNickName());
        command.setTel(entity.getTel());
        command.setEmail(entity.getEmail());
        command.setFrozen(entity.getFrozen());
        command.setSort(entity.getSort());
        command.setRemark(entity.getRemark());
        command.setLastIp(entity.getLastIp());
        command.setLastLogin(entity.getLastLogin());
        try {
            return accountCreation.create(command);
        } catch (AccountCreationException exception) {
            switch (exception.getReason()) {
                case INVALID_ACCOUNT -> SysCodes.USER_ACCOUNT_ILLEGAL.newException();
                case MISSING_PASSWORD -> SysCodes.BAD_ORG_PASSWORD.newException();
                case DUPLICATE_ACCOUNT -> SysCodes.ACCOUNT_OR_CODE_ALREADY_EXIST.newException();
            }
            throw exception;
        }
    }

    /** 按账号主键维护可选管理资料，首次管理员编辑可补建档案。 */
    private void saveProfile(UserEntity user) {
        AccountProfileEntity profile = new AccountProfileEntity();
        profile.setAccountId(user.getId());
        profile.setCode(user.getCode());
        profile.setName(user.getName());
        profile.setDeptId(user.getDeptId());
        int affectedRows = accountProfileMapper.selectById(user.getId()) == null
                ? accountProfileMapper.insert(profile) : accountProfileMapper.updateById(profile);
        if (1 != affectedRows) {
            // 资料更新同样属于账号事务，失败必须回滚账号变更。
            throw new IllegalStateException("账号档案更新失败，请刷新后重试");
        }
    }
}
