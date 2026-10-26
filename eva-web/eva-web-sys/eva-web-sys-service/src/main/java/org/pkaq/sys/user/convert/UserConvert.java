package org.pkaq.sys.user.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.pkaq.core.enums.BaseEnum;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.bo.UserQueryBo;
import org.pkaq.sys.user.bo.UserTenantAdminBo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.pkaq.sys.user.vo.UserListVo;
import org.pkaq.sys.user.vo.UserSimpleVo;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class, imports = {BaseEnum.class, FrozenEnumm.class})
public interface UserConvert {

    UserListVo entityToListVo(UserEntity entity);

    @Mapping(target = "frozen", expression = "java(BaseEnum.of(FrozenEnumm.class, bo == null ? null : bo.getFrozen()))")
    UserEntity boToEntity(UserAoeBo bo);

    @Mapping(target = "frozen", expression = "java(BaseEnum.of(FrozenEnumm.class, "
            + "queryBo == null ? null : queryBo.getFrozen()))")
    UserEntity boToEntity(UserQueryBo queryBo);

    UserEntity boToEntity(UserTenantAdminBo bo);

    List<UserListVo> entityToListVo(List<UserEntity> list);

    @Mapping(target = "assignedRoles", ignore = true)
    UserDetailVo entityToDetailVo(UserEntity user);

    List<UserSimpleVo> entityToSimpleVo(List<UserEntity> user);
}
