package org.pkaq.core.mybatis.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.mybatis.log.entity.MybatisBizLogEntity;
import org.springframework.stereotype.Repository;

/**
 * @author PKAQ
 */
@Mapper
@Repository
public interface MybatisSupporterMapper extends BaseMapper<MybatisBizLogEntity> {
}
