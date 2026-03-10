package org.pkaq.core.mongo.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.LogSupporter;
import org.pkaq.core.log.condition.MongoSupporterCondition;
import org.pkaq.core.mongo.log.entity.MongoBizLogEntity;
import org.pkaq.core.mongo.log.repository.MongoBizLogRepository;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.util.BeanUtils;
import org.pkaq.core.util.DatePatterns;
import org.pkaq.core.util.DateUtils;
import org.pkaq.core.util.json.JsonUtil;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * MongoDB 日志存储实现类
 *
 * @author PKAQ
 * @date 2026-03-10
 */
@Slf4j
@Component
@Conditional(MongoSupporterCondition.class)
@RequiredArgsConstructor
public class MongoLogSupporter implements LogSupporter {

    private final MongoBizLogRepository mongoBizLogRepository;

    @Override
    public void save(BizLogEntity bizLogEntity) {
        MongoBizLogEntity mongoEntity = new MongoBizLogEntity();
        BeanUtils.copyProperties(bizLogEntity, mongoEntity);
        this.mongoBizLogRepository.save(mongoEntity);
    }

    @Override
    public String get(String id) {
        return this.mongoBizLogRepository.findById(id)
                .map(JsonUtil::toJson)
                .orElse("");
    }

    @Override
    public Object list(DateRangeBo dateRangeBo) {
        Date begin = dateRangeBo.getBegin();
        Date end = dateRangeBo.getEnd();
        if (null == begin) {
            begin = DateUtils.addDay(new Date(), -7);
        }
        if (null == end) {
            end = new Date();
        }

        String beginStr = DateUtils.format(begin, DatePatterns.NORM_DATETIME_PATTERN);
        String endStr = DateUtils.format(end, DatePatterns.NORM_DATETIME_PATTERN);

        PageRequest pageRequest = PageRequest.of(0, 30, Sort.by(Sort.Direction.DESC, "operateDatetime"));
        Page<MongoBizLogEntity> page = this.mongoBizLogRepository
                .findByOperateDatetimeBetweenOrderByOperateDatetimeDesc(beginStr, endStr, pageRequest);
        return page;
    }

    @Override
    public List<? extends BizLogEntity> getLog() {
        return this.mongoBizLogRepository.findAll();
    }

    @Override
    public List<? extends BizLogEntity> getLogByType(String type) {
        return this.mongoBizLogRepository.findByOperateType(type);
    }

    @Override
    public List<? extends BizLogEntity> getLogAfter(Date dateTime) {
        String dateStr = DateUtils.format(dateTime, DatePatterns.NORM_DATETIME_PATTERN);
        return this.mongoBizLogRepository.findByOperateDatetimeGreaterThanEqual(dateStr);
    }

    @Override
    public List<? extends BizLogEntity> getLogBetween(Date begin, Date end) {
        String beginStr = DateUtils.format(begin, DatePatterns.NORM_DATETIME_PATTERN);
        String endStr = DateUtils.format(end, DatePatterns.NORM_DATETIME_PATTERN);
        return this.mongoBizLogRepository.findByOperateDatetimeBetween(beginStr, endStr);
    }

    @Override
    public void cleanAll() {
        this.mongoBizLogRepository.deleteAll();
    }

    @Override
    public void cleanBefore(Date dateTime) {
        String dateStr = DateUtils.format(dateTime, DatePatterns.NORM_DATETIME_PATTERN);
        this.mongoBizLogRepository.deleteByOperateDatetimeLessThanEqual(dateStr);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        String beginStr = DateUtils.format(begin, DatePatterns.NORM_DATETIME_PATTERN);
        String endStr = DateUtils.format(end, DatePatterns.NORM_DATETIME_PATTERN);
        this.mongoBizLogRepository.deleteByOperateDatetimeBetween(beginStr, endStr);
    }

    @Override
    public void print() {
    }
}
