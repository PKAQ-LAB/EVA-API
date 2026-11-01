package org.pkaq.core.auth.adapter.mybatis.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.pkaq.core.auth.audit.bo.LoginLogQueryBo;
import org.pkaq.core.auth.adapter.mybatis.audit.entity.LoginLogEntity;

/**
 * 登录日志Mapper
 *
 * @author PKAQ
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLogEntity> {
    /** 固定授权范围内的历史详情。 */
    @SelectProvider(type = LoginLogSqlProvider.class, method = "detail")
    LoginLogEntity selectHistoryDetail(@Param("id") Long id);

    /** 固定授权范围内的历史分页及同范围总数。 */
    @SelectProvider(type = LoginLogSqlProvider.class, method = "page")
    IPage<LoginLogEntity> selectHistoryPage(IPage<LoginLogEntity> page, @Param("query") LoginLogQueryBo query);

    /** 系统风险识别的设备计数。 */
    @SelectProvider(type = LoginLogSqlProvider.class, method = "riskCount")
    Long selectRiskDeviceCount(@Param("tenantId") Long tenantId, @Param("userId") Long userId,
                              @Param("fingerprint") String fingerprint);

    /** 系统风险识别的最近登录。 */
    @SelectProvider(type = LoginLogSqlProvider.class, method = "riskLast")
    LoginLogEntity selectRiskLastLogin(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
