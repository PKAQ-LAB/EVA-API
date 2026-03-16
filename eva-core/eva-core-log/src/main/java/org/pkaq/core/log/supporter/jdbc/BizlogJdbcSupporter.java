package org.pkaq.core.log.supporter.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.LogSupporter;
import org.pkaq.core.log.condition.JdbcSupporterCondition;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 鍩轰簬JDBC鐨勬棩蹇楁寔涔呭寲绫?
 *
 * @author PKAQ
 */
@Slf4j
@Component
@Conditional(JdbcSupporterCondition.class)
@RequiredArgsConstructor
public class BizlogJdbcSupporter implements LogSupporter {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(BizLogEntity entity) {
        // TODO 瀹炵幇JDBC鏃ュ織淇濆瓨
        log.info(entity.toString());
    }

    @Override
    public String get(String id) {
        return "";
    }

    @Override
    public Object list(DateRangeBo dateRangeBo) {
        return Collections.emptyList();
    }

    @Override
    public List<BizLogEntity> getLog() {
        return Collections.emptyList();
    }

    @Override
    public List<BizLogEntity> getLogByType(String type) {
        return Collections.emptyList();
    }

    @Override
    public List<BizLogEntity> getLogAfter(Date dateTime) {
        return Collections.emptyList();
    }

    @Override
    public List<BizLogEntity> getLogBetween(Date begin, Date end) {
        return Collections.emptyList();
    }

    @Override
    public void cleanAll() {
    }

    @Override
    public void cleanBefore(Date dateTime) {
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
    }

    @Override
    public void print() {
    }
}

