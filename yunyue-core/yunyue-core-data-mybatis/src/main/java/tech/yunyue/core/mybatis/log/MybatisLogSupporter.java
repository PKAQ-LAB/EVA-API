package tech.yunyue.core.mybatis.log;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import tech.yunyue.core.log.base.LogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.events.LogEvent;

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
public class MybatisLogSupporter<T extends LogEntity, E extends LogEvent> implements LogSupporter<T,E> {
    private final BaseMapper mapper;
    private final String dateTimeField;
    private final Type[] realTE;
    public MybatisLogSupporter(TypeToken<MybatisLogSupporter<T,E>> typeToken, BaseMapper<T> mapper, String orderField) {
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
        var  mybatisObj = getActualTObj(t);
        this.mapper.insert(mybatisObj);
    }

    @Override
    public List<T> getLog() {
        return this.mapper.selectList(null);
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
}
