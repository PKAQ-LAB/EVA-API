package org.pkaq.core.bizlog.supporter.mybatis;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.bizlog.base.BizLogEntity;
import org.pkaq.core.bizlog.base.BizLogSupporter;
import org.pkaq.core.bizlog.condition.MybatisSupporterCondition;
import org.pkaq.core.bizlog.supporter.mybatis.entity.MybatisBizLogEntity;
import org.pkaq.core.bizlog.supporter.mybatis.mapper.MybatisSupporterMapper;
import org.pkaq.core.util.UUIDHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * mybatis 存储实现类
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 8:38
 */
@Slf4j
@Component
@Conditional(MybatisSupporterCondition.class)
public class MybatisSupporter implements BizLogSupporter {
    private BizLogEntity bizLogEntity;

    @Autowired
    private MybatisSupporterMapper mybatisSupporterMapper;

    public MybatisSupporter(){
        super();
    }

    public MybatisSupporter(BizLogEntity bizLogEntity) {
        this.bizLogEntity = bizLogEntity;
    }

    @Override
    public void save(BizLogEntity bizLogEntity) {
        MybatisBizLogEntity mybatisBizLogEntity = new MybatisBizLogEntity();
        mybatisBizLogEntity.setId(UUIDHelper.id());
        mybatisBizLogEntity.setDescription(bizLogEntity.getDescription());
        mybatisBizLogEntity.setOperateType(bizLogEntity.getOperateType());
        mybatisBizLogEntity.setOperator(bizLogEntity.getOperator());
        mybatisBizLogEntity.setOperateDatetime(DateUtil.now());

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
}
