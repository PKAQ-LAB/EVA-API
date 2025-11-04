package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.user.bo.UserGrantBo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserRoleRefSerivce implements IUserRoleRefSerivce {
    private final RoleUserMapper roleUserMapper;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void saveRoles(UserGrantBo bo) {
        // 保存权限
        if (CollUtils.isNotEmpty(bo.getRoleIds())) {
            // 先删除该用户原有的权限
            LambdaQueryWrapper<RoleUserEntity> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(RoleUserEntity::getUserId, bo.getUserId());

            this.roleUserMapper.delete(deleteWrapper);
            // 再插入更新后的权限
            bo.getRoleIds().forEach(item -> {
                RoleUserEntity roleUserEntity = new RoleUserEntity();
                roleUserEntity.setRoleId(item);
                roleUserEntity.setUserId(bo.getUserId());
                roleUserMapper.insert(roleUserEntity);
            });
        }
    }
}
