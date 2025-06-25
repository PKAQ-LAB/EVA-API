package org.pkaq.sys.post.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.post.bo.PostAoeBo;
import org.pkaq.sys.post.entity.PostEntity;
import org.pkaq.sys.post.vo.PostDetailVo;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface PostConvert extends Convert<PostEntity> {
    PostEntity aoeBoToEntity(PostAoeBo bo);

    PostDetailVo entityToDetailVo(PostEntity entity);
}
