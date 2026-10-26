package org.pkaq.sys.tenant.pkg.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.pkaq.core.enums.BaseEnum;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.tenant.pkg.bo.TenantPackageAoeBo;
import org.pkaq.sys.tenant.pkg.entity.TenantPackageEntity;
import org.pkaq.sys.tenant.pkg.vo.TenantPackageVo;

/**
 * 租户套餐对象转换器。
 *
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class, imports = {BaseEnum.class, FrozenEnumm.class})
public interface TenantPackageConvert {

    /**
     * 将新增编辑参数转换为实体。
     *
     * @param bo 新增编辑参数
     * @return 租户套餐实体
     */
    @Mapping(target = "frozen", expression = "java(BaseEnum.of(FrozenEnumm.class, bo == null ? null : bo.getFrozen()))")
    TenantPackageEntity boToEntity(TenantPackageAoeBo bo);

    /**
     * 将实体转换为视图对象。
     *
     * @param entity 租户套餐实体
     * @return 租户套餐视图对象
     */
    TenantPackageVo entityToVo(TenantPackageEntity entity);
}
