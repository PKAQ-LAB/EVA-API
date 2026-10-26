package org.pkaq.sys.organization.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.pkaq.core.enums.BaseEnum;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.pkaq.sys.organization.vo.OrganizationDetailVo;
import org.pkaq.sys.organization.vo.OrganizationListVo;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class, imports = {BaseEnum.class, FrozenEnumm.class})
public interface OrganizationConvert {

    @Mapping(target = "frozen", expression = "java(BaseEnum.of(FrozenEnumm.class, aoeBo.getFrozen()))")
    OrganizationEntity aoeBoToEntity(OrganizationAoeBo aoeBo);

    OrganizationDetailVo entityToDetailVo(OrganizationEntity entity);

    List<OrganizationListVo> entityToListVo(List<OrganizationEntity> entities);
}
