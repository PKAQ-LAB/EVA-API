package tech.yunyue.core.log.supporter.jdbc;

import tech.yunyue.core.log.base.LogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.yunyue.core.log.events.LogEvent;

import java.util.Date;
import java.util.List;

/**
 * 基于数据库的日志持久话类
 *
 * @author PKAQ
 */
public class JdbcSupporter<T extends LogEntity, E extends LogEvent<T>> implements LogSupporter<T,E> {
    private T bizLogEntity;

    public JdbcSupporter(T bizLogEntity) {
        this.bizLogEntity = bizLogEntity;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void save(T t) {
        String sql = "";
        this.jdbcTemplate.execute(sql);
    }

    @Override
    public List<T> getLog() {
        return null;
    }

    @Override
    public List<T> getLogByType(String type) {
        return null;
    }

    @Override
    public List<T> getLogAfter(Date dateTime) {
        return null;
    }

    @Override
    public List<T> getLogBetween(Date begin, Date end) {
        return null;
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
