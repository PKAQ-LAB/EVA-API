package io.nerv.web.sys.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.web.sys.dict.entity.DictViewEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * 字典视图view
 * @author: S.PKAQ
 * @Datetime: 2019/3/15 15:14
 */
@Mapper
@Repository
public interface DictViewMapper extends BaseMapper<DictViewEntity> {
    Map<String, Map<String, String>> queryForMap();
}
