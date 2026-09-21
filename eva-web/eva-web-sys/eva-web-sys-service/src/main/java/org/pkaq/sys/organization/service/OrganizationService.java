package org.pkaq.sys.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.tenant.TenantSchema;
import org.pkaq.core.mybatis.util.TreeHelper;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.bo.OrganizationFrozenBo;
import org.pkaq.sys.organization.bo.OrganizationQueryBo;
import org.pkaq.sys.organization.bo.OrganizationSortBo;
import org.pkaq.sys.organization.convert.OrganizationConvert;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.organization.vo.OrganizationDetailVo;
import org.pkaq.sys.organization.vo.OrganizationListVo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组织管理服务。
 * <p>
 * 维护组织树的增删改查、冻结、同级排序和 path/isleaf 等树形字段。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class OrganizationService extends StdService<OrganizationMapper, OrganizationEntity> {
    /** 根节点 pid，与数据库默认值保持一致。 */
    private static final long ROOT_PID = 0L;

    private final OrganizationConvert organizationConvert;
    private final UserMapper userMapper;

    /**
     * 校验同级组织 code 或名称是否重复。
     *
     * @return true 表示已存在
     */
    @TenantSchema
    @Transactional(readOnly = true)
    public boolean checkUnique(OrganizationAoeBo bo) {
        if (bo == null) {
            return false;
        }
        long pid = bo.getPid() == null ? ROOT_PID : bo.getPid();
        boolean hasCode = bo.getCode() != null && !bo.getCode().isEmpty();
        boolean hasName = bo.getName() != null && !bo.getName().isEmpty();
        if (!hasCode && !hasName) {
            return false;
        }
        LambdaQueryWrapper<OrganizationEntity> wrapper = new LambdaQueryWrapper<OrganizationEntity>()
                .eq(OrganizationEntity::getPid, pid)
                .and(w -> {
                    if (hasCode) {
                        w.eq(OrganizationEntity::getCode, bo.getCode());
                    }
                    if (hasCode && hasName) {
                        w.or();
                    }
                    if (hasName) {
                        w.eq(OrganizationEntity::getName, bo.getName());
                    }
                });

        if (bo.getId() != null && bo.getId() != 0L) {
            wrapper.ne(OrganizationEntity::getId, bo.getId());
        }
        return this.mapper.selectCount(wrapper) > 0;
    }

    /**
     * 查询。
     */
    @TenantSchema
    @Transactional(readOnly = true)
    public Collection<OrganizationListVo> list(OrganizationQueryBo queryBo) {
        List<OrganizationListVo> orgList = this.organizationConvert.entityToListVo(
                this.mapper.selectOrgMapList(queryBo));
        return TreeHelper.buildTree(orgList);
    }

    /**
     * 新增或编辑组织。
     */
    @TenantSchema
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(OrganizationAoeBo bo) {
        OrganizationEntity org = this.organizationConvert.aoeBoToEntity(bo);
        if (org.getPid() == null) {
            org.setPid(ROOT_PID);
        }

        Long orgId = org.getId();
        long pid = org.getPid();
        boolean isNew = orgId == null || orgId == 0L;
        boolean isRoot = pid == ROOT_PID;

        if (isNew) {
            orgId = IdWorker.getId();
            org.setId(orgId);
            org.setIsleaf(true);
            org.setPath(buildPath(pid, orgId, isRoot));
            org.setSort(nextSort(pid));
            this.mapper.insert(org);

            if (!isRoot) {
                setParentLeaf(pid, false);
            }
            return;
        }

        OrganizationEntity origin = this.mapper.selectById(orgId);
        if (origin == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(orgId);
            return;
        }
        ensureOrganizationEditable(origin);

        if (!Objects.equals(origin.getPid(), pid)) {
            assertValidParent(origin, pid);
            handleParentChange(org, origin, isRoot);
            return;
        }

        org.setPath(origin.getPath());
        org.setSort(origin.getSort());
        this.mapper.updateById(org);
    }

    /**
     * 查询组织详情。
     */
    @TenantSchema
    @Transactional(readOnly = true)
    public OrganizationDetailVo get(Long id) {
        OrganizationEntity entity = this.mapper.selectById(id);
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(id);
            return null;
        }
        OrganizationDetailVo vo = this.organizationConvert.entityToDetailVo(entity);
        if (entity.getPid() != null && entity.getPid() != ROOT_PID) {
            OrganizationEntity parent = this.mapper.selectById(entity.getPid());
            if (parent != null) {
                vo.setParentName(parent.getName());
            }
        }
        return vo;
    }

    /**
     * 删除组织。
     */
    @TenantSchema
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return;
        }

        ensureOrganizationsEditable(ids);

        List<OrganizationEntity> leafList = this.mapper.selectList(new LambdaQueryWrapper<OrganizationEntity>()
                .in(OrganizationEntity::getPid, ids));
        if (CollUtils.isNotEmpty(leafList)) {
            String nameStr = leafList.stream()
                    .map(OrganizationEntity::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            CommonCodes.CHILD_EXIST.newException(nameStr);
            return;
        }

        if (this.userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .in(UserEntity::getDeptId, ids)) > 0) {
            SysCodes.RESOURCE_USED.newException();
            return;
        }

        Set<Long> originPids = this.mapper.selectList(new LambdaQueryWrapper<OrganizationEntity>()
                        .select(OrganizationEntity::getPid)
                        .in(OrganizationEntity::getId, ids))
                .stream()
                .map(OrganizationEntity::getPid)
                .filter(pid -> pid != null && pid != ROOT_PID)
                .collect(Collectors.toSet());

        this.mapper.delete(new LambdaQueryWrapper<OrganizationEntity>().in(OrganizationEntity::getId, ids));
        refreshParentLeaf(originPids);
    }

    /**
     * 同级拖拽排序。
     */
    @TenantSchema
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void sort(OrganizationSortBo bo) {
        if (bo == null || bo.getId() == null) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        OrganizationEntity self = this.mapper.selectById(bo.getId());
        if (self == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(bo.getId());
            return;
        }
        ensureOrganizationEditable(self);
        if (bo.getOldSort() == bo.getNewSort()) {
            return;
        }
        this.mapper.updateSort(bo.getId(), self.getPid(), bo.getOldSort(), bo.getNewSort());
    }

    /**
     * 切换冻结状态，并级联处理子节点。
     */
    @TenantSchema
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void switchFrozen(OrganizationFrozenBo bo) {
        if (bo == null || CollUtils.isEmpty(bo.getParam()) || bo.getFrozen() == null) {
            return;
        }
        FrozenEnumm target;
        if (FrozenEnumm.FROZEN.getCode().equals(bo.getFrozen())) {
            target = FrozenEnumm.FROZEN;
        } else if (FrozenEnumm.UN_FROZEN.getCode().equals(bo.getFrozen())) {
            target = FrozenEnumm.UN_FROZEN;
        } else {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        for (Long id : bo.getParam()) {
            OrganizationEntity self = this.mapper.selectById(id);
            if (self == null || self.getFrozen() == FrozenEnumm.READ_ONLY) {
                continue;
            }
            if (self.getFrozen() == target) {
                continue;
            }

            if (target == FrozenEnumm.UN_FROZEN && self.getPid() != null && self.getPid() != ROOT_PID) {
                OrganizationEntity parent = this.mapper.selectById(self.getPid());
                if (parent != null && parent.getFrozen() == FrozenEnumm.FROZEN) {
                    continue;
                }
            }
            this.mapper.cascadeFrozen(id, self.getPath(), target.getCode());
        }
    }

    /**
     * 构建组织 path。
     */
    private String buildPath(long pid, long id, boolean isRoot) {
        if (isRoot) {
            return "/" + id;
        }
        OrganizationEntity parent = this.mapper.selectById(pid);
        if (parent == null || parent.getPath() == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(pid);
            return null;
        }
        return parent.getPath() + "/" + id;
    }

    /** 禁止将节点移动到自身或其任一后代节点下。 */
    private void assertValidParent(OrganizationEntity origin, long newPid) {
        if (newPid == origin.getId()) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        if (newPid == ROOT_PID) {
            return;
        }
        OrganizationEntity parent = this.mapper.selectById(newPid);
        if (parent == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(newPid);
            return;
        }
        String originPath = origin.getPath();
        String parentPath = parent.getPath();
        if (originPath != null && parentPath != null
                && (parentPath.equals(originPath) || parentPath.startsWith(originPath + "/"))) {
            CommonCodes.PARAM_ERROR.newException();
        }
    }

    /**
     * 获取同级下一个排序值。
     */
    private double nextSort(long pid) {
        Integer maxSort = this.mapper.listOrder(pid);
        return (maxSort == null ? 0 : maxSort) + 1;
    }

    /**
     * 设置父节点叶子状态。
     */
    private void setParentLeaf(long pid, boolean isleaf) {
        this.mapper.update(null, new LambdaUpdateWrapper<OrganizationEntity>()
                .eq(OrganizationEntity::getId, pid)
                .set(OrganizationEntity::getIsleaf, isleaf));
    }

    /**
     * 刷新父节点叶子状态。
     */
    private void refreshParentLeaf(Set<Long> parentIds) {
        if (CollUtils.isEmpty(parentIds)) {
            return;
        }
        for (Long pid : parentIds) {
            Long childCount = this.mapper.selectCount(new LambdaQueryWrapper<OrganizationEntity>()
                    .eq(OrganizationEntity::getPid, pid));
            if (childCount == null || childCount == 0L) {
                setParentLeaf(pid, true);
            }
        }
    }

    private void ensureOrganizationsEditable(Set<Long> ids) {
        long readOnlyCount = this.mapper.selectCount(new LambdaQueryWrapper<OrganizationEntity>()
                .in(OrganizationEntity::getId, ids)
                .eq(OrganizationEntity::getFrozen, FrozenEnumm.READ_ONLY));
        if (readOnlyCount > 0) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private void ensureOrganizationEditable(OrganizationEntity entity) {
        if (entity.getFrozen() == FrozenEnumm.READ_ONLY) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    /** 更换父节点时刷新当前节点、子孙节点路径以及新旧父节点叶子状态。 */
    private void handleParentChange(OrganizationEntity org, OrganizationEntity origin, boolean isRoot) {
        long orgId = org.getId();
        long newPid = org.getPid();
        long oldPid = origin.getPid();
        String oldPath = origin.getPath();

        String newPath = buildPath(newPid, orgId, isRoot);
        org.setPath(newPath);
        org.setSort(nextSort(newPid));
        this.mapper.updateById(org);

        if (oldPath != null && !oldPath.isEmpty()) {
            this.mapper.refreshPath(oldPath, oldPath.length(), newPath);
        }

        if (oldPid != ROOT_PID) {
            refreshParentLeaf(Set.of(oldPid));
        }
        if (!isRoot) {
            setParentLeaf(newPid, false);
        }
    }
}
