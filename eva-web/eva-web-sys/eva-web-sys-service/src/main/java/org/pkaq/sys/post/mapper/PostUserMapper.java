package org.pkaq.sys.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.post.entity.PostUserEntity;
import org.springframework.stereotype.Repository;

/**
 * 岗位用户关系mapper
 *
 * @author PKAQ
 */
@Mapper
@Repository
@Ignore
public interface PostUserMapper extends BaseMapper<PostUserEntity> {
    /** 与既有实际级联SQL相同的活动节点范围，变更前捕获受影响账号。 */
    @Select("""
            <script>SELECT DISTINCT ref.USER_ID FROM SYS_POSTUSER_REF ref
            JOIN SYS_POST post ON post.ID=ref.POST_ID
            WHERE post.DELETED=0 AND (post.ID=#{id}
            <if test="path != null and path != ''">OR post.PATH LIKE CONCAT(#{path}, '/%')</if>)
            AND (post.FROZEN IS NULL OR post.FROZEN &lt;&gt; #{target})</script>
            """)
    java.util.List<Long> selectCascadeUserIds(@Param("id") Long id, @Param("path") String path,
                                              @Param("target") Integer target);

}
