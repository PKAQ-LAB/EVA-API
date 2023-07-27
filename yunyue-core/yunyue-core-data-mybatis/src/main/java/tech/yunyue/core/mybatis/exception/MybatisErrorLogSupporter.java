package tech.yunyue.core.mybatis.exception;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.ErrorLogSupporter;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.mybatis.exception.entity.MybatisErrorlogEntity;
import tech.yunyue.core.mybatis.exception.mapper.ErrorlogMapper;

import java.util.Date;
import java.util.List;

/**
 * mybatis 存储实现错误日志实现类
 */
@Slf4j
@Component
@ConditionalOnExpression("'mybatis'.equals('${eva.errorlog.impl}')")
@RequiredArgsConstructor
public class MybatisErrorLogSupporter implements ErrorLogSupporter {

    private final ErrorlogMapper errorlogMapper;

    @Override
    public void save(ErrorlogEntity errorlogEntity) {
        MybatisErrorlogEntity mybatisErrorlog = new MybatisErrorlogEntity();
        BeanUtil.copyProperties(errorlogEntity, mybatisErrorlog);

        this.errorlogMapper.insert(mybatisErrorlog);
    }

    @Override
    public List<? extends ErrorlogEntity> getLog() {
        return this.errorlogMapper.selectList(null);
    }

    @Override
    public List<? extends ErrorlogEntity> getLogAfter(Date dateTime) {

        QueryWrapper<MybatisErrorlogEntity> wrapper = new QueryWrapper<>();
        wrapper.ge(LogConstant.ERROR_LOG_DATE_FIELD, dateTime);

        return this.errorlogMapper.selectList(wrapper);
    }

    @Override
    public List<? extends ErrorlogEntity> getLogBetween(Date begin, Date end) {

        QueryWrapper<MybatisErrorlogEntity> wrapper = new QueryWrapper<>();
        wrapper.between(LogConstant.ERROR_LOG_DATE_FIELD, begin, end);

        return this.errorlogMapper.selectList(wrapper);
    }

    @Override
    public void cleanAll() {
        this.errorlogMapper.delete(null);
    }

    @Override
    public void cleanBefore(Date dateTime) {
        QueryWrapper<MybatisErrorlogEntity> wrapper = new QueryWrapper<>();
        wrapper.le(LogConstant.ERROR_LOG_DATE_FIELD, dateTime);

        this.errorlogMapper.delete(wrapper);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        QueryWrapper<MybatisErrorlogEntity> wrapper = new QueryWrapper<>();
        wrapper.between(LogConstant.ERROR_LOG_DATE_FIELD, begin, end);

        this.errorlogMapper.delete(wrapper);
    }

    @Override
    public void print() {
    }
}
