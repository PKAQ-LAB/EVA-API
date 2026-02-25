package org.pkaq.sys.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
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

}
