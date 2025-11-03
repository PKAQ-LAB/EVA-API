package org.pkaq.sys.tenant.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.bo.TenantCheckBo;
import org.pkaq.sys.tenant.convert.TenantConvert;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.mapper.TenantMapper;
import org.pkaq.sys.tenant.vo.TenantDetailVo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * 租户管理Service
 *
 * @author PKAQ
 */
@Service
@Schema(description = "租户管理")
@AllArgsConstructor
public class TenantService extends StdService<TenantMapper, TenantEntity> implements ITenantService {
    private final UserMapper userMapper;
    private final UserService userService;
    private final TenantConvert convert;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void switchFrozen(SingleArray<Long> ids, Integer frozen) {
        if (null == ids || null == ids.getParam() || null == frozen) {
            CommonCodes.PARAM_ERROR.newException();
        }

        UpdateWrapper<TenantEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .in("id", ids)
                .setSql("frozen = abs(frozen - 1)")
                .ne("frozen", -1);

        this.mapper.update(updateWrapper);

        // 同时冻结用户
        if (FrozenEnumm.FROZEN.getCode() == frozen) {
            this.mapper.frozenUser(ids);
        }

        // 同时解锁用户
        if (FrozenEnumm.UN_FROZEN.getCode() == frozen) {
            this.mapper.unfronzenUser(ids);
        }
    }

    /**
     * 根据ID批量删除
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void delete(Set<Long> ids) {
        if (CollUtil.isNotEmpty(ids)) {
            CommonCodes.PARAM_ERROR.newException();
        }
        // 删除租户
        this.mapper.deleteByIds(ids);
        // 删除租户所有用户
        LambdaUpdateWrapper<UserEntity> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.in(UserEntity::getTenantId, ids);
        userMapper.delete(updateWrapper);
    }

    /**
     * 新增/编辑一条租户信息
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void edit(TenantAoeBo editBo) {
        if (null == editBo) {
            CommonCodes.PARAM_ERROR.newException();
        }

        boolean isNew = (null == editBo.getId() || editBo.getId() == 0);
        long tid = isNew ? IdWorker.getId() : editBo.getId();
        editBo.setId(tid);

        TenantEntity entity = this.convert.boToEntity(editBo);
        if (isNew) {
            var adminId = IdWorker.getId();
            entity.setAdminId(adminId);
            this.mapper.insert(entity);

            // 初始化管理员用户
            UserEntity user = new UserEntity();
            user.setFrozen(FrozenEnumm.READ_ONLY);
            user.setAccount(editBo.getAdminAccount());
            user.setPassword(BCrypt.hashpw(editBo.getAdminPass()));
            user.setTenantId(editBo.getId());
            this.userService.createTenantAdmin(user);
        } else {
            // 租户号、管理员账号 不可修改
            entity.setCode(null);
            entity.setAdminId(null);

            LambdaQueryWrapper<TenantEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(TenantEntity::getAuthUserCount);
            wrapper.eq(TenantEntity::getId, editBo.getId());
            var orAuthCount = this.mapper.selectOne(wrapper).getAuthUserCount();

            this.mapper.updateById(entity);
            if (editBo.getAuthUserCount() < orAuthCount) {
                // 授权用户减少 锁定超出数量的用户
                this.mapper.reGrantUser(tid, editBo.getAuthUserCount(), -1);
            }

            if (editBo.getAuthUserCount() > orAuthCount) {
                // 授权用户增加 解锁超出的锁定用户
                this.mapper.reGrantUser(tid, editBo.getAuthUserCount(), 0);
            }
        }
    }

    /**
     * 根据ID获取一条租户信息
     *
     * @param id 租户ID
     * @return 租户信息
     */
    @Override
    public TenantDetailVo get(String id) {
        var entity = this.mapper.selectById(id);
        var vo = convert.entityToVo(entity);

        // 查询管理员账号
        Optional.ofNullable(userMapper.selectById(entity.getAdminId())).ifPresent(admin -> vo.setAdminAccount(admin.getAccount()));
        return vo;
    }

    /**
     * 校验code/名称是否唯一
     */
    @Override
    public boolean checkUnique(TenantCheckBo checkBo) {
        LambdaQueryWrapper<TenantEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.nested(w ->
                w.eq(TenantEntity::getName, checkBo.getName())
                        .or()
                        .eq(TenantEntity::getCode, checkBo.getCode()));

        if (null != checkBo.getId() && checkBo.getId() != 0) {
            wrapper.ne(TenantEntity::getId, checkBo.getId());
        }
        return this.mapper.selectCount(wrapper) > 0;
    }
}
