package org.pkaq.sys.role.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.pkaq.core.enums.BaseEnum;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.sys.role.bo.RoleQueryBo;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.vo.RoleDetailVo;
import org.pkaq.sys.role.vo.RoleGrantedModuleVo;
import org.pkaq.sys.role.vo.RoleGrantedUserVo;
import org.pkaq.sys.role.vo.RoleListVo;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.user.vo.UserSimpleVo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author PKAQ
 */
@Mapper(componentModel = "spring", config = MapConvertConfig.class, imports = {BaseEnum.class, FrozenEnumm.class})
public abstract class RoleConvert extends Convert {

    public abstract RoleEntity fromBo(RoleQueryBo queryBo);

    @Mapping(target = "dataPostIds", expression = "java(postIds(roleEntity == null ? null : "
            + "roleEntity.getDataPostIds()))")
    public abstract RoleDetailVo toVo(RoleEntity roleEntity);

    @Mapping(target = "dataPostIds", expression = "java(postIds(roleEntity == null ? null : "
            + "roleEntity.getDataPostIds()))")
    public abstract RoleListVo toListItemVo(RoleEntity roleEntity);

    public abstract List<RoleListVo> toListVo(List<RoleEntity> roleEntities);

    @Mapping(target = "frozen", expression = "java(BaseEnum.of(FrozenEnumm.class, "
            + "roleAoeBo == null ? null : roleAoeBo.getFrozen()))")
    @Mapping(target = "dataPostIds", expression = "java(postCsv(roleAoeBo == null ? null : "
            + "roleAoeBo.getDataPostIds()))")
    public abstract RoleEntity fromBo(RoleAoeBo roleAoeBo);

    /** 岗位接口数组显式转换为规范存储值，省略保持NULL。 */
    protected String postCsv(List<Long> ids) {
        return ids == null ? null : ids.stream().sorted().map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
    }

    /** 历史空配置回显空集合，非法配置不得回显为有效候选。 */
    protected List<Long> postIds(String value) {
        if (value == null || !value.matches("[1-9][0-9]*(,[1-9][0-9]*)*")) {
            return List.of();
        }
        try {
            List<Long> ids = java.util.Arrays.stream(value.split(",", -1)).map(Long::valueOf).toList();
            return ids.stream().allMatch(id -> id > 0L) && ids.stream().distinct().count() == ids.size()
                    ? ids : List.of();
        } catch (NumberFormatException exception) {
            return List.of();
        }
    }

    public abstract List<RoleGrantedModuleVo> entityToModuleList(List<ModuleEntity> entities);

    public abstract RoleGrantedModuleVo toGrantedModuleVo(Collection<ModuleDetailVo> modules,
                                                           Set<Long> checkedModuleIds);

    public abstract RoleGrantedUserVo toGrantedUserVo(List<UserSimpleVo> users, Set<Long> checkedUser);

}
