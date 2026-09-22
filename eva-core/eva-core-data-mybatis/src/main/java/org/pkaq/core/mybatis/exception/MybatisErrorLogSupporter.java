package org.pkaq.core.mybatis.exception;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.ErrorLogEntity;
import org.pkaq.core.log.base.ErrorLogSupporter;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.exception.entity.ErrorlogEntity;
import org.pkaq.core.mybatis.exception.mapper.ErrorlogMapper;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.BeanUtils;
import org.pkaq.core.util.DateUtils;
import org.pkaq.core.util.DatePatterns;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

/**
 * MyBatis 错误日志存储实现
 *
 * @author PKAQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MybatisErrorLogSupporter implements ErrorLogSupporter {

    private final ErrorlogMapper errorlogMapper;

    private final EvaConfig evaConfig;

    @Override
    public void save(ErrorLogEntity entity) {
        if (entity == null) {
            return;
        }
        try {
            var mybatisEntity = new ErrorlogEntity();
            BeanUtils.copyProperties(entity, mybatisEntity);
            errorlogMapper.insert(mybatisEntity);
        } catch (Exception exception) {
            // 错误日志持久化失败不得覆盖原始业务异常。
            log.error("保存错误日志失败, tenantId: {}, className: {}",
                    entity.getTenantId(), entity.getClassName(), exception);
        }
    }

    @Override
    public ErrorLogEntity get(String id) {
        QueryWrapper<ErrorlogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        applyTenantFilter(wrapper);
        return errorlogMapper.selectOne(wrapper);
    }

    @Override
    public PageVo<? extends ErrorLogEntity> list(DateRangeBo dateRangeBo, int pageNo, int pageSize) {
        QueryWrapper<ErrorlogEntity> wrapper = new QueryWrapper<>();

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
        wrapper.orderByDesc("request_time");

        applyTenantFilter(wrapper);

        PageResult<ErrorlogEntity> page = new PageResult<>(pageNo, pageSize);
        errorlogMapper.selectPage(page, wrapper);
        return page.map(Function.identity());
    }

    private void applyTenantFilter(QueryWrapper<ErrorlogEntity> wrapper) {
        if (this.evaConfig.isStandaloneMode()) {
            wrapper.eq("tenant_id", 0L);
            return;
        }
        ThreadUser currentUser = ThreadUserHelper.getCurrentUserOrNull();
        wrapper.eq("tenant_id", currentUser == null ? -1L : currentUser.getTenantId());
    }
}
