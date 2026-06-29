package org.pkaq.sys.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.post.entity.PostEntity;
import org.pkaq.sys.post.entity.PostUserEntity;
import org.pkaq.sys.post.mapper.PostMapper;
import org.pkaq.sys.post.mapper.PostUserMapper;
import org.pkaq.sys.user.bo.UserPostBo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户岗位关系服务
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserPostRefSerivce {
    private final PostUserMapper postUserMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final EvaConfig evaConfig;

    /**
     * 保存用户岗位关系。
     *
     * @param bo 用户岗位参数
     */
    @BizLog(operateType = BizLogCodes.EDIT, description = "更新用户岗位关系[{0}]", args = {"param:0.userId"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void savePosts(UserPostBo bo) {
        if (bo == null || bo.getUserId() == null) {
            SysCodes.CANNOT_FIND_USER.newException();
            return;
        }

        Long userId = bo.getUserId();
        this.ensureUserExists(userId);

        Set<Long> incoming = this.sanitizeIds(bo.getPostIds());
        this.ensurePostsUsable(incoming);

        Set<Long> existing = this.postUserMapper.selectList(
                        new LambdaQueryWrapper<PostUserEntity>()
                                .select(PostUserEntity::getPostId)
                                .eq(PostUserEntity::getUserId, userId))
                .stream()
                .map(PostUserEntity::getPostId)
                .collect(Collectors.toSet());

        Set<Long> toInsert = new HashSet<>(incoming);
        toInsert.removeAll(existing);
        Set<Long> toDelete = new HashSet<>(existing);
        toDelete.removeAll(incoming);

        if (!toDelete.isEmpty()) {
            this.postUserMapper.delete(new LambdaQueryWrapper<PostUserEntity>()
                    .eq(PostUserEntity::getUserId, userId)
                    .in(PostUserEntity::getPostId, toDelete));
        }
        if (!toInsert.isEmpty()) {
            for (Long postId : toInsert) {
                PostUserEntity ref = new PostUserEntity();
                ref.setUserId(userId);
                ref.setPostId(postId);
                ref.setTenantId(resolveTenantId());
                this.postUserMapper.insert(ref);
            }
        }

        if (!toInsert.isEmpty() || !toDelete.isEmpty()) {
            this.userMapper.incrementPermVer(userId);
        }
    }

    /**
     * 查询用户拥有的岗位ID。
     *
     * @param userId 用户ID
     * @return 岗位ID列表
     */
    public List<Long> listPostIdsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return this.postUserMapper.selectList(new LambdaQueryWrapper<PostUserEntity>()
                        .select(PostUserEntity::getPostId)
                        .eq(PostUserEntity::getUserId, userId))
                .stream()
                .map(PostUserEntity::getPostId)
                .collect(Collectors.toList());
    }

    private void ensureUserExists(Long userId) {
        UserEntity user = this.userMapper.selectById(userId);
        if (user == null) {
            SysCodes.CANNOT_FIND_USER.newException();
        }
    }

    private void ensurePostsUsable(Set<Long> postIds) {
        if (postIds.isEmpty()) {
            return;
        }

        List<PostEntity> posts = this.postMapper.selectList(new LambdaQueryWrapper<PostEntity>()
                .select(PostEntity::getId)
                .in(PostEntity::getId, postIds)
                .ne(PostEntity::getFrozen, FrozenEnumm.FROZEN));
        if (posts.size() != postIds.size()) {
            SysCodes.RECORD_NOT_FOUND.newException();
        }
    }

    private Long resolveTenantId() {
        if (evaConfig.isStandaloneMode()) {
            return 0L;
        }
        if (evaConfig.isPlatformMode()) {
            return -1L;
        }
        return ThreadUserHelper.getTenantId();
    }

    private Set<Long> sanitizeIds(List<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return Collections.emptySet();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0L)
                .collect(Collectors.toSet());
    }
}
