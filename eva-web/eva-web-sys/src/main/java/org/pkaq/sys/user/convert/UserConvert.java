package org.pkaq.sys.user.convert;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.vo.DictViewVo;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.vo.UserListVo;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConvert {
//
//    DictViewVo entityToVo(DictEntity dict);
//
    List<UserListVo> toVoList(List<UserEntity> list);

    UserEntity boToEntity(UserAoeBo bo);

//    DictItemEntity boToItemEntity(DictAoeLineBo bo);

}
