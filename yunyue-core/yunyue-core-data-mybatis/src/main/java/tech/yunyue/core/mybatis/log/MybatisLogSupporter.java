package tech.yunyue.core.mybatis.log;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import tech.yunyue.core.log.base.*;
import tech.yunyue.core.log.base.bo.LogQueryBo;
import tech.yunyue.core.log.events.LogEvent;
import tech.yunyue.core.mybatis.mvc.util.Page;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * mybatis 存储实现类
 *
 * @author: S.PKAQ
 */
@Slf4j
public class MybatisLogSupporter<T extends LogEntity, E extends LogEvent> implements LogSupporter<T, E> {
    private final BaseMapper mapper;
    private final String dateTimeField;
    private final Type[] realTE;

    public MybatisLogSupporter(TypeToken<MybatisLogSupporter<T, E>> typeToken, BaseMapper<T> mapper, String orderField) {
        this.mapper = mapper;
        this.dateTimeField = orderField;
        this.realTE = ((ParameterizedType) typeToken.getType()).getActualTypeArguments();
    }

    @Override
    public Type[] getRealTE() {
        return realTE;
    }

    @Override
    public void save(T t) {
        var mybatisObj = getActualTObj(t);
        this.mapper.insert(mybatisObj);
    }

    @Override
    public List<T> getLog() {
        return this.mapper.selectList(null);
    }

    @Override
    public T getLogById(String id) {
        return (T) this.mapper.selectById(id);
    }

    @Override
    public IPage<T> getLogByQuery(LogQueryBo<T> queryBo) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        if (queryBo.getLogEntity() instanceof BizLogEntity log) {
            buildBizLogQuery(queryBo, wrapper, log);
        } else if (queryBo.getLogEntity() instanceof ErrorlogEntity log) {
            buildErrorLogQuery(queryBo, wrapper, log);
        } else if (queryBo.getLogEntity() instanceof LoginlogEntity log) {
            buildLoginLogQuery(queryBo, wrapper, log);
        }
        Page<T> pagination = new Page<>(queryBo.getPageNo(), queryBo.getPageSize());
        return this.mapper.selectPage(pagination, wrapper);
    }
    @Override
    public List<T> getLogByType(String type) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.eq("operate_type", type);
        return this.mapper.selectList(wrapper);
    }

    @Override
    public List<T> getLogAfter(Date dateTime) {

        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.ge(this.dateTimeField, dateTime);

        return this.mapper.selectList(wrapper);
    }

    @Override
    public List<T> getLogBetween(Date begin, Date end) {

        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.between(this.dateTimeField, begin, end);

        return this.mapper.selectList(wrapper);
    }

    @Override
    public void cleanAll() {
        this.mapper.delete(null);
    }

    @Override
    public void cleanBefore(Date dateTime) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.le(this.dateTimeField, dateTime);

        this.mapper.delete(wrapper);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.between(this.dateTimeField, begin, end);

        this.mapper.delete(wrapper);
    }

    @Override
    public void print() {
    }

    private void buildLoginLogQuery(LogQueryBo<T> queryBo, QueryWrapper<T> wrapper, LoginlogEntity log) {
        wrapper.like(CharSequenceUtil.isNotBlank(log.getOperator()), "operator", log.getOperator());
        wrapper.eq(CharSequenceUtil.isNotBlank(log.getOperateType()), "operate_type", log.getOperator());
        wrapper.between(queryBo.getBegin() != null && queryBo.getEnd() != null, "operate_datetime", queryBo.getBegin(), queryBo.getEnd());
        wrapper.ge(queryBo.getBegin() != null && queryBo.getEnd() == null, "operate_datetime", queryBo.getBegin());
        wrapper.le(queryBo.getBegin() == null && queryBo.getEnd() != null, "operate_datetime", queryBo.getEnd());
        wrapper.like(CharSequenceUtil.isNotBlank(log.getDevice()), "device", log.getDevice());
        wrapper.like(CharSequenceUtil.isNotBlank(log.getVersion()), "version", log.getVersion());
    }
    private void buildErrorLogQuery(LogQueryBo<T> queryBo, QueryWrapper<T> wrapper, ErrorlogEntity log) {
        wrapper.between(queryBo.getBegin() != null && queryBo.getEnd() != null, "request_time", queryBo.getBegin(), queryBo.getEnd());
        wrapper.ge(queryBo.getBegin() != null && queryBo.getEnd() == null, "request_time", queryBo.getBegin());
        wrapper.le(queryBo.getBegin() == null && queryBo.getEnd() != null, "request_time", queryBo.getEnd());
        wrapper.like(CharSequenceUtil.isNotBlank(log.getIp()), "ip", log.getIp());
        wrapper.like(CharSequenceUtil.isNotBlank(log.getClassName()), "class_name", log.getClassName());
        wrapper.like(CharSequenceUtil.isNotBlank(log.getMethod()), "method", log.getMethod());
        wrapper.like(CharSequenceUtil.isNotBlank(log.getLoginUser()), "login_user", log.getLoginUser());
    }
    private void buildBizLogQuery(LogQueryBo<T> queryBo, QueryWrapper<T> wrapper, BizLogEntity log) {
        wrapper.like(CharSequenceUtil.isNotBlank(log.getOperator()), "operator", log.getOperator());
        wrapper.eq(CharSequenceUtil.isNotBlank(log.getOperateType()), "operate_type", log.getOperator());
        wrapper.between(queryBo.getBegin() != null && queryBo.getEnd() != null, "operate_datetime", queryBo.getBegin(), queryBo.getEnd());
        wrapper.ge(queryBo.getBegin() != null && queryBo.getEnd() == null, "operate_datetime", queryBo.getBegin());
        wrapper.le(queryBo.getBegin() == null && queryBo.getEnd() != null, "operate_datetime", queryBo.getEnd());
        wrapper.like(CharSequenceUtil.isNotBlank(log.getClassName()), "class_name", log.getClassName());
        wrapper.like(CharSequenceUtil.isNotBlank(log.getMethod()), "method", log.getMethod());
        wrapper.like(CharSequenceUtil.isNotBlank(log.getDevice()), "device", log.getDevice());
        wrapper.like(CharSequenceUtil.isNotBlank(log.getVersion()), "version", log.getVersion());
    }
}
