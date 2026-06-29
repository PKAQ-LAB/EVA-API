package org.pkaq.sys.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.sys.post.bo.PostQueryBo;
import org.pkaq.sys.post.entity.PostEntity;
import org.pkaq.sys.post.vo.PostListVo;

import java.util.List;
import java.util.Map;

/**
 * 岗位管理 Mapper
 *
 * @author dmz
 */
@Mapper
public interface PostMapper extends BaseMapper<PostEntity> {

    /**
     * 树形查询（含 parentTitle 回填，按 pid + sort 排序）
     */
    @MapKey("id")
    Map<Long, PostListVo> selectPostMapList(@Param("q") PostQueryBo queryBo);

    /**
     * 取指定父节点下当前最大 sort（用于新增节点 sort = max + 1）
     */
    Integer listOrder(@Param("pid") Long pid);

    /**
     * 同级拖拽排序（仅在相同 pid 下生效）。根节点之间排序时 pid = 0。
     */
    void updateSort(@Param("id") Long id,
                    @Param("pid") Long pid,
                    @Param("oldSort") Integer oldSort,
                    @Param("newSort") Integer newSort);

    /**
     * 级联冻结/解锁：自身 + 所有 path 以本节点 path 为前缀的子孙
     */
    void cascadeFrozen(@Param("id") Long id,
                       @Param("path") String path,
                       @Param("frozen") Integer frozen);

    /**
     * 移动节点后刷新所有子孙 path 前缀
     */
    void refreshPath(@Param("oldPath") String oldPath,
                     @Param("oldPathLength") int oldPathLength,
                     @Param("newPath") String newPath);
}
