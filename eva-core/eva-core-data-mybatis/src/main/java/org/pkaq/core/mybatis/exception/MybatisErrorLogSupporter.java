package org.pkaq.core.mybatis.exception;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.ErrorLogEntity;
import org.pkaq.core.log.base.ErrorLogSupporter;
import org.pkaq.core.log.condition.ErrorlogSupporterCondition;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.exception.entity.ErrorlogEntity;
import org.pkaq.core.mybatis.exception.mapper.ErrorlogMapper;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.BeanUtils;
import org.pkaq.core.util.DateUtils;
import org.springframework.context.annotation.Conditional;
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
@Conditional(ErrorlogSupporterCondition.class)
public class MybatisErrorLogSupporter implements ErrorLogSupporter {

    private final ErrorlogMapper errorlogMapper;

    @Override
    public void save(ErrorLogEntity entity) {
        var mybatisEntity = new ErrorlogEntity();
        BeanUtils.copyProperties(entity, mybatisEntity);
        errorlogMapper.insert(mybatisEntity);
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

        wrapper.ge("request_time", begin);
        wrapper.le("request_time", end);
        wrapper.orderByDesc("request_time");

        applyTenantFilter(wrapper);

        PageResult<ErrorlogEntity> page = new PageResult<>(pageNo, pageSize);
        errorlogMapper.selectPage(page, wrapper);
        return page.map(Function.identity());
    }

    private void applyTenantFilter(QueryWrapper<ErrorlogEntity> wrapper) {
        try {
            wrapper.eq("tenant_id", ThreadUserHelper.getTenantId());
        } catch (Exception ignored) {
            // 未登录场景不做租户过滤
        }
    }
}
