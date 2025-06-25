package org.pkaq.sys.dict.convert;

import org.mapstruct.Mapper;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.bo.DictAoeLineBo;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.vo.DictViewVo;

import java.util.List;

/**
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface DictConvert extends Convert<DictEntity> {

    DictViewVo entityToVo(DictEntity dict);

    List<DictViewVo> toVoList(List<DictEntity> list);

    DictEntity boToEntity(DictAoeBo bo);

    DictItemEntity boToItemEntity(DictAoeLineBo bo);

}
