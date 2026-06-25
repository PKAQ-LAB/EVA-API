package org.pkaq.sys.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.util.CollUtils;
import org.pkaq.core.util.ObjectUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.post.bo.PostAoeBo;
import org.pkaq.sys.post.bo.PostQueryBo;
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

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 岗位管理 Service
 *
 * @author dmz
 */
@Service
@Schema(description = "岗位管理")
@RequiredArgsConstructor
public class PostService extends StdService<PostMapper, PostEntity> {
    private final PostConvert postConvert;
    private final PostUserMapper postUserMapper;

    /**
     * 校验编码 / 岗位名称唯一性
     *
     * @return true = 已存在重复（编辑场景排除自身）
     */
    public boolean checkUnique(PostAoeBo bo) {
        if (bo == null) {
            return false;
        }
        LambdaQueryWrapper<PostEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.ne(StrUtils.isNotBlank(bo.getId()), PostEntity::getId, bo.getId())
                .and(w -> w.eq(StrUtils.isNotBlank(bo.getTitle()), PostEntity::getTitle, bo.getTitle())
                        .or()
                        .eq(StrUtils.isNotBlank(bo.getCode()), PostEntity::getCode, bo.getCode()));

        return this.mapper.selectCount(wrapper) > 0;
    }

    /**
     * 分页列表查询
     */
    public IPage<PostListVo> list(PostQueryBo query) {
        return this.mapper.list(new PageResult<>(query.getPageNo(), query.getPageSize()), query);
    }

    /**
     * 新增 / 编辑
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(PostAoeBo bo) {
        boolean isUpdate = StrUtils.isNotBlank(bo.getId());
        PostEntity entity = this.postConvert.aoeBoToEntity(bo);
        if (isUpdate) {
            this.mapper.updateById(entity);
        } else {
            this.mapper.insert(entity);
        }
    }

    /**
     * 根据 id 查询详情
     */
    public PostDetailVo get(String id) {
        PostEntity entity = this.mapper.selectById(id);
        if (ObjectUtils.isNull(entity)) {
            SysCodes.RECORD_NOT_FOUND.newException();
        }
        return this.postConvert.entityToDetailVo(entity);
    }

    /**
     * 批量删除：
     * - 限制单次最多 100 条
     * - 同步清理岗位-用户关系
     *
     * @param param 待删除 id 集合
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void del(Set<String> param) {
        if (CollUtils.isEmpty(param)) {
            return;
        }
        if (param.size() > 100) {
            SysCodes.DELETE_LIMIT.newException();
        }

        // 删除岗位本体（StdEntity 启用了 @TableLogic → 逻辑删）
        this.mapper.delete(new LambdaQueryWrapper<PostEntity>().in(PostEntity::getId, param));

        // 清理岗位-用户中间表（物理删，PostUserEntity 无逻辑删）
        Set<Long> postIds = param.stream().map(Long::valueOf).collect(Collectors.toSet());
        this.postUserMapper.delete(new LambdaQueryWrapper<PostUserEntity>().in(PostUserEntity::getPostId, postIds));
    }

    /**
     * 批量切换冻结状态
     * 行为：按当前节点 frozen 翻转（FROZEN ↔ UN_FROZEN），READ_ONLY 节点跳过保护
     *
     * @param ids 岗位 id 集合
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
            this.mapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                    .eq(PostEntity::getId, id)
                    .set(PostEntity::getFrozen, target));
        }
    }
}
