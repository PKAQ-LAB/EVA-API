package org.pkaq.core.mybatis.log;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.BizLogSupporter;
import org.pkaq.core.log.condition.MybatisSupporterCondition;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mybatis.log.entity.MybatisBizLogEntity;
import org.pkaq.core.mybatis.log.mapper.MybatisSupporterMapper;
import org.pkaq.core.util.BeanUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * mybatis 存储实现类
 *
 * @author: S.PKAQ
 */
@Slf4j
@Component
@Conditional(MybatisSupporterCondition.class)
@RequiredArgsConstructor
public class MybatisLogSupporter implements BizLogSupporter {

    private final MybatisSupporterMapper mybatisSupporterMapper;

    @Override
    public void save(BizLogEntity bizLogEntity) {
        MybatisBizLogEntity mybatisBizLogEntity = new MybatisBizLogEntity();
        BeanUtils.copyProperties(bizLogEntity, mybatisBizLogEntity);

        this.mybatisSupporterMapper.insert(mybatisBizLogEntity);
    }

    @Override
    public List<? extends BizLogEntity> getLog() {
        return this.mybatisSupporterMapper.selectList(null);
    }

    @Override
    public List<? extends BizLogEntity> getLogByType(String type) {

        MybatisBizLogEntity mybatisBizLogEntity = new MybatisBizLogEntity();
        mybatisBizLogEntity.setOperateType(type);

        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>(mybatisBizLogEntity);
        return this.mybatisSupporterMapper.selectList(wrapper);
    }

    @Override
    public List<? extends BizLogEntity> getLogAfter(Date dateTime) {

        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        wrapper.ge("operate_datetime", dateTime);

        return this.mybatisSupporterMapper.selectList(wrapper);
    }

    @Override
    public List<? extends BizLogEntity> getLogBetween(Date begin, Date end) {

        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        wrapper.between("operate_datetime", begin, end);

        return this.mybatisSupporterMapper.selectList(wrapper);
    }

    @Override
    public void cleanAll() {
        this.mybatisSupporterMapper.delete(null);
    }

    @Override
    public void cleanBefore(Date dateTime) {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        wrapper.le("operate_datetime", dateTime);

        this.mybatisSupporterMapper.delete(wrapper);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();
        wrapper.between("operate_datetime", begin, end);

        this.mybatisSupporterMapper.delete(wrapper);
    }

    @Override
    public void print() {
    }

    @Override
    public String get(String id) {
        return "";
    }

    @Override
    public IPage list(DateRangeBo dateRangeBo) {
        return null;
    }
}
