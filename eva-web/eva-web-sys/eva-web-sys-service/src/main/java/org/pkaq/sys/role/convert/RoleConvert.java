package org.pkaq.sys.role.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.sys.role.bo.RoleQueryBo;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.vo.RoleDetailVo;
import org.pkaq.sys.role.vo.RoleGrantedModuleVo;
import org.pkaq.sys.role.vo.RoleListVo;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper(componentModel = "spring", config = MapConvertConfig.class)
public abstract class RoleConvert extends Convert {

    public abstract RoleEntity fromBo(RoleQueryBo queryBo);

    public abstract RoleDetailVo toVo(RoleEntity roleEntity);

    public abstract RoleListVo toListItemVo(RoleEntity roleEntity);

    public abstract List<RoleListVo> toListVo(List<RoleEntity> roleEntities);

    public abstract RoleEntity fromBo(RoleAoeBo roleAoeBo);

    public abstract List<RoleGrantedModuleVo> entityToModuleList(List<ModuleEntity> entities);

}
