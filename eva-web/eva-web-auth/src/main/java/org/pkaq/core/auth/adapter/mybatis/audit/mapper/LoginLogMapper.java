package org.pkaq.core.auth.adapter.mybatis.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.auth.adapter.mybatis.audit.entity.LoginLogEntity;

/**
 * 登录日志Mapper
 *
 * @author PKAQ
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLogEntity> {
}
