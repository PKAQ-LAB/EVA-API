package org.pkaq.core.mybatis.exception;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.ErrorIncident;
import org.pkaq.core.log.base.ErrorIncidentRepository;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.exception.entity.ErrorIncidentEntity;
import org.pkaq.core.mybatis.exception.mapper.ErrorIncidentMapper;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.BeanUtils;
import org.pkaq.core.util.DateUtils;
import org.pkaq.core.util.DatePatterns;
import org.springframework.stereotype.Component;
import org.pkaq.core.util.Snowflake;

import java.util.Date;
import java.util.function.Function;

/**
 * PostgreSQL 错误事件存储实现。
 *
 * @author PKAQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresErrorIncidentRepository implements ErrorIncidentRepository {

    private final ErrorIncidentMapper errorIncidentMapper;

    private final EvaConfig evaConfig;
    private final Snowflake snowflake = new Snowflake();

    @Override
    public void save(ErrorIncident entity) {
        if (entity == null) {
            return;
        }
        try {
            ErrorIncidentEntity postgresEntity = new ErrorIncidentEntity();
            BeanUtils.copyProperties(entity, postgresEntity);
            postgresEntity.setId(this.snowflake.nextId());
            this.errorIncidentMapper.upsert(postgresEntity);
        } catch (Exception exception) {
            // 错误日志持久化失败不得覆盖原始业务异常。
            log.error("保存错误日志失败, tenantId: {}, className: {}",
                    entity.getTenantId(), entity.getClassName(), exception);
        }
    }

    @Override
    public ErrorIncident get(String id) {
        QueryWrapper<ErrorIncidentEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        applyTenantFilter(wrapper);
        return this.errorIncidentMapper.selectOne(wrapper);
    }

    @Override
    public PageVo<? extends ErrorIncident> list(DateRangeBo dateRangeBo, int pageNo, int pageSize) {
        QueryWrapper<ErrorIncidentEntity> wrapper = new QueryWrapper<>();

        Date begin = dateRangeBo.getBegin();
        Date end = dateRangeBo.getEnd();

        if (begin == null) {
            begin = DateUtils.addDay(new Date(), -7);
        }
        if (end == null) {
            end = new Date();
        }

        wrapper.ge("request_time", DateUtils.format(begin, DatePatterns.NORM_DATETIME_PATTERN));
        wrapper.le("request_time", DateUtils.format(end, DatePatterns.NORM_DATETIME_PATTERN));
        wrapper.orderByDesc("last_occurred_at");

        applyTenantFilter(wrapper);

        PageResult<ErrorIncidentEntity> page = new PageResult<>(pageNo, pageSize);
        this.errorIncidentMapper.selectPage(page, wrapper);
        return page.map(Function.identity());
    }

    private void applyTenantFilter(QueryWrapper<ErrorIncidentEntity> wrapper) {
        if (this.evaConfig.isStandaloneMode()) {
            wrapper.eq("tenant_id", 0L);
            return;
        }
        ThreadUser currentUser = ThreadUserHelper.getCurrentUserOrNull();
        wrapper.eq("tenant_id", currentUser == null ? -1L : currentUser.getTenantId());
    }
}
