package org.pkaq.core.auth.openapi.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.auth.openapi.log.entity.OpenApiCallLogEntity;

/**
 * OpenAPI调用日志 Mapper。
 *
 * @author PKAQ
 */
@Mapper
public interface OpenApiCallLogMapper extends BaseMapper<OpenApiCallLogEntity> {
}
