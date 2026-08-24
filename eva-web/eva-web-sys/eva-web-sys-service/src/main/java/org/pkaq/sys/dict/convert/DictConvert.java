package org.pkaq.sys.dict.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.bo.DictLineBo;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.vo.DictLineVo;
import org.pkaq.sys.dict.vo.DictViewVo;

import java.util.List;

/**
 * 字典对象转换器。
 *
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface DictConvert {

    /**
     * 将字典编辑参数转换为字典实体。
     *
     * @param bo 字典编辑参数
     * @return 字典实体
     */
    @Mapping(target = "frozen", source = "frozen", qualifiedByName = "codeToFrozen")
    DictEntity boToEntity(DictAoeBo bo);

    /**
     * 将字典明细编辑参数转换为字典明细实体。
     *
     * @param bo 字典明细编辑参数
     * @return 字典明细实体
     */
    @Mapping(target = "DCode", source = "keyName")
    @Mapping(target = "DValue", source = "keyValue")
    @Mapping(target = "sort", source = "orders")
    @Mapping(target = "frozen", source = "frozen", qualifiedByName = "codeToFrozen")
    DictItemEntity lineBoToEntity(DictLineBo bo);

    /**
     * 将字典实体转换为字典视图对象。
     *
     * @param entity 字典实体
     * @return 字典视图对象
     */
    @Mapping(target = "frozen", source = "frozen", qualifiedByName = "frozenToCode")
    @Mapping(target = "lines", ignore = true)
    DictViewVo entityToVo(DictEntity entity);

    /**
     * 将字典明细实体转换为字典明细视图对象。
     *
     * @param entity 字典明细实体
     * @return 字典明细视图对象
     */
    @Mapping(target = "keyName", source = "DCode")
    @Mapping(target = "keyValue", source = "DValue")
    @Mapping(target = "orders", source = "sort")
    @Mapping(target = "frozen", source = "frozen", qualifiedByName = "frozenToCode")
    DictLineVo lineEntityToVo(DictItemEntity entity);

    /**
     * 批量转换字典明细实体。
     *
     * @param entities 字典明细实体
     * @return 字典明细视图对象
     */
    List<DictLineVo> lineEntityToVo(List<DictItemEntity> entities);

    /**
     * 将锁定状态编码转换为枚举。
     *
     * @param code 锁定状态编码
     * @return 锁定状态枚举
     */
    @Named("codeToFrozen")
    default FrozenEnumm codeToFrozen(Integer code) {
        if (FrozenEnumm.FROZEN.getCode().equals(code)) {
            return FrozenEnumm.FROZEN;
        }
        if (FrozenEnumm.READ_ONLY.getCode().equals(code)) {
            return FrozenEnumm.READ_ONLY;
        }
        return FrozenEnumm.UN_FROZEN;
    }

    /**
     * 将锁定状态枚举转换为编码。
     *
     * @param frozen 锁定状态枚举
     * @return 锁定状态编码
     */
    @Named("frozenToCode")
    default Integer frozenToCode(FrozenEnumm frozen) {
        return frozen == null ? null : frozen.getCode();
    }
}
