package org.pkaq.sys.tenant.convert;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.pkaq.sys.tenant.bo.TenantEditBo;
import org.pkaq.sys.tenant.bo.TenantStatusBo;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.vo.TenantDetailVo;

/**
 * @author PKAQ
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantConvert {

    TenantEntity boToEntity(TenantEditBo bo);

    TenantDetailVo entityToVo(TenantEntity entity);

    TenantEntity boToEntity(TenantStatusBo bo);
}
