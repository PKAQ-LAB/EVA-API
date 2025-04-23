package org.pkaq.sys.dict.covernt;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.entity.DictEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictConvert {

    DictAoeBo entityToBo(DictEntity dict);

    DictEntity boToEntity(DictAoeBo bo);

}
