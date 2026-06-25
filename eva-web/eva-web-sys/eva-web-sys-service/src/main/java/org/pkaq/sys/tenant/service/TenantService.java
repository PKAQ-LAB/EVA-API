package org.pkaq.sys.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.util.BCryptUtils;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.bo.TenantCheckBo;
import org.pkaq.sys.tenant.bo.TenantQueryBo;
import org.pkaq.sys.tenant.convert.TenantConvert;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.vo.TenantDetailVo;
import org.pkaq.sys.tenant.vo.TenantListVo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租户管理 Service
 * <p>
 * 冻结规则：
 * - 翻转 tenant.frozen（FROZEN ↔ UN_FROZEN），READ_ONLY 跳过
 * - 翻转为 FROZEN：同步把该租户下所有 UN_FROZEN 用户置为 FROZEN 并踢下线
 * - 翻转为 UN_FROZEN：同步把该租户下所有 FROZEN 用户置为 UN_FROZEN
 * （注：当前不区分"用户级冻结"与"租户级冻结"，整租户解冻会解冻所有用户）
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class TenantService extends StdService<org.pkaq.sys.tenant.mapper.TenantMapper, TenantEntity> implements ITenantService {

    private final UserMapper userMapper;
    private final UserService userService;
    private final TenantConvert convert;
    private final CacheTokenUtil cacheTokenUtil;

    /**
     * 批量切换冻结状态
     */
    @Override
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

            // 翻转租户本体
            this.mapper.update(null, new LambdaUpdateWrapper<TenantEntity>()
                    .eq(TenantEntity::getId, id)
                    .set(TenantEntity::getFrozen, target));

            // 同步联动租户下的用户
            cascadeUserFrozen(id, target);
        }
    }

    /**
     * 批量删除租户：同步软删该租户下所有用户并踢下线
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(Set<Long> ids) {
        // T-T01 修复：原代码 if(isNotEmpty) newException()，判断完全反向
        if (CollUtils.isEmpty(ids)) {
            return;
        }

        // 收集即将被删用户，用于踢下线
        Set<Long> affectedUsers = this.userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                        .select(UserEntity::getId)
                        .in(UserEntity::getTenantId, ids))
                .stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());

        // 删除租户
        this.mapper.deleteByIds(ids);
        // 删除租户下所有用户（StdEntity @TableLogic → 逻辑删）
        this.userMapper.delete(Wrappers.<UserEntity>lambdaUpdate().in(UserEntity::getTenantId, ids));
        // 踢下线
        this.cacheTokenUtil.removeTokens(affectedUsers);
    }

    /**
     * 新增 / 编辑租户。新增时同步创建管理员账号
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(TenantAoeBo editBo) {
        if (editBo == null) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }

        boolean isNew = (editBo.getId() == null || editBo.getId() == 0L);
        long tid = isNew ? IdWorker.getId() : editBo.getId();
        editBo.setId(tid);

        TenantEntity entity = this.convert.boToEntity(editBo);

        if (isNew) {
            // T-T03 修复：admin user.id = tenant.adminId（直接复用同一 id）
            long adminId = IdWorker.getId();
            entity.setAdminId(adminId);
            this.mapper.insert(entity);

            UserEntity admin = new UserEntity();
            admin.setId(adminId);
            admin.setFrozen(FrozenEnumm.READ_ONLY);
            admin.setAccount(editBo.getAdminAccount());
            admin.setPassword(editBo.getAdminPass());
            admin.setName(editBo.getAdminAccount());
            admin.setCode(editBo.getAdminAccount());
            admin.setTenantId(tid);
            this.userService.createTenantAdmin(admin);
        } else {
            // T-T08：租户号、管理员 ID 不可修改 —— 显式 set null（依赖 MP "null 不更新" 策略 +
            // 项目 MybatisMetaObjectHandler.strictFillStrategy 中 null 检查跳过填充）
            entity.setCode(null);
            entity.setAdminId(null);

            // 取出原授权用户数用于变更对比
            TenantEntity orig = this.mapper.selectOne(new LambdaQueryWrapper<TenantEntity>()
                    .select(TenantEntity::getAuthUserCount)
                    .eq(TenantEntity::getId, tid));
            int origAuthCount = orig == null ? 0 : orig.getAuthUserCount();

            this.mapper.updateById(entity);

            // 授权用户数变更后续处理（如锁定超额、解锁多余）暂未实现，TODO 与套餐功能一起补
            if (editBo.getAuthUserCount() != origAuthCount) {
                // no-op，留作扩展点
            }
        }
    }

    /**
     * 详情查询
     */
    @Override
    public TenantDetailVo get(Long id) {
        TenantEntity entity = this.mapper.selectById(id);
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(id);
            return null;
        }
        TenantDetailVo vo = this.convert.entityToVo(entity);
        // 回填管理员账号
        Optional.ofNullable(this.userMapper.selectById(entity.getAdminId()))
                .ifPresent(admin -> vo.setAdminAccount(admin.getAccount()));
        return vo;
    }

    /**
     * 分页查询
     */
    @Override
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
        return this.mapper.selectPage(pagination, wrapper).map(e -> {
            TenantListVo vo = new TenantListVo();
            vo.setId(e.getId());
            vo.setName(e.getName());
            vo.setCode(e.getCode());
            vo.setType(e.getType());
            vo.setFullName(e.getFullName());
            vo.setCardType(e.getCardType());
            vo.setCardNo(e.getCardNo());
            vo.setContactName(e.getContactName());
            vo.setContactTel(e.getContactTel());
            vo.setAuthUserCount(e.getAuthUserCount());
            vo.setExpirationDate(e.getExpirationDate());
            vo.setRemark(e.getRemark());
            return vo;
        });
    }

    /**
     * 校验 code / name 唯一性
     */
    @Override
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
     * 联动租户下所有用户的 frozen 状态（跳过 READ_ONLY）
     * 冻结时同步踢下线
     */
    private void cascadeUserFrozen(Long tenantId, FrozenEnumm target) {
        if (target == FrozenEnumm.FROZEN) {
            // 取出将被冻结的用户用于踢下线
            List<UserEntity> toFrozen = this.userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                    .select(UserEntity::getId)
                    .eq(UserEntity::getTenantId, tenantId)
                    .eq(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN));

            this.userMapper.update(null, new LambdaUpdateWrapper<UserEntity>()
                    .eq(UserEntity::getTenantId, tenantId)
                    .eq(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN)
                    .set(UserEntity::getFrozen, FrozenEnumm.FROZEN));

            Set<Long> uids = toFrozen.stream().map(UserEntity::getId).collect(Collectors.toSet());
            this.cacheTokenUtil.removeTokens(uids);
        } else if (target == FrozenEnumm.UN_FROZEN) {
            this.userMapper.update(null, new LambdaUpdateWrapper<UserEntity>()
                    .eq(UserEntity::getTenantId, tenantId)
                    .eq(UserEntity::getFrozen, FrozenEnumm.FROZEN)
                    .set(UserEntity::getFrozen, FrozenEnumm.UN_FROZEN));
        }
    }
}
