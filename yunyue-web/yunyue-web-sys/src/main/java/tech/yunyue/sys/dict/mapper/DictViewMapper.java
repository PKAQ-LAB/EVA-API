package tech.yunyue.sys.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.yunyue.core.annotation.Ignore;
import tech.yunyue.sys.dict.entity.DictViewEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 字典视图view
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
@Ignore
public interface DictViewMapper extends BaseMapper<DictViewEntity> {

}
