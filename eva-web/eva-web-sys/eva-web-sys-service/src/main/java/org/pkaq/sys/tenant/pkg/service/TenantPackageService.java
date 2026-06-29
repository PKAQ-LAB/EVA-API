package org.pkaq.sys.tenant.pkg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.util.CollUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.tenant.pkg.bo.TenantPackageAoeBo;
import org.pkaq.sys.tenant.pkg.bo.TenantPackageQueryBo;
import org.pkaq.sys.tenant.pkg.entity.TenantPackageEntity;
import org.pkaq.sys.tenant.pkg.entity.TenantPackageResourceEntity;
import org.pkaq.sys.tenant.pkg.mapper.TenantPackageMapper;
import org.pkaq.sys.tenant.pkg.mapper.TenantPackageResourceMapper;
import org.pkaq.sys.tenant.pkg.vo.TenantPackageVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租户套餐服务。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class TenantPackageService {
    private final TenantPackageMapper tenantPackageMapper;
    private final TenantPackageResourceMapper tenantPackageResourceMapper;
    private final RoleResourceMapper roleResourceMapper;

    /**
     * 分页查询租户套餐。
     *
     * @param queryBo 查询参数
     * @return 套餐分页数据
     */
    public IPage<TenantPackageVo> list(TenantPackageQueryBo queryBo) {
        TenantPackageQueryBo safeQueryBo = queryBo == null ? new TenantPackageQueryBo() : queryBo;
        Page<TenantPackageEntity> page = new Page<>(safeQueryBo.getPageNo(), safeQueryBo.getPageSize());
        return this.tenantPackageMapper.selectPage(page, new LambdaQueryWrapper<TenantPackageEntity>()
                        .like(StrUtils.isNotBlank(safeQueryBo.getCode()), TenantPackageEntity::getCode, safeQueryBo.getCode())
                        .like(StrUtils.isNotBlank(safeQueryBo.getName()), TenantPackageEntity::getName, safeQueryBo.getName())
                        .orderByDesc(TenantPackageEntity::getUtcModify))
                .convert(this::toVo);
    }

    /**
     * 查询租户套餐详情。
     *
     * @param id 套餐ID
     * @return 套餐详情
     */
    public TenantPackageVo get(Long id) {
        TenantPackageEntity entity = this.tenantPackageMapper.selectById(id);
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(id);
            return null;
        }
        TenantPackageVo vo = toVo(entity);
        vo.setResourceIds(this.tenantPackageResourceMapper.selectAuthorizedResourceIds(id));
        return vo;
    }

    /**
     * 校验套餐编码或名称是否重复。
     *
     * @param bo 套餐参数
     * @return true 表示已存在
     */
    public boolean checkUnique(TenantPackageAoeBo bo) {
        if (bo == null || StrUtils.isAllBlank(bo.getCode(), bo.getName())) {
            return false;
        }
        return checkUnique(bo.getId(), bo.getCode(), bo.getName());
    }

    /**
     * 新增或编辑租户套餐。
     *
     * @param bo 套餐参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void edit(TenantPackageAoeBo bo) {
        if (bo == null || StrUtils.isBlank(bo.getCode()) || StrUtils.isBlank(bo.getName())) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        if (checkUnique(bo.getId(), bo.getCode(), bo.getName())) {
            CommonCodes.DUPLICATE_CODE_ERROR.newException();
            return;
        }
        Set<Long> resourceIds = sanitizeIds(bo.getResourceIds());
        ensureResourcesValid(resourceIds);

        TenantPackageEntity entity = new TenantPackageEntity();
        entity.setId(bo.getId());
        entity.setRevision(bo.getRevision());
        entity.setCode(bo.getCode().trim());
        entity.setName(bo.getName().trim());
        entity.setAuthUserCount(bo.getAuthUserCount());
        entity.setValidDays(bo.getValidDays());
        entity.setFrozen(resolveFrozen(bo.getFrozen()));
        entity.setSort(bo.getSort());
        entity.setRemark(bo.getRemark());
        this.tenantPackageMapper.insertOrUpdate(entity);

        syncResources(entity.getId(), resourceIds);
    }

    /**
     * 删除租户套餐。
     *
     * @param ids 套餐ID集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return;
        }
        this.tenantPackageResourceMapper.delete(new LambdaQueryWrapper<TenantPackageResourceEntity>()
                .in(TenantPackageResourceEntity::getPackageId, ids));
        this.tenantPackageMapper.deleteByIds(ids);
    }

    /**
     * 切换套餐冻结状态。
     *
     * @param ids 套餐ID集合
     */
    public void switchFrozen(SingleArray<Long> ids) {
        if (ids == null || CollUtils.isEmpty(ids.getParam())) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        for (Long id : ids.getParam()) {
            TenantPackageEntity entity = this.tenantPackageMapper.selectById(id);
            if (entity == null || entity.getFrozen() == FrozenEnumm.READ_ONLY) {
                continue;
            }
            FrozenEnumm target = entity.getFrozen() == FrozenEnumm.FROZEN
                    ? FrozenEnumm.UN_FROZEN
                    : FrozenEnumm.FROZEN;
            this.tenantPackageMapper.update(null, new LambdaUpdateWrapper<TenantPackageEntity>()
                    .eq(TenantPackageEntity::getId, id)
                    .set(TenantPackageEntity::getFrozen, target));
        }
    }

    private boolean checkUnique(Long id, String code, String name) {
        String safeCode = StrUtils.isBlank(code) ? null : code.trim();
        String safeName = StrUtils.isBlank(name) ? null : name.trim();
        return this.tenantPackageMapper.selectCount(new LambdaQueryWrapper<TenantPackageEntity>()
                .and(w -> {
                    if (StrUtils.isNotBlank(safeCode)) {
                        w.eq(TenantPackageEntity::getCode, safeCode);
                    }
                    if (StrUtils.isNotBlank(safeCode) && StrUtils.isNotBlank(safeName)) {
                        w.or();
                    }
                    if (StrUtils.isNotBlank(safeName)) {
                        w.eq(TenantPackageEntity::getName, safeName);
                    }
                })
                .ne(id != null && id != 0L, TenantPackageEntity::getId, id)) > 0;
    }

    private void syncResources(Long packageId, Set<Long> resourceIds) {
        this.tenantPackageResourceMapper.delete(new LambdaQueryWrapper<TenantPackageResourceEntity>()
                .eq(TenantPackageResourceEntity::getPackageId, packageId));
        for (Long resourceId : resourceIds) {
            TenantPackageResourceEntity entity = new TenantPackageResourceEntity();
            entity.setPackageId(packageId);
            entity.setResourceId(resourceId);
            this.tenantPackageResourceMapper.insert(entity);
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

    private Set<Long> sanitizeIds(java.util.List<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return new HashSet<>();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0L)
                .collect(Collectors.toSet());
    }

    private FrozenEnumm resolveFrozen(Integer code) {
        if (code == null) {
            return FrozenEnumm.UN_FROZEN;
        }
        for (FrozenEnumm item : FrozenEnumm.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return FrozenEnumm.UN_FROZEN;
    }

    private TenantPackageVo toVo(TenantPackageEntity entity) {
        TenantPackageVo vo = new TenantPackageVo();
        vo.setId(entity.getId());
        vo.setRevision(entity.getRevision() == null ? 0 : entity.getRevision());
        vo.setFrozen(entity.getFrozen());
        vo.setSort(entity.getSort());
        vo.setTenantId(entity.getTenantId());
        vo.setCreateId(entity.getCreateId());
        vo.setCreateBy(entity.getCreateBy());
        vo.setUtcCreate(entity.getUtcCreate());
        vo.setModifyId(entity.getModifyId());
        vo.setModifyBy(entity.getModifyBy());
        vo.setUtcModify(entity.getUtcModify());
        vo.setRemark(entity.getRemark());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setAuthUserCount(entity.getAuthUserCount());
        vo.setValidDays(entity.getValidDays());
        return vo;
    }
}
