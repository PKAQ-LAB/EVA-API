package tech.yunyue.core.log.supporter.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.ErrorLogSupporter;

import java.util.Date;
import java.util.List;

/**
 * 基于数据库的错误日志持久话类
 *
 * @author PKAQ
 */
public class JdbcErrorLogSupporter implements ErrorLogSupporter {
    private ErrorlogEntity errorlogEntity;

    public JdbcErrorLogSupporter(ErrorlogEntity ErrorlogEntity) {
        this.errorlogEntity = ErrorlogEntity;
    }
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void save(ErrorlogEntity entity) {
        String sql = "";
        this.jdbcTemplate.execute(sql);
    }

    @Override
    public List<ErrorlogEntity> getLog() {
        return null;
    }

    @Override
    public List<ErrorlogEntity> getLogAfter(Date dateTime) {
        return null;
    }

    @Override
    public List<ErrorlogEntity> getLogBetween(Date begin, Date end) {
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
