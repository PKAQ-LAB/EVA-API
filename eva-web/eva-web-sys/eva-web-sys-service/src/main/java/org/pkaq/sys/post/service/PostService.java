package org.pkaq.sys.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.TreeHelper;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.post.bo.PostAoeBo;
import org.pkaq.sys.post.bo.PostQueryBo;
import org.pkaq.sys.post.bo.PostSortBo;
import org.pkaq.sys.post.convert.PostConvert;
import org.pkaq.sys.post.entity.PostEntity;
import org.pkaq.sys.post.entity.PostUserEntity;
import org.pkaq.sys.post.mapper.PostMapper;
import org.pkaq.sys.post.mapper.PostUserMapper;
import org.pkaq.sys.post.vo.PostDetailVo;
import org.pkaq.sys.post.vo.PostListVo;
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
 * 岗位管理 Service —— 树形结构（CRUD + 冻结 + 同级拖拽）标准范本
 * <p>
 * 数据约定（与 sys_module 一致）：
 * 1. pid 非空，根节点 pid = 0
 * 2. path 形如 "/{id}"（根）、"/{parentPath}/{id}"（子孙）
 * 3. sort 同级递增，跨级移动后自动追加到末尾
 * 4. isleaf：新增/移走最后一个子节点时自动维护
 *
 * @author dmz
 */
@Service
@RequiredArgsConstructor
public class PostService extends StdService<PostMapper, PostEntity> {
    /** 根节点 pid 哨兵值 */
    private static final long ROOT_PID = 0L;

    private final PostConvert postConvert;
    private final PostUserMapper postUserMapper;

    /**
     * 校验编码 / 岗位名称在同 pid 下唯一（同租户由拦截器隔离）
     *
     * @return true = 已存在重复
     */
    public boolean checkUnique(PostAoeBo bo) {
        if (bo == null) {
            return false;
        }
        long pid = bo.getPid() == null ? ROOT_PID : bo.getPid();
        LambdaQueryWrapper<PostEntity> wrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getPid, pid)
                .and(w -> w
                        .eq(bo.getCode() != null && !bo.getCode().isEmpty(), PostEntity::getCode, bo.getCode())
                        .or()
                        .eq(bo.getTitle() != null && !bo.getTitle().isEmpty(), PostEntity::getTitle, bo.getTitle()));

