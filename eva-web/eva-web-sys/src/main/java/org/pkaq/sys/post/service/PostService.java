package org.pkaq.sys.post.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.mybatis.util.Page;
import org.pkaq.sys.SysCodeEnum;
import org.pkaq.sys.post.bo.PostEditBo;
import org.pkaq.sys.post.bo.PostQueryBo;
import org.pkaq.sys.post.consts.SYSConstant;
import org.pkaq.sys.post.entity.PostEntity;
import org.pkaq.sys.post.mapper.PostMapper;
import org.pkaq.sys.post.vo.PostDetailVo;
import org.pkaq.sys.post.vo.PostTableVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author dmz
 */
@Service
@RequiredArgsConstructor
@Schema(description = "岗位管理")
public class PostService {

    /**
     * 岗位Mapper
     */
    private final PostMapper postMapper;

    /**
     * 编码/供应商名称 唯一性校验
     */
    public boolean checkUnique(PostEditBo postEditBo) {
        LambdaQueryWrapper<PostEntity> wrapper = Wrappers.lambdaQuery();

        wrapper.ne(CharSequenceUtil.isNotBlank(postEditBo.getId()), PostEntity::getId, postEditBo.getId())
                .and(w -> w.eq(PostEntity::getTitle, postEditBo.getTitle())
                        .or()
                        .eq(PostEntity::getCode, postEditBo.getCode()));

        Long results = this.postMapper.selectCount(wrapper);

        return results < 1;
    }

    /**
     * 分页列表查询
     *
     * @param query 分页参数
     */
    public IPage<PostTableVo> list(PostQueryBo query) {

        return this.postMapper.list(new Page<>(query.getPageNo(), query.getPageSize()), SYSConstant.COMMON_STATUS_DICT, query);
    }

    /**
     * 新增/编辑
     *
     * @param postEditBo 实体参数
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(PostEditBo postEditBo) {

        boolean isUpdate = CharSequenceUtil.isNotBlank(postEditBo.getId());

        PostEntity dto = new PostEntity();
        if (isUpdate) {
            dto = this.postMapper.selectById(postEditBo.getId());
        }

        BeanUtils.copyProperties(postEditBo, dto);
        // 检测通过 保存
        if (isUpdate) {
            this.postMapper.updateById(dto);
        } else {
            this.postMapper.insert(dto);
        }
    }

    /**
     * 根据id查询详情
     */
    public PostDetailVo get(String id) {

        PostDetailVo vo = new PostDetailVo();
        PostEntity entity = this.postMapper.selectById(id);
        if (ObjectUtil.isNull(entity)) {
            SysCodeEnum.RECORD_NOT_FOUND.newException();
        }
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * 批量删除记录
     *
     * @param param 批量传入id
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void del(List<String> param) {
        // 限制： 最多只允许同时删除100条
        if (param.size() > 100) {
            SysCodeEnum.DELETE_LIMIT.newException();
        }

        // 查询所删除ID
        LambdaQueryWrapper<PostEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(PostEntity::getId, param);

        this.postMapper.delete(lambdaQueryWrapper);
    }

}
