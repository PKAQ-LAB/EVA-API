package org.pkaq.sys.organization.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.bo.OrganizationQueryBo;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.pkaq.sys.organization.vo.OrganizationDetailVo;
import org.pkaq.sys.organization.vo.OrganizationListVo;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface OrganizationConvert extends Convert<OrganizationEntity> {
    OrganizationEntity queryBoToEntity(OrganizationQueryBo queryBo);

    OrganizationEntity aoeBoToEntity(OrganizationAoeBo aoeBo);

    List<OrganizationListVo> entityToListVo(List<OrganizationEntity> entityList);

    OrganizationDetailVo entityToDetailVo(OrganizationEntity entity);

}
