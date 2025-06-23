package org.pkaq.sys.user.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.bo.UserQueryBo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.pkaq.sys.user.vo.UserListVo;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface UserConvert extends Convert {

    UserListVo entityToListVo(UserEntity entity);

    UserEntity boToEntity(UserAoeBo bo);

    UserEntity queryBoToEntity(UserQueryBo queryBo);

    List<UserListVo> entityListToVoList(List<UserEntity> list);

    UserDetailVo entityToDetilVo(UserEntity user);
}
