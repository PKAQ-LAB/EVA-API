package org.pkaq.sys.organization.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.pkaq.sys.organization.vo.OrganizationDetailVo;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface OrganizationConvert {

    OrganizationEntity aoeBoToEntity(OrganizationAoeBo aoeBo);

    OrganizationDetailVo entityToDetailVo(OrganizationEntity entity);
}
