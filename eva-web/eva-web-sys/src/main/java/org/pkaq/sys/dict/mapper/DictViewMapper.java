package org.pkaq.sys.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.pkaq.sys.dict.entity.DictViewEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 字典视图view
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
public interface DictViewMapper extends BaseMapper<DictViewEntity> {

}
