package io.nerv.core.mybatis.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.core.mybatis.log.entity.MybatisBizLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author: S.PKAQ
 */
@Mapper
@Repository
public interface MybatisSupporterMapper extends BaseMapper<MybatisBizLogEntity> {
}
