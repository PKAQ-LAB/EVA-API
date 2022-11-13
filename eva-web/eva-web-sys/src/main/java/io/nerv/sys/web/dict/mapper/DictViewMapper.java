package io.nerv.sys.web.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.sys.web.dict.entity.DictViewEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 字典视图view
 * @author: S.PKAQ
 */
@Mapper
@Repository
public interface DictViewMapper extends BaseMapper<DictViewEntity> {

}
