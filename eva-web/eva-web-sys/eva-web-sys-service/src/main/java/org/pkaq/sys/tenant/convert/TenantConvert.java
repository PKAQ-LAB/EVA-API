package org.pkaq.sys.tenant.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.vo.TenantDetailVo;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface TenantConvert {

    TenantEntity boToEntity(TenantAoeBo bo);

    TenantDetailVo entityToVo(TenantEntity entity);
}
