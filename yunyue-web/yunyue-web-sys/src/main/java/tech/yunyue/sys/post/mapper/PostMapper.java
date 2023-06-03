package tech.yunyue.sys.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.yunyue.sys.post.bo.PostQueryBo;
import tech.yunyue.sys.post.entity.PostEntity;
import tech.yunyue.sys.post.vo.PostTableVo;

import java.util.List;

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
    List<PostTableVo> list(@Param("code") String code,@Param("q") PostQueryBo queryBo);

}
