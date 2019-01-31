package io.nerv.core.bizlog.supporter.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.core.bizlog.supporter.mybatis.entity.MybatisBizLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 8:40
 */
@Mapper
@Repository
public interface MybatisSupporterMapper extends BaseMapper<MybatisBizLogEntity> {
}
