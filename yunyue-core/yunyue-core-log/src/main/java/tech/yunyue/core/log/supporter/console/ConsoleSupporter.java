package tech.yunyue.core.log.supporter.console;

import tech.yunyue.core.log.base.LogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import lombok.extern.slf4j.Slf4j;
import tech.yunyue.core.log.events.LogEvent;

import java.util.Date;
import java.util.List;

/**
 * 控制台日志实现类
 *
 * @author PKAQ
 */
@Slf4j
public class ConsoleSupporter<T extends LogEntity, E extends LogEvent<T>> implements LogSupporter<T,E>{
    private T t;

    @Override
    public void save(T t) {
        this.t = t;
        this.print();
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
        log.info("log: " + this.t.toString());
    }
}
