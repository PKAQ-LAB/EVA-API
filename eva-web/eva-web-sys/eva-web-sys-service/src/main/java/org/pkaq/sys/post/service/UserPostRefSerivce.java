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

/**
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserPostRefSerivce {
    private final PostUserMapper postUserMapper;

    @BizLog(operateType = BizLogCodes.EDIT, description = "更新用户岗位关系[{0}]", args = {"param:0.userId"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void savePosts(UserPostBo bo) {
        // 保存权限
        if (CollUtils.isNotEmpty(bo.getPostIds())) {
            // 先删除该用户原有的权限
            LambdaQueryWrapper<PostUserEntity> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(PostUserEntity::getUserId, bo.getUserId());

            this.postUserMapper.delete(deleteWrapper);
            // 再插入更新后的权限
            bo.getPostIds().forEach(item -> {
                PostUserEntity postUserEntity = new PostUserEntity();
                postUserEntity.setPostId(item);
                postUserEntity.setUserId(bo.getUserId());
                postUserMapper.insert(postUserEntity);
            });
        }
    }
}
