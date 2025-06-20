package org.pkaq.sys.module.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.vo.ModuleDetailVo;

@Mapper(config = MapConvertConfig.class)
public interface ModuleConvert extends Convert {
    ModuleEntity aoeBoToEntity(ModuleAoeBo bo);

    ModuleDetailVo entityToDetailVo(ModuleEntity entity);
}
