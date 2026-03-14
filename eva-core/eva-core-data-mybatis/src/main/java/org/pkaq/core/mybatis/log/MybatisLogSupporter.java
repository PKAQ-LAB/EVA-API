package org.pkaq.core.mybatis.log;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.LogSupporter;
import org.pkaq.core.log.condition.MybatisSupporterCondition;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.log.entity.MybatisBizLogEntity;
import org.pkaq.core.mybatis.log.mapper.MybatisSupporterMapper;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.BeanUtils;
import org.pkaq.core.util.DatePatterns;
import org.pkaq.core.util.DateUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * mybatis 日志存储实现类
 * 提供基于mybatis-plus的完整日志增删查功能，支持租户隔离
 *
 * @author PKAQ
 */
@Slf4j
@Component
@Conditional(MybatisSupporterCondition.class)
@RequiredArgsConstructor
public class MybatisLogSupporter implements LogSupporter {

    private final MybatisSupporterMapper mybatisSupporterMapper;

    /**
     * 为QueryWrapper添加租户隔离条件
     *
     * @param wrapper 查询条件
     */
    private void applyTenantFilter(QueryWrapper<MybatisBizLogEntity> wrapper) {
        wrapper.eq("tenant_id", ThreadUserHelper.getTenantId());
    }

    @Override
    public void save(BizLogEntity bizLogEntity) {
        MybatisBizLogEntity mybatisBizLogEntity = new MybatisBizLogEntity();
        BeanUtils.copyProperties(bizLogEntity, mybatisBizLogEntity);
        this.mybatisSupporterMapper.insert(mybatisBizLogEntity);
    }

    @Override
    public BizLogEntity get(String id) {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("id", Long.valueOf(id));
        applyTenantFilter(wrapper);
        return this.mybatisSupporterMapper.selectOne(wrapper);
    }

    /**
     * 分页查询日志列表
     * 按操作时间降序排列，支持日期范围过滤、分页和租户隔离
     *
     * @param dateRangeBo 日期范围
     * @param pageNo      页码
     * @param pageSize    每页条数
     * @return 分页结果
     */
    @Override
    public PageVo<MybatisBizLogEntity> list(DateRangeBo dateRangeBo, int pageNo, int pageSize) {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        applyTenantFilter(wrapper);

        String beginStr = DateUtils.format(dateRangeBo.getBegin(), DatePatterns.NORM_DATE_PATTERN);
        String endStr = DateUtils.format(dateRangeBo.getEnd(), DatePatterns.NORM_DATE_PATTERN);

        // 日期范围过滤
        wrapper.ge("operate_datetime", beginStr);
        wrapper.le("operate_datetime", endStr);
        // 按操作时间降序排列
        wrapper.orderByDesc("operate_datetime");

        PageResult<MybatisBizLogEntity> page = new PageResult<>(pageNo, pageSize);
        mybatisSupporterMapper.selectPage(page, wrapper);

        return page.map(Function.identity());
    }

    @Override
    public List<? extends BizLogEntity> getLog() {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        applyTenantFilter(wrapper);
        wrapper.orderByDesc("operate_datetime");
        return this.mybatisSupporterMapper.selectList(wrapper);
    }

    @Override
    public List<? extends BizLogEntity> getLogByType(String type) {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        applyTenantFilter(wrapper);
        wrapper.eq("operate_type", type);
        wrapper.orderByDesc("operate_datetime");
        return this.mybatisSupporterMapper.selectList(wrapper);
    }

    @Override
    public List<? extends BizLogEntity> getLogAfter(Date dateTime) {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        applyTenantFilter(wrapper);
        wrapper.ge("operate_datetime", dateTime);
        wrapper.orderByDesc("operate_datetime");
        return this.mybatisSupporterMapper.selectList(wrapper);
    }

    @Override
    public List<? extends BizLogEntity> getLogBetween(Date begin, Date end) {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        applyTenantFilter(wrapper);
        wrapper.between("operate_datetime", begin, end);
        wrapper.orderByDesc("operate_datetime");
        return this.mybatisSupporterMapper.selectList(wrapper);
    }

    @Override
    public void cleanAll() {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        applyTenantFilter(wrapper);
        this.mybatisSupporterMapper.delete(wrapper);
    }

    @Override
    public void cleanBefore(Date dateTime) {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        applyTenantFilter(wrapper);
        wrapper.le("operate_datetime", dateTime);
        this.mybatisSupporterMapper.delete(wrapper);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        applyTenantFilter(wrapper);
        wrapper.between("operate_datetime", begin, end);
        this.mybatisSupporterMapper.delete(wrapper);
    }

    @Override
    public void print() {
        // mybatis模式不支持控制台打印
    }
}
