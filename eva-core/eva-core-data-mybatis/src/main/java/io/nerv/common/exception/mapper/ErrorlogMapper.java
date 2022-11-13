package io.nerv.common.exception.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.common.exception.entity.ErrorlogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author: S.PKAQ
 */
@Mapper
@Repository
public interface ErrorlogMapper extends BaseMapper<ErrorlogEntity> {
}
