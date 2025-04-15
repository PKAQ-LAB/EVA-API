package org.pkaq.sys.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.mybatis.util.Page;
import org.pkaq.sys.post.bo.PostQueryBo;
import org.pkaq.sys.post.entity.PostEntity;
import org.pkaq.sys.post.vo.PostTableVo;

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
    IPage<PostTableVo> list(Page<PostTableVo> page, @Param("code") String code, @Param("q") PostQueryBo queryBo);

}
