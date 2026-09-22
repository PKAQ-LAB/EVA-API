package org.pkaq.core.mybatis.exception.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.pkaq.core.mybatis.exception.entity.ErrorIncidentEntity;
import org.springframework.stereotype.Repository;

/**
 * @author PKAQ
 */
@Mapper
@Repository
public interface ErrorIncidentMapper extends BaseMapper<ErrorIncidentEntity> {

    /**
     * 按租户和指纹新增或聚合错误事件。
     *
     * @param incident 错误事件
     * @return 影响行数
     */
    @Insert("""
            INSERT INTO LOG_ERROR (
                ID, REQUEST_TIME, IP, CLASS_NAME, METHOD, PARAMS, LOGIN_USER, TENANT_ID, SPEND_TIME,
                FINGERPRINT, EXCEPTION_TYPE, SUMMARY, TRACE_ID, FIRST_OCCURRED_AT,
                LAST_OCCURRED_AT, OCCURRENCE_COUNT, STATUS
            ) VALUES (
                #{id}, #{requestTime}, #{ip}, #{className}, #{method}, #{params}, #{loginUser}, #{tenantId},
                #{spendTime}, #{fingerprint}, #{exceptionType}, #{summary}, #{traceId}, #{firstOccurredAt},
                #{lastOccurredAt}, #{occurrenceCount}, #{status}
            )
            ON CONFLICT (TENANT_ID, FINGERPRINT) DO UPDATE SET
                REQUEST_TIME = EXCLUDED.REQUEST_TIME,
                IP = EXCLUDED.IP,
                CLASS_NAME = EXCLUDED.CLASS_NAME,
                METHOD = EXCLUDED.METHOD,
                PARAMS = EXCLUDED.PARAMS,
                LOGIN_USER = EXCLUDED.LOGIN_USER,
                TRACE_ID = EXCLUDED.TRACE_ID,
                LAST_OCCURRED_AT = EXCLUDED.LAST_OCCURRED_AT,
                OCCURRENCE_COUNT = LOG_ERROR.OCCURRENCE_COUNT + 1,
                STATUS = 'OPEN'
            """)
    int upsert(ErrorIncidentEntity incident);
}
