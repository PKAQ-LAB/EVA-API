package org.pkaq.core.auth.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.auth.log.entity.LoginLogEntity;

/**
 * 登录日志Mapper
 *
 * @author PKAQ
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLogEntity> {
}
