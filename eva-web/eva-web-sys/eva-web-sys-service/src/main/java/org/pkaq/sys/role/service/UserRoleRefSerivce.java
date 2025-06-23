package org.pkaq.sys.role.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
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
public class UserRoleRefSerivce {
    private final RoleUserMapper roleUserMapper;

    @BizLog(operateType = BizLogCodes.EDIT, description = "更新用户角色关系[{0}]", args = {"param:0.userId"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void saveRoles(UserGrantBo bo) {
        // 保存权限
        if (CollUtil.isNotEmpty(bo.getRoleIds())) {
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