        if (bo.getId() != null && bo.getId() != 0L) {
            wrapper.ne(PostEntity::getId, bo.getId());
        }
        return this.mapper.selectCount(wrapper) > 0;
    }

    /**
     * 树形列表查询
     */
    public Collection<PostListVo> list(PostQueryBo queryBo) {
        Map<Long, PostListVo> postMap = this.mapper.selectPostMapList(queryBo);
        if (CollUtils.isEmpty(postMap)) {
            return Collections.emptyList();
        }
        return TreeHelper.buildTree(postMap.values());
    }

    /**
     * 新增 / 编辑岗位
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(PostAoeBo bo) {
        PostEntity post = this.postConvert.aoeBoToEntity(bo);
        // 入口规范化：前端未传 pid 时统一为根节点哨兵 0
        if (post.getPid() == null) {
            post.setPid(ROOT_PID);
        }

        Long postId = post.getId();
        long pid = post.getPid();
        boolean isNew = postId == null || postId == 0L;
        boolean isRoot = pid == ROOT_PID;

        if (isNew) {
            // 新增：先生成 id 以便计算 path
            postId = IdWorker.getId();
            post.setId(postId);
            post.setIsleaf(true);
            post.setPath(buildPath(pid, postId, isRoot));
            post.setSort(nextSort(pid));
            this.mapper.insert(post);

            if (!isRoot) {
                setParentLeaf(pid, false);
            }
        } else {
            PostEntity origin = this.mapper.selectById(postId);
            if (origin == null) {
                CommonCodes.CAN_NOT_FIND_RECORD.newException(postId);
                return;
            }

            if (!Objects.equals(origin.getPid(), pid)) {
                handleParentChange(post, origin, isRoot);
            } else {
                post.setPath(origin.getPath());
                post.setSort(origin.getSort());
                this.mapper.updateById(post);
            }
        }
    }

    /**
     * 详情查询
     */
    public PostDetailVo get(Long id) {
        PostEntity entity = this.mapper.selectById(id);
        if (entity == null) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return null;
        }
        PostDetailVo vo = this.postConvert.entityToDetailVo(entity);
        // 回填上级岗位名称
        if (entity.getPid() != null && entity.getPid() != ROOT_PID) {
            PostEntity parent = this.mapper.selectById(entity.getPid());
            if (parent != null) {
                vo.setParentTitle(parent.getTitle());
            }
        }
        return vo;
    }

    /**
     * 批量删除：
     * - 子节点存在性检查（不允许删除非叶子）
     * - 同步清理岗位-用户关系
     * - 删除后维护原父节点 isleaf
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void del(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return;
        }
        if (ids.size() > 100) {
            SysCodes.DELETE_LIMIT.newException();
        }

        // 子节点存在性检查
        List<PostEntity> leafList = this.mapper.selectList(new LambdaQueryWrapper<PostEntity>()
                .in(PostEntity::getPid, ids));
        if (CollUtils.isNotEmpty(leafList)) {
            String nameStr = leafList.stream()
                    .map(PostEntity::getTitle)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            CommonCodes.CHILD_EXIST.newException(nameStr);
            return;
        }

        // 收集原父节点 id（非根）
        Set<Long> originPids = this.mapper.selectList(new LambdaQueryWrapper<PostEntity>()
                        .select(PostEntity::getPid)
                        .in(PostEntity::getId, ids))
                .stream()
                .map(PostEntity::getPid)
                .filter(pid -> pid != null && pid != ROOT_PID)
                .collect(Collectors.toSet());

        // 删除岗位本体（StdEntity @TableLogic → 逻辑删）
        this.mapper.delete(new LambdaQueryWrapper<PostEntity>().in(PostEntity::getId, ids));
        // 物理清理岗位-用户关系
        this.postUserMapper.delete(new LambdaQueryWrapper<PostUserEntity>().in(PostUserEntity::getPostId, ids));

        // 刷新原父节点 isleaf
        refreshParentLeaf(originPids);
    }

    /**
     * 同级拖拽排序
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void sort(PostSortBo bo) {
        if (bo == null || bo.getId() == null) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        PostEntity self = this.mapper.selectById(bo.getId());
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
            PostEntity self = this.mapper.selectById(id);
            if (self == null || self.getFrozen() == FrozenEnumm.READ_ONLY) {
                continue;
            }
            FrozenEnumm target = self.getFrozen() == FrozenEnumm.FROZEN
                    ? FrozenEnumm.UN_FROZEN
                    : FrozenEnumm.FROZEN;

            // 解锁时若父节点为冻结，禁止解锁子节点
            if (target == FrozenEnumm.UN_FROZEN && self.getPid() != null && self.getPid() != ROOT_PID) {
                PostEntity parent = this.mapper.selectById(self.getPid());
                if (parent != null && parent.getFrozen() == FrozenEnumm.FROZEN) {
                    continue;
                }
            }
            this.mapper.cascadeFrozen(id, self.getPath(), target.getCode());
        }
    }

    // ------------------------------------------------------------------
    // 私有辅助方法（与 ModuleService 范本一致）
    // ------------------------------------------------------------------

    /** 计算节点 path：根节点 = "/{id}"，非根 = "{parentPath}/{id}" */
    private String buildPath(long pid, long id, boolean isRoot) {
        if (isRoot) {
            return "/" + id;
        }
        PostEntity parent = this.mapper.selectById(pid);
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
        this.mapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, pid)
                .set(PostEntity::getIsleaf, isleaf));
    }

    /** 对一批父节点 id，若已无子节点则置 isleaf=true */
    private void refreshParentLeaf(Set<Long> parentIds) {
        if (CollUtils.isEmpty(parentIds)) {
            return;
        }
        for (Long pid : parentIds) {
            Long childCount = this.mapper.selectCount(new LambdaQueryWrapper<PostEntity>()
                    .eq(PostEntity::getPid, pid));
            if (childCount == null || childCount == 0L) {
                setParentLeaf(pid, true);
            }
        }
    }

    /** 处理父节点变更：重算 path、刷新所有子孙 path、维护两边 isleaf */
    private void handleParentChange(PostEntity post, PostEntity origin, boolean isRoot) {
        long postId = post.getId();
        long newPid = post.getPid();
        long oldPid = origin.getPid();
        String oldPath = origin.getPath();

        String newPath = buildPath(newPid, postId, isRoot);
        post.setPath(newPath);
        post.setSort(nextSort(newPid));
        this.mapper.updateById(post);

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
