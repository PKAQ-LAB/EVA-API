package org.pkaq.sys.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.post.entity.PostUserEntity;
import org.pkaq.sys.post.mapper.PostUserMapper;
import org.pkaq.sys.user.bo.UserPostBo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户-岗位 关系维护服务
 * <p>
 * savePosts 采用 diff 模式：避免"先全删再全插"导致的事务窗口期
 * （在 grantUser / handleResources 之外保持一致风格）
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserPostRefSerivce {
    private final PostUserMapper postUserMapper;

    /**
     * 保存用户的岗位授权关系（diff 模式）
     */
    @BizLog(operateType = BizLogCodes.EDIT, description = "更新用户岗位关系[{0}]", args = {"param:0.userId"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void savePosts(UserPostBo bo) {
        if (bo == null || bo.getUserId() == null) {
            return;
        }
        Long userId = bo.getUserId();

        // 当前已有的 postId 集合
        Set<Long> existing = this.postUserMapper.selectList(
                        new LambdaQueryWrapper<PostUserEntity>()
                                .select(PostUserEntity::getPostId)
                                .eq(PostUserEntity::getUserId, userId))
                .stream()
                .map(PostUserEntity::getPostId)
                .collect(Collectors.toSet());

        Set<Long> incoming = CollUtils.isEmpty(bo.getPostIds())
                ? new HashSet<>()
                : new HashSet<>(bo.getPostIds());

        // diff
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
                this.postUserMapper.insert(ref);
            }
        }
    }

    /**
     * 查询用户拥有的所有 postId
     */
    public List<Long> listPostIdsByUserId(Long userId) {
        if (userId == null) {
            return java.util.Collections.emptyList();
        }
        return this.postUserMapper.selectList(new LambdaQueryWrapper<PostUserEntity>()
                        .select(PostUserEntity::getPostId)
                        .eq(PostUserEntity::getUserId, userId))
                .stream()
                .map(PostUserEntity::getPostId)
                .collect(Collectors.toList());
    }
}
