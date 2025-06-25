package org.pkaq.sys.module.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.bo.Bo;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.core.mvc.vo.Vo;
import org.pkaq.core.mybatis.mvc.entity.StdTreeEntity;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.module.vo.ModuleListVo;
import org.pkaq.sys.user.vo.UserResourceVo;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface ModuleConvert extends Convert<ModuleEntity> {

    List<ModuleListVo> entityToListVo(List<ModuleEntity> entity);

    ModuleListVo entityToListVo(ModuleEntity entity);

    ModuleEntity boToEntity(ModuleAoeBo bo);

    ModuleDetailVo entityToDetailVo(ModuleEntity entity);

    List<UserResourceVo> moduleTreeToUserResourceVo(List<StdTreeEntity> treeModule);
}
