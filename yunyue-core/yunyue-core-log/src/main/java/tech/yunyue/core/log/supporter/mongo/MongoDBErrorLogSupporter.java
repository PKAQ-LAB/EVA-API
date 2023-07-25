package tech.yunyue.core.log.supporter.mongo;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.ErrorLogSupporter;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.supporter.mongo.entity.MongoErrorLogEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 基于MongoDB的错误日志持久化类
 */
@Component
@ConditionalOnClass(MongoTemplate.class)
@ConditionalOnExpression("${eva.errorlog.enabled}&&'mongo'.equals('${eva.errorlog.impl}')")
@RequiredArgsConstructor
public class MongoDBErrorLogSupporter implements ErrorLogSupporter {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String dateField = LogConstant.ERROR_LOG_DATE_FIELD;
    private static final Sort sort = Sort.by(Sort.Order.desc(dateField));
    private final MongoTemplate mongoTemplate;

    @Override
    public void save(ErrorlogEntity errorlogEntity){
        MongoErrorLogEntity mongoErrorLog = new MongoErrorLogEntity();
        BeanUtil.copyProperties(errorlogEntity, mongoErrorLog);
        mongoErrorLog.setExpireTime(new Date());

        mongoTemplate.insert(mongoErrorLog, LogConstant.ERROR_DB_NAME);
    }



    @Override
    public List<? extends ErrorlogEntity> getLog() {
        return mongoTemplate.find(new Query(), ErrorlogEntity.class, LogConstant.ERROR_DB_NAME);
    }

    @Override
    public List<? extends ErrorlogEntity> getLogAfter(Date dateTime) {
        Criteria criteria = Criteria.where(dateField).gte(dateFormat.format(dateTime));
        return mongoTemplate.find(
                new Query(criteria).with(sort),
                ErrorlogEntity.class,
                LogConstant.ERROR_DB_NAME);
    }

    @Override
    public List<? extends ErrorlogEntity> getLogBetween(Date begin, Date end) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where(dateField).gte(dateFormat.format(begin)),
                Criteria.where(dateField).lte(dateFormat.format(end))
        );
        return mongoTemplate.find(
                new Query(criteria).with(sort),
                ErrorlogEntity.class,
                LogConstant.ERROR_DB_NAME);
    }

    @Override
    public void cleanAll() {
        mongoTemplate.dropCollection(LogConstant.ERROR_DB_NAME);
    }

    @Override
    public void cleanBefore(Date dateTime) {
        Criteria criteria = Criteria.where(dateField).gte(dateFormat.format(dateTime));
        mongoTemplate.remove(new Query(criteria), LogConstant.ERROR_DB_NAME);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where(dateField).gte(dateFormat.format(begin)),
                Criteria.where(dateField).lte(dateFormat.format(end))
        );
        mongoTemplate.remove(new Query(criteria), LogConstant.ERROR_DB_NAME);
    }

    @Override
    public void print() {
    }
}
