package org.pkaq.web.sys.dict.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.web.sys.dict.entity.DictEntity;
import org.springframework.stereotype.Repository;

/**
 * 字典管理mapper
 * @author S.PKAQ
 */
@Mapper
@Repository
public interface DictMapper extends BaseMapper<DictEntity>{

}