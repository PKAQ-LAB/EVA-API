package org.pkaq.sys.role.convert;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.sys.role.bo.RoleQueryBo;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.vo.RoleDetailVo;
import org.pkaq.sys.role.vo.RoleListVo;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleConvert {
    RoleEntity queryBoToEntity(RoleQueryBo queryBo);

    List<RoleListVo> listToVoList(List<RoleEntity> roleEntities);

    RoleListVo entityToListVo(RoleEntity roleEntity);

    RoleEntity boToEntity(RoleAoeBo roleAoeBo);

    RoleDetailVo entityToDetailVo(RoleEntity roleEntity);
}
