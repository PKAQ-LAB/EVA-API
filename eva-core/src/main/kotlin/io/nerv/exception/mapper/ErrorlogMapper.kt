package io.nerv.exception.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import io.nerv.exception.entity.ErrorlogEntity
import org.apache.ibatis.annotations.Mapper
import org.springframework.stereotype.Repository

/**
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 8:40
 */
@Mapper
@Repository
interface ErrorlogMapper : BaseMapper<ErrorlogEntity?> 