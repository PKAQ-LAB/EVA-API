package org.pkaq.core.mongo.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.LogSupporter;
import org.pkaq.core.log.condition.MongoSupporterCondition;
import org.pkaq.core.mongo.log.entity.MongoBizLogEntity;
import org.pkaq.core.mongo.log.repository.MongoBizLogRepository;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.BeanUtils;
import org.pkaq.core.util.DatePatterns;
import org.pkaq.core.util.DateUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * MongoDB日志存储实现类
 * 提供基于MongoDB的完整日志增删查功能，支持分页、排序和租户隔离
 *
 * @author PKAQ
 * @date 2026-03-10
 */
@Slf4j
@Component
@Conditional(MongoSupporterCondition.class)
@RequiredArgsConstructor
public class MongoLogSupporter implements LogSupporter {

    private final MongoBizLogRepository repository;

    @Override
    public void save(BizLogEntity bizLogEntity) {
        MongoBizLogEntity mongoEntity = new MongoBizLogEntity();
        BeanUtils.copyProperties(bizLogEntity, mongoEntity);
        repository.save(mongoEntity);
    }

    @Override
    public BizLogEntity get(String id) {
        long tenantId = ThreadUserHelper.getTenantId();
        return repository.findByIdAndTenantId(id, tenantId).orElse(null);
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
    public PageVo<MongoBizLogEntity> list(DateRangeBo dateRangeBo, int pageNo, int pageSize) {
        long tenantId = ThreadUserHelper.getTenantId();
        String beginStr = DateUtils.format(dateRangeBo.getBegin(), DatePatterns.NORM_DATE_PATTERN);
        String endStr = DateUtils.format(dateRangeBo.getEnd(), DatePatterns.NORM_DATE_PATTERN);

        // 按操作时间降序排序 + 分页（PageRequest页码从0开始）
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "operateDatetime"));
        Page<MongoBizLogEntity> page = repository.findByTenantIdAndOperateDatetimeBetween(
                tenantId, beginStr, endStr, pageRequest);

        PageVo<MongoBizLogEntity> pageVo = new PageVo<>();
        pageVo.setRecords(page.getContent());
        pageVo.setTotal(page.getTotalElements());
        pageVo.setSize(pageSize);
        pageVo.setCurrent(pageNo);
        return pageVo;
    }

    @Override
    public List<MongoBizLogEntity> getLog() {
        long tenantId = ThreadUserHelper.getTenantId();
        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE,
                Sort.by(Sort.Direction.DESC, "operateDatetime"));
        return repository.findByTenantId(tenantId, pageRequest).getContent();
    }

    @Override
    public List<MongoBizLogEntity> getLogByType(String type) {
        long tenantId = ThreadUserHelper.getTenantId();
        return repository.findByTenantIdAndOperateType(tenantId, type);
    }

    @Override
    public List<MongoBizLogEntity> getLogAfter(Date dateTime) {
        long tenantId = ThreadUserHelper.getTenantId();
        String dateStr = DateUtils.format(dateTime, DatePatterns.NORM_DATETIME_PATTERN);
        return repository.findByTenantIdAndOperateDatetimeGreaterThanEqual(tenantId, dateStr);
    }

    @Override
    public List<MongoBizLogEntity> getLogBetween(Date begin, Date end) {
        long tenantId = ThreadUserHelper.getTenantId();
        String beginStr = DateUtils.format(begin, DatePatterns.NORM_DATETIME_PATTERN);
        String endStr = DateUtils.format(end, DatePatterns.NORM_DATETIME_PATTERN);
        return repository.findByTenantIdAndOperateDatetimeBetween(tenantId, beginStr, endStr);
    }

    @Override
    public void cleanAll() {
        long tenantId = ThreadUserHelper.getTenantId();
        repository.deleteByTenantId(tenantId);
    }

    @Override
    public void cleanBefore(Date dateTime) {
        long tenantId = ThreadUserHelper.getTenantId();
        String dateStr = DateUtils.format(dateTime, DatePatterns.NORM_DATETIME_PATTERN);
        repository.deleteByTenantIdAndOperateDatetimeLessThanEqual(tenantId, dateStr);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        long tenantId = ThreadUserHelper.getTenantId();
        String beginStr = DateUtils.format(begin, DatePatterns.NORM_DATETIME_PATTERN);
        String endStr = DateUtils.format(end, DatePatterns.NORM_DATETIME_PATTERN);
        repository.deleteByTenantIdAndOperateDatetimeBetween(tenantId, beginStr, endStr);
    }

    @Override
    public void print() {
        // MongoDB模式不支持控制台打印
    }
}
