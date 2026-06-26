package org.pkaq.sys.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.TreeHelper;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.bo.OrganizationQueryBo;
import org.pkaq.sys.organization.bo.OrganizationSortBo;
import org.pkaq.sys.organization.convert.OrganizationConvert;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.organization.vo.OrganizationDetailVo;
import org.pkaq.sys.organization.vo.OrganizationListVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组织 / 部门管理 Service —— 树形结构（CRUD + 冻结 + 同级拖拽）标准范本
 * <p>
 * 数据约定（与 sys_module / sys_post 一致）：
 * 1. pid 非空，根节点 pid = 0
 * 2. path 形如 "/{id}"（根）、"/{parentPath}/{id}"（子孙）
 * 3. sort 同级递增，跨级移动后自动追加到末尾
 * 4. isleaf：新增 / 移走最后一个子节点时自动维护
 * <p>
 * 注意：删除时"部门-用户引用"检查暂未实现，后续作为 ReferenceChecker SPI 接入。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class OrganizationService extends StdService<OrganizationMapper, OrganizationEntity> {
    /** 根节点 pid 哨兵值 */
    private static final long ROOT_PID = 0L;

    private final OrganizationConvert organizationConvert;

    /**
     * 校验编码在同 pid 下唯一（同租户由拦截器隔离）
     *
     * @return true = 已存在重复
     */
    public boolean checkUnique(OrganizationAoeBo bo) {
        if (bo == null) {
            return false;
        }
        long pid = bo.getPid() == null ? ROOT_PID : bo.getPid();
        LambdaQueryWrapper<OrganizationEntity> wrapper = new LambdaQueryWrapper<OrganizationEntity>()
                .eq(OrganizationEntity::getPid, pid)
                .and(w -> w
                        .eq(bo.getCode() != null && !bo.getCode().isEmpty(), OrganizationEntity::getCode, bo.getCode())
                        .or()
                        .eq(bo.getName() != null && !bo.getName().isEmpty(), OrganizationEntity::getName, bo.getName()));

        if (bo.getId() != null && bo.getId() != 0L) {
            wrapper.ne(OrganizationEntity::getId, bo.getId());
        }
        return this.mapper.selectCount(wrapper) > 0;
    }

    /**
     * 树形列表查询
     */
    public Collection<OrganizationListVo> list(OrganizationQueryBo queryBo) {
        Map<Long, OrganizationListVo> orgMap = this.mapper.selectOrgMapList(queryBo);
        if (CollUtils.isEmpty(orgMap)) {
            return Collections.emptyList();
        }
        return TreeHelper.buildTree(orgMap.values());
    }

    /**
     * 新增 / 编辑组织
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(OrganizationAoeBo bo) {
        OrganizationEntity org = this.organizationConvert.aoeBoToEntity(bo);
        // 入口规范化：前端未传 pid 时统一为根节点哨兵 0
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
        } else {
            OrganizationEntity origin = this.mapper.selectById(orgId);
            if (origin == null) {
                CommonCodes.CAN_NOT_FIND_RECORD.newException(orgId);
                return;
            }

            if (!Objects.equals(origin.getPid(), pid)) {
                handleParentChange(org, origin, isRoot);
            } else {
                org.setPath(origin.getPath());
                org.setSort(origin.getSort());
                this.mapper.updateById(org);
            }
        }
    }

    /**
     * 详情查询
     */
    public OrganizationDetailVo get(Long id) {
        OrganizationEntity entity = this.mapper.selectById(id);
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(id);
            return null;
        }
        OrganizationDetailVo vo = this.organizationConvert.entityToDetailVo(entity);
        // 回填上级节点名称
        if (entity.getPid() != null && entity.getPid() != ROOT_PID) {
            OrganizationEntity parent = this.mapper.selectById(entity.getPid());
            if (parent != null) {
                vo.setParentName(parent.getName());
            }
        }
        return vo;
    }

    /**
     * 批量删除：
     * - 子节点存在性检查（不允许删除非叶子）
     * - 删除后维护原父节点 isleaf
     * - 部门-用户引用检查留待 ReferenceChecker SPI 后续接入
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return;
        }

        // 子节点存在性检查
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

        // 收集原父节点 id（非根）
        Set<Long> originPids = this.mapper.selectList(new LambdaQueryWrapper<OrganizationEntity>()
                        .select(OrganizationEntity::getPid)
                        .in(OrganizationEntity::getId, ids))
                .stream()
                .map(OrganizationEntity::getPid)
                .filter(pid -> pid != null && pid != ROOT_PID)
                .collect(Collectors.toSet());

        // 删除组织本体（StdEntity @TableLogic → 逻辑删）
        this.mapper.delete(new LambdaQueryWrapper<OrganizationEntity>().in(OrganizationEntity::getId, ids));

        // 刷新原父节点 isleaf
        refreshParentLeaf(originPids);
    }

    /**
     * 同级拖拽排序
     */
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
        if (bo.getOldSort() == bo.getNewSort()) {
            return;
        }
        this.mapper.updateSort(bo.getId(), self.getPid(), bo.getOldSort(), bo.getNewSort());
    }

    /**
     * 批量切换冻结状态（逐个翻转，子节点跳过判断）
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void switchFrozen(SingleArray<Long> ids) {
        if (ids == null || CollUtils.isEmpty(ids.getParam())) {
            return;
        }
        for (Long id : ids.getParam()) {
            OrganizationEntity self = this.mapper.selectById(id);
            if (self == null || self.getFrozen() == FrozenEnumm.READ_ONLY) {
                continue;
            }
            FrozenEnumm target = self.getFrozen() == FrozenEnumm.FROZEN
                    ? FrozenEnumm.UN_FROZEN
                    : FrozenEnumm.FROZEN;

            // 解锁时若父节点为冻结，禁止解锁子节点
            if (target == FrozenEnumm.UN_FROZEN && self.getPid() != null && self.getPid() != ROOT_PID) {
                OrganizationEntity parent = this.mapper.selectById(self.getPid());
                if (parent != null && parent.getFrozen() == FrozenEnumm.FROZEN) {
                    continue;
                }
            }
            this.mapper.cascadeFrozen(id, self.getPath(), target.getCode());
        }
    }

    // ------------------------------------------------------------------
    // 私有辅助方法（与 ModuleService / PostService 范本一致）
    // ------------------------------------------------------------------

    /** 计算节点 path：根节点 = "/{id}"，非根 = "{parentPath}/{id}" */
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

    /** 取指定父节点下的下一个 sort 值 */
    private double nextSort(long pid) {
        Integer maxSort = this.mapper.listOrder(pid);
        return (maxSort == null ? 0 : maxSort) + 1;
    }

    /** 设置指定节点的 isleaf */
    private void setParentLeaf(long pid, boolean isleaf) {
        this.mapper.update(null, new LambdaUpdateWrapper<OrganizationEntity>()
                .eq(OrganizationEntity::getId, pid)
                .set(OrganizationEntity::getIsleaf, isleaf));
    }

    /** 对一批父节点 id，若已无子节点则置 isleaf=true */
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

    /** 处理父节点变更：重算 path、刷新所有子孙 path、维护两边 isleaf */
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
