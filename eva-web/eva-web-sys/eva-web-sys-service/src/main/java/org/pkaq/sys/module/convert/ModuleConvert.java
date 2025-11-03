package org.pkaq.sys.module.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.core.mybatis.mvc.entity.StdTreeEntity;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.bo.ModuleQueryBo;
import org.pkaq.sys.module.bo.ModuleResourcesBo;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.entity.ModuleResources;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.module.vo.ModuleListVo;
import org.pkaq.sys.module.vo.ModuleResourcesVo;
import org.pkaq.sys.user.vo.UserResourceVo;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface ModuleConvert  {

    List<ModuleListVo> entityToListVo(List<ModuleEntity> entity);

    ModuleListVo entityToListVo(ModuleEntity entity);

    ModuleEntity boToEntity(ModuleAoeBo bo);

    ModuleEntity boToEntity(ModuleQueryBo bo);

    ModuleDetailVo entityToDetailVo(ModuleEntity entity);

    List<UserResourceVo> moduleTreeToUserResourceVo(List<StdTreeEntity> treeModule);

    List<ModuleResources> resourceBoToEntity(List<ModuleResourcesBo> resourceBo);

    List<ModuleResourcesVo> resourceEntityToVo(List<ModuleResources> resources);
}
