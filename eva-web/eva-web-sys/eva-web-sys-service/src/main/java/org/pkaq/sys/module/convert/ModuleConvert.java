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
import org.pkaq.sys.module.vo.ModuleMenuResourceVo;
import org.pkaq.sys.module.vo.ModuleMenuVo;
import org.pkaq.sys.module.vo.ModuleResourcesVo;
import org.pkaq.sys.user.vo.UserResourceVo;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface ModuleConvert {

    List<ModuleListVo> entityToListVo(List<ModuleEntity> entity);

    ModuleListVo entityToListVo(ModuleEntity entity);

    ModuleEntity boToEntity(ModuleAoeBo bo);

    ModuleEntity boToEntity(ModuleQueryBo bo);

    ModuleDetailVo entityToDetailVo(ModuleEntity entity);

    List<ModuleDetailVo> entityToDetailVo(List<ModuleEntity> entities);

    List<UserResourceVo> moduleTreeToUserResourceVo(List<StdTreeEntity> treeModule);

    List<ModuleResources> resourceBoToEntity(List<ModuleResourcesBo> resourceBo);

    List<ModuleResourcesVo> resourceEntityToVo(List<ModuleResources> resources);

    /**
     * 将模块详情树转换为前端菜单树。
     *
     * @param module 模块详情
     * @return 前端菜单
     */
    default ModuleMenuVo detailToMenuVo(ModuleDetailVo module) {
        if (module == null) {
            return null;
        }
        ModuleMenuVo vo = new ModuleMenuVo();
        vo.setId(module.getId());
        vo.setCode(module.getCode());
        vo.setName(module.getName());
        vo.setPid(module.getPid());
        vo.setPath(module.getPath());
        vo.setIsleaf(module.getIsleaf());
        vo.setIcon(module.getIcon());
        vo.setRouteUrl(module.getRouteUrl());
        vo.setComponentUrl(module.getComponentUrl());
        vo.setSort(module.getSort());
        vo.setResources(resourcesToMenuVo(module.getResources()));
        if (module.getOriginChildren() != null) {
            vo.setChildren(module.getOriginChildren().stream()
                    .filter(ModuleDetailVo.class::isInstance)
                    .map(ModuleDetailVo.class::cast)
                    .map(this::detailToMenuVo)
                    .collect(Collectors.toList()));
        }
        return vo;
    }

    /**
     * 将模块详情树转换为用户资源树。
     *
     * @param module 模块详情
     * @return 用户资源
     */
    default UserResourceVo detailToUserResourceVo(ModuleDetailVo module) {
        if (module == null) {
            return null;
        }
        UserResourceVo vo = new UserResourceVo();
        vo.setId(module.getId());
        vo.setPid(module.getPid());
        vo.setRouteurl(module.getRouteUrl());
        vo.setModelurl(module.getComponentUrl());
        vo.setResources(module.getResources());
        List<UserResourceVo> children = module.getOriginChildren() == null
                ? Collections.emptyList()
                : module.getOriginChildren().stream()
                        .filter(ModuleDetailVo.class::isInstance)
                        .map(ModuleDetailVo.class::cast)
                        .map(this::detailToUserResourceVo)
                        .collect(Collectors.toList());
        if (!children.isEmpty()) {
            vo.setChildren(children);
        }
        return vo;
    }

    /**
     * 将模块资源转换为菜单资源。
     *
     * @param resources 模块资源
     * @return 菜单资源
     */
    default List<ModuleMenuResourceVo> resourcesToMenuVo(List<ModuleResourcesVo> resources) {
        if (resources == null) {
            return Collections.emptyList();
        }
        return resources.stream().map(resource -> {
            ModuleMenuResourceVo vo = new ModuleMenuResourceVo();
            vo.setId(resource.getId());
            vo.setCode(resource.getCode());
            vo.setResourceDesc(resource.getResourceDesc());
            vo.setResourceUrl(resource.getResourceUrl());
            vo.setResourceType(resource.getResourceType());
            return vo;
        }).collect(Collectors.toList());
    }
}
