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
 * 岗位管理服务。
 * <p>
 * 维护岗位树的增删改查、冻结、同级排序和 path/isleaf 等树形字段。
 *
 * @author dmz
 */
@Service
@RequiredArgsConstructor
public class PostService extends StdService<PostMapper, PostEntity> {
    /** 根节点 pid，与数据库默认值保持一致。 */
    private static final long ROOT_PID = 0L;

    private final PostConvert postConvert;
    private final PostUserMapper postUserMapper;

    /**
     * 校验同级岗位 code 或名称是否重复。
     *
     * @return true 表示已存在
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
     * 查询。
     */
    public Collection<PostListVo> list(PostQueryBo queryBo) {
        Map<Long, PostListVo> postMap = this.mapper.selectPostMapList(queryBo);
        if (CollUtils.isEmpty(postMap)) {
            return Collections.emptyList();
        }
        return TreeHelper.buildTree(postMap.values());
    }

    /**
     * 新增或编辑岗位。
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(PostAoeBo bo) {
        PostEntity post = this.postConvert.aoeBoToEntity(bo);
        if (post.getPid() == null) {
            post.setPid(ROOT_PID);
        }

        Long postId = post.getId();
        long pid = post.getPid();
        boolean isNew = postId == null || postId == 0L;
        boolean isRoot = pid == ROOT_PID;

        if (isNew) {
            postId = IdWorker.getId();
            post.setId(postId);
            post.setIsleaf(true);
            post.setPath(buildPath(pid, postId, isRoot));
            post.setSort(nextSort(pid));
            this.mapper.insert(post);

            if (!isRoot) {
                setParentLeaf(pid, false);
            }
            return;
        }

        PostEntity origin = this.mapper.selectById(postId);
        if (origin == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(postId);
            return;
        }

        if (!Objects.equals(origin.getPid(), pid)) {
            handleParentChange(post, origin, isRoot);
            return;
        }

        post.setPath(origin.getPath());
        post.setSort(origin.getSort());
        this.mapper.updateById(post);
    }

    /**
     * 查询岗位详情。
     */
    public PostDetailVo get(Long id) {
        PostEntity entity = this.mapper.selectById(id);
        if (entity == null) {
            SysCodes.RECORD_NOT_FOUND.newException();
            return null;
        }
        PostDetailVo vo = this.postConvert.entityToDetailVo(entity);
        if (entity.getPid() != null && entity.getPid() != ROOT_PID) {
            PostEntity parent = this.mapper.selectById(entity.getPid());
            if (parent != null) {
                vo.setParentTitle(parent.getTitle());
            }
        }
        return vo;
    }

    /**
     * 删除岗位，并清理岗位-用户关系。
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void del(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return;
        }
        if (ids.size() > 100) {
            SysCodes.DELETE_LIMIT.newException();
        }

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

        Set<Long> originPids = this.mapper.selectList(new LambdaQueryWrapper<PostEntity>()
                        .select(PostEntity::getPid)
                        .in(PostEntity::getId, ids))
                .stream()
                .map(PostEntity::getPid)
                .filter(pid -> pid != null && pid != ROOT_PID)
                .collect(Collectors.toSet());

        this.mapper.delete(new LambdaQueryWrapper<PostEntity>().in(PostEntity::getId, ids));
        this.postUserMapper.delete(new LambdaQueryWrapper<PostUserEntity>().in(PostUserEntity::getPostId, ids));
        refreshParentLeaf(originPids);
    }

    /**
     * 同级拖拽排序。
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
     * 切换冻结状态，并级联处理子节点。
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

            if (target == FrozenEnumm.UN_FROZEN && self.getPid() != null && self.getPid() != ROOT_PID) {
                PostEntity parent = this.mapper.selectById(self.getPid());
                if (parent != null && parent.getFrozen() == FrozenEnumm.FROZEN) {
                    continue;
                }
            }
            this.mapper.cascadeFrozen(id, self.getPath(), target.getCode());
        }
    }

    /**
     * 构建岗位 path。
     */
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
        this.mapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, pid)
                .set(PostEntity::getIsleaf, isleaf));
    }

    /**
     * 刷新父节点叶子状态。
     */
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

    /** 更换父节点时刷新当前节点、子孙节点路径以及新旧父节点叶子状态。 */
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
