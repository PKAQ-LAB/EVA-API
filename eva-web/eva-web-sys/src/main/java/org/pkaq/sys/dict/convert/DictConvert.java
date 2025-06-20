package org.pkaq.sys.dict.convert;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.bo.DictAoeLineBo;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.vo.DictViewVo;

import java.util.List;

@Mapper(config = MapConvertConfig.class)
public interface DictConvert {

    DictViewVo entityToVo(DictEntity dict);

    List<DictViewVo> toVoList(List<DictEntity> list);

    DictEntity boToEntity(DictAoeBo bo);

    DictItemEntity boToItemEntity(DictAoeLineBo bo);

}
