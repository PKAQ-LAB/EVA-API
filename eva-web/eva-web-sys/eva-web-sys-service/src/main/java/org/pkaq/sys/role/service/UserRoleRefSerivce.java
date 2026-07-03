package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.user.bo.UserGrantBo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户角色关系服务
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserRoleRefSerivce implements IUserRoleRefSerivce {
    /** 系统内置管理员账号编码。 */
    private static final String SYSTEM_ADMIN_CODE = "9999";

    private final RoleUserMapper roleUserMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    /**
     * 保存用户角色关系。
     *
     * @param bo 用户授权参数
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void saveRoles(UserGrantBo bo) {
        if (bo == null || bo.getUserId() == null) {
            SysCodes.CANNOT_FIND_USER.newException();
            return;
        }

        Long userId = bo.getUserId();
        this.ensureUserExists(userId);

        Set<Long> incoming = this.sanitizeIds(bo.getRoleIds());
        this.ensureRolesUsable(incoming);

        Set<Long> existing = this.roleUserMapper.selectList(
                        new LambdaQueryWrapper<RoleUserEntity>()
                                .select(RoleUserEntity::getRoleId)
                                .eq(RoleUserEntity::getUserId, userId))
                .stream()
                .map(RoleUserEntity::getRoleId)
                .collect(Collectors.toSet());

        Set<Long> toInsert = new HashSet<>(incoming);
        toInsert.removeAll(existing);
        Set<Long> toDelete = new HashSet<>(existing);
        toDelete.removeAll(incoming);

        if (!toDelete.isEmpty()) {
            this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>()
                    .eq(RoleUserEntity::getUserId, userId)
                    .in(RoleUserEntity::getRoleId, toDelete));
        }
        if (!toInsert.isEmpty()) {
            for (Long roleId : toInsert) {
                RoleUserEntity ref = new RoleUserEntity();
                ref.setRoleId(roleId);
                ref.setUserId(userId);
                this.roleUserMapper.insert(ref);
            }
        }

        if (!toInsert.isEmpty() || !toDelete.isEmpty()) {
            this.userMapper.incrementPermVer(userId);
        }
    }

    private void ensureUserExists(Long userId) {
        UserEntity user = this.userMapper.selectById(userId);
        if (user == null) {
            SysCodes.CANNOT_FIND_USER.newException();
            return;
        }
        if (user.getFrozen() == FrozenEnumm.READ_ONLY || SYSTEM_ADMIN_CODE.equals(user.getCode())) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private void ensureRolesUsable(Set<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return;
        }

        List<RoleEntity> roles = this.roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                .select(RoleEntity::getId)
                .in(RoleEntity::getId, roleIds)
                .ne(RoleEntity::getFrozen, FrozenEnumm.FROZEN));
        if (roles.size() != roleIds.size()) {
            SysCodes.RECORD_NOT_FOUND.newException();
        }
    }

    private Set<Long> sanitizeIds(List<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return Collections.emptySet();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0L)
                .collect(Collectors.toSet());
    }
}
