package org.pkaq.sys.tenant.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.pkaq.core.enums.BaseEnum;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.vo.TenantDetailVo;
import org.pkaq.sys.tenant.vo.TenantListVo;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class, imports = {BaseEnum.class, FrozenEnumm.class})
public interface TenantConvert {

    @Mapping(target = "frozen", expression = "java(BaseEnum.of(FrozenEnumm.class, bo == null ? null : bo.getFrozen()))")
    TenantEntity boToEntity(TenantAoeBo bo);

    TenantDetailVo entityToVo(TenantEntity entity);

    TenantListVo entityToListVo(TenantEntity entity);
}
