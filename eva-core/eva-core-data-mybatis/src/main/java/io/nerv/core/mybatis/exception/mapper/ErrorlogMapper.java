package io.nerv.core.mybatis.exception.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.core.mybatis.exception.entity.ErrorlogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author: S.PKAQ
 */
@Mapper
@Repository
public interface ErrorlogMapper extends BaseMapper<ErrorlogEntity> {
}
