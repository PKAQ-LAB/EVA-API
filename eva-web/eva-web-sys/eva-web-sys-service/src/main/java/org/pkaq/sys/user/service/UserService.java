package org.pkaq.sys.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.event.UserOfflineEvent;
import org.pkaq.core.event.UserOfflineEvent.OfflineReason;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mvc.entity.Entity;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mvc.vo.Vo;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.upload.provider.FileProvider;
import org.pkaq.core.util.BCryptUtils;
import org.pkaq.core.util.CollUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.post.entity.PostUserEntity;
import org.pkaq.sys.post.mapper.PostUserMapper;
import org.pkaq.sys.post.service.UserPostRefSerivce;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.role.service.UserRoleRefSerivce;
import org.pkaq.sys.user.bo.*;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.pkaq.sys.user.vo.UserListVo;
import org.pkaq.sys.user.vo.UserResourceVo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户管理
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserService extends StdService<UserMapper, UserEntity> implements IUserService {

    /** 系统保留账号 / 非法账号黑名单（不区分大小写） */
    private static final Set<String> ILLEGAL_USERNAMES = new HashSet<>(Arrays.asList(
            "null", "undefined", "true", "false", "admin", "root", "", " ", "\t", "\n"
    ));

    private final UserRoleRefSerivce userRoleRefSerivce;
    private final UserPostRefSerivce userPostRefSerivce;
    private final FileProvider fileProvider;
    private final RoleUserMapper roleUserMapper;
    private final PostUserMapper postUserMapper;
    private final UserConvert convert;
    private final EvaConfig evaConfig;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 账号合法性校验：不允许为黑名单中的保留字
     */
    public void validateUsername(String username) {
        if (username == null || ILLEGAL_USERNAMES.contains(username.trim().toLowerCase())) {
            throw new BizException(SysCodes.USER_ACCOUNT_ILLEGAL);
        }
    }

    /**
     * 修改密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
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

        // 改密后强制下线（事务提交后由 listener 清 token）
        eventPublisher.publishEvent(new UserOfflineEvent(this, uid, OfflineReason.PASSWORD_CHANGED));
    }

    /**
     * 批量删除：
     * - 同步清理 角色-用户、岗位-用户 中间表
     * - 删除后踢被删用户下线
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(Set<Long> param) {
        if (CollUtils.isEmpty(param)) {
            return;
        }
        this.mapper.deleteByIds(param);
        // 清理角色关系
        this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>().in(RoleUserEntity::getUserId, param));
        // 清理岗位关系（U-03 修复）
        this.postUserMapper.delete(new LambdaQueryWrapper<PostUserEntity>().in(PostUserEntity::getUserId, param));
        // 发布下线事件（事务提交后由 listener 清 token）
        eventPublisher.publishEvent(new UserOfflineEvent(this, param, OfflineReason.USER_DELETED));
    }

    /**
     * 查询用户列表 无分页
     */
    @Override
    public List<UserListVo> listUser(UserQueryBo queryBo) {
        UserEntity user = this.convert.boToEntity(queryBo);
        LambdaQueryWrapper<UserEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(user);
        wrapper.orderByDesc(UserEntity::getUtcModify);
        return this.convert.entityToListVo(this.mapper.selectList(wrapper));
    }

    public List<? extends Vo> listUser(UserQueryBo queryBo,
                                       Function<List<? extends Entity>, List<? extends Vo>> convertFn) {
        UserEntity user = this.convert.boToEntity(queryBo);
        LambdaQueryWrapper<UserEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(user);
        wrapper.orderByDesc(UserEntity::getUtcModify);
        return convertFn.apply(this.mapper.selectList(wrapper));
    }

    /**
     * 列表查询 - 分页
     */
    @Override
    public PageVo<UserListVo> listPage(UserQueryBo queryBo) {
        LambdaQueryWrapper<UserEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(this.convert.boToEntity(queryBo));
        wrapper.orderByDesc(UserEntity::getUtcModify);
        PageResult<UserEntity> pagination = new PageResult<>(queryBo.getPageNo(), queryBo.getPageSize());
        return this.mapper.selectPage(pagination, wrapper).map(this.convert::entityToListVo);
    }

    /**
     * 切换冻结状态（解锁 / 锁定）
     * - 翻转 frozen
     * - 切换后若用户处于"冻结"状态则立即踢下线
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void updateUser(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            CommonCodes.NULL_ID.newException();
            return;
        }

        // 记录翻转前为"未冻结"的用户 —— 翻转后会变冻结，需要踢下线
        Set<Long> willBeFrozen = this.mapper.selectList(new LambdaQueryWrapper<UserEntity>()
                        .select(UserEntity::getId)
                        .in(UserEntity::getId, ids)
                        .eq(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN))
                .stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());

        this.mapper.change(ids);

        if (!willBeFrozen.isEmpty()) {
            eventPublisher.publishEvent(new UserOfflineEvent(this, willBeFrozen, OfflineReason.USER_FROZEN));
        }
    }

    /**
     * 获取用户详情（含角色 id 列表）
     */
    @Override
    public UserDetailVo getUser(Long id) {
        UserEntity user = this.mapper.selectById(id);
        if (user == null) {
            SysCodes.CANNOT_FIND_USER.newException();
            return null;
        }
        UserDetailVo vo = this.convert.entityToDetailVo(user);
        vo.setRoleIds(this.roleUserMapper.selectRoleIds(id));
        return vo;
    }

    /**
     * 新增 / 编辑用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void saveUser(UserAoeBo user) {
        // 账号合法性
        this.validateUsername(user.getAccount());

        Long userId = user.getId();
        boolean isInsert = (userId == null || userId == 0L);

        if (isInsert) {
            // 新增：生成 id + 校验授权数量 + hash 密码
            userId = IdWorker.getId();
            user.setId(userId);

            // 多租户模式下校验授权用户数（U-07 修复：之前条件反向）
            if (!CommonConstant.MODE_SINGLETON.equals(evaConfig.getMode())) {
                Long tid = ThreadUserHelper.getTenantId();
                Integer leftCt = this.mapper.availableCounts(tid);
                if (leftCt == null || leftCt < 1) {
                    SysCodes.USER_ACCOUNT_LIMIT.newException();
                }
            }

            if (StrUtils.isBlank(user.getPassword())) {
                SysCodes.BAD_ORG_PASSWORD.newException();
            }
            user.setPassword(BCryptUtils.hashpw(user.getPassword()));
        } else {
            // 编辑：处理头像替换；密码为空则不更新（MyBatis-Plus null 不更新）
            UserEntity oldUser = this.mapper.selectById(userId);
            if (oldUser == null) {
                SysCodes.CANNOT_FIND_USER.newException();
                return;
            }
            String oldAvatar = oldUser.getAvatar();
            if (StrUtils.isNotBlank(oldAvatar) && !oldAvatar.equals(user.getAvatar())) {
                fileProvider.delFromStorage(oldAvatar);
            }
            if (StrUtils.isNotBlank(user.getPassword())) {
                user.setPassword(BCryptUtils.hashpw(user.getPassword()));
            } else {
                user.setPassword(null);
            }
        }

        // 保存新头像缩略图
        if (StrUtils.isNotBlank(user.getAvatar())) {
            try {
                fileProvider.storageWithThumbnail(0.3f, user.getAvatar());
            } catch (IOException e) {
                throw new BizException(CommonCodes.SERVER_ERROR);
            }
        }

        UserEntity entity = this.convert.boToEntity(user);
        if (isInsert) {
            this.mapper.insert(entity);
        } else {
            this.mapper.updateById(entity);
        }

        // 保存角色关系
        if (CollUtils.isNotEmpty(user.getRoleIds())) {
            UserGrantBo grantBo = new UserGrantBo();
            grantBo.setUserId(userId);
            grantBo.setRoleIds(user.getRoleIds());
            this.userRoleRefSerivce.saveRoles(grantBo);
        }

        // 保存岗位关系（U-01 修复：原代码条件用了 roleIds，导致只传岗位不传角色时不保存岗位）
        if (CollUtils.isNotEmpty(user.getPostId())) {
            UserPostBo postBo = new UserPostBo();
            postBo.setUserId(userId);
            postBo.setPostIds(user.getPostId());
            this.userPostRefSerivce.savePosts(postBo);
        }
    }

    /**
     * TODO 待实现：返回用户菜单 + 资源
     */
    public List<UserResourceVo> fetchModuleByUid(Long uid) {
        return null;
    }

    /**
     * 校验账号 / 编码唯一性
     * 注意：@TableLogic 自动添加 deleted=0 条件，已逻辑删除的用户自动排除
     */
    @Override
    public boolean checkUnique(UserCheckBo user) {
        if (user == null || (StrUtils.isBlank(user.getAccount()) && StrUtils.isBlank(user.getCode()))) {
            return false;
        }
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.nested(w -> w.eq(StrUtils.isNotBlank(user.getAccount()), UserEntity::getAccount, user.getAccount())
                .or()
                .eq(StrUtils.isNotBlank(user.getCode()), UserEntity::getCode, user.getCode()));

        if (user.getId() != null && user.getId() != 0L) {
            wrapper.ne(UserEntity::getId, user.getId());
        }
        return this.mapper.selectCount(wrapper) > 0;
    }


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void createTenantAdmin(UserEntity user) {
        this.validateUsername(user.getAccount());

        if (StrUtils.isBlank(user.getPassword())) {
            SysCodes.BAD_ORG_PASSWORD.newException();
        }
        user.setPassword(BCryptUtils.hashpw(user.getPassword()));
        this.mapper.insert(user);
    }
}
