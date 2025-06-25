package org.pkaq.sys.role.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.sys.role.bo.RoleQueryBo;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.vo.RoleDetailVo;
import org.pkaq.sys.role.vo.RoleListVo;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface RoleConvert extends Convert<RoleEntity> {
    RoleEntity queryBoToEntity(RoleQueryBo queryBo);

    List<RoleListVo> listToVoList(List<RoleEntity> roleEntities);

    RoleListVo entityToListVo(RoleEntity roleEntity);

    RoleEntity boToEntity(RoleAoeBo roleAoeBo);

    RoleDetailVo entityToDetailVo(RoleEntity roleEntity);
}
