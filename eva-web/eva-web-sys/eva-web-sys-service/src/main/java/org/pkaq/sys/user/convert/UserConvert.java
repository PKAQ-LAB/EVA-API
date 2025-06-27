package org.pkaq.sys.user.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.bo.UserQueryBo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.pkaq.sys.user.vo.UserListVo;
import org.pkaq.sys.user.vo.UserSimpleVo;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface UserConvert extends Convert<UserEntity> {

    @Override
    UserListVo entityToListVo(UserEntity entity);

    UserEntity boToEntity(UserAoeBo bo);

    UserEntity boToEntity(UserQueryBo queryBo);

    @Override
    List<UserListVo> entityToListVo(List<UserEntity> list);

    @Override
    UserDetailVo entityToDetailVo(UserEntity user);

    List<UserSimpleVo> entityToSimpleVo(List<UserEntity> user);
}
