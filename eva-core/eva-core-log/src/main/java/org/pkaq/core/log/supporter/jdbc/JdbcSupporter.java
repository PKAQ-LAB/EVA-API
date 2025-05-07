package org.pkaq.core.log.supporter.jdbc;

import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.BizLogSupporter;
import org.pkaq.core.log.constant.LogConstant;
import org.pkaq.core.log.events.BizLogEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 基于数据库的日志持久话类
 *
 * @author PKAQ
 */
public class JdbcSupporter implements BizLogSupporter {
    private BizLogEntity bizLogEntity;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public JdbcSupporter(BizLogEntity bizLogEntity) {
        this.bizLogEntity = bizLogEntity;
    }

    @Override
    public void save(BizLogEntity entity) {
        String sql = "";
        this.jdbcTemplate.execute(sql);
    }

    /**
     * 日志保存
     *
     * @param event
     */
    @Async
    @EventListener(value = BizLogEvent.class)
    public void listener(BizLogEvent event) {
        Map<String, Object> source = (Map<String, Object>) event.getSource();
        BizLogEntity errorEntity = (BizLogEntity) source.get(LogConstant.EVENT_LOG);
        this.save(errorEntity);
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
        // TODO document why this method is empty
    }

    @Override
    public void cleanBefore(Date dateTime) {
        // TODO document why this method is empty
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        // TODO document why this method is empty
    }

    @Override
    public void print() {
        // TODO document why this method is empty
    }
}
