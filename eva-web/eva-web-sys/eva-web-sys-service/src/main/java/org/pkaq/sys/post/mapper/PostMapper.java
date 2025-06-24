package org.pkaq.sys.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.sys.post.bo.PostQueryBo;
import org.pkaq.sys.post.entity.PostEntity;
import org.pkaq.sys.post.vo.PostListVo;

/**
 * @author dmz
 */
@Mapper
public interface PostMapper extends BaseMapper<PostEntity> {

    /**
     * 岗位管理列表查询
     *
     * @param queryBo 传入参数
     * @return 列表
     */
    IPage<PostListVo> list(PageResult<PostListVo> page, @Param("q") PostQueryBo queryBo);

}
