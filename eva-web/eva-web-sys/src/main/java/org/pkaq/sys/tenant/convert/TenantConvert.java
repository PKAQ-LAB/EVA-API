package org.pkaq.sys.tenant.convert;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.bo.TenantStatusBo;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.vo.TenantDetailVo;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface TenantConvert {

    TenantEntity boToEntity(TenantAoeBo bo);

    TenantDetailVo entityToVo(TenantEntity entity);

    TenantEntity boToEntity(TenantStatusBo bo);
}
