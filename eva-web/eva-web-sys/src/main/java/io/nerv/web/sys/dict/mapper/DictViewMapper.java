package io.nerv.web.sys.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.web.sys.dict.entity.DictViewEntity;
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
