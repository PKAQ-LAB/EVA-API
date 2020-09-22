package io.nerv.core.exception.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.core.exception.entity.ErrorlogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 8:40
 */
@Mapper
@Repository
public interface ErrorlogMapper extends BaseMapper<ErrorlogEntity> {
}
