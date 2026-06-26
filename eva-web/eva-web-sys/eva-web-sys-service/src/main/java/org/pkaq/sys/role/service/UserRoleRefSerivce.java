package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.user.bo.UserGrantBo;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户角色关系服务
 * <p>
 * saveRoles 采用 diff 模式：与 UserPostRefSerivce.savePosts 对称，
 * 避免"先全删再全插"导致的事务窗口期、审计字段污染、CDC 事件雪崩。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserRoleRefSerivce implements IUserRoleRefSerivce {
    private final RoleUserMapper roleUserMapper;
    private final UserMapper userMapper;

    /**
     * 保存用户角色关系（diff 模式）
     * - retained：两边都有的不动
     * - toDelete：existing − incoming，DELETE IN(...)
     * - toInsert：incoming − existing，逐条 INSERT
     * - 仅在关系真正发生变化时才自增权限版本号，避免无谓刷 permVer
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void saveRoles(UserGrantBo bo) {
        if (bo == null || bo.getUserId() == null) {
            return;
        }
        Long userId = bo.getUserId();

        // 当前已授权的 roleId 集合
        Set<Long> existing = this.roleUserMapper.selectList(
                        new LambdaQueryWrapper<RoleUserEntity>()
                                .select(RoleUserEntity::getRoleId)
                                .eq(RoleUserEntity::getUserId, userId))
                .stream()
                .map(RoleUserEntity::getRoleId)
                .collect(Collectors.toSet());

        Set<Long> incoming = CollUtils.isEmpty(bo.getRoleIds())
                ? new HashSet<>()
                : new HashSet<>(bo.getRoleIds());

        // diff
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

        // 关系真正变化才刷权限版本号
        if (!toInsert.isEmpty() || !toDelete.isEmpty()) {
            this.userMapper.incrementPermVer(userId);
        }
    }
}
