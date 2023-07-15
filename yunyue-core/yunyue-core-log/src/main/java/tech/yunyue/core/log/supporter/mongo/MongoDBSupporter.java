package tech.yunyue.core.log.supporter.mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Sort;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.BizLogSupporter;
import tech.yunyue.core.log.condition.MongoSupporterCondition;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 基于MongoDB的日志持久化类
 *
 * @author PKAQ
 */
@Component
@ConditionalOnClass(org.springframework.data.mongodb.core.MongoTemplate.class)
@Conditional(MongoSupporterCondition.class)
@RequiredArgsConstructor
public class MongoDBSupporter implements BizLogSupporter {
    private static final String DB_NAME = "log_biz";
    private static final String HISTORY_DB_NAME = "history_log_biz";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Sort sort = Sort.by(Sort.Order.desc("operate_datetime"));
    private final MongoTemplate mongoTemplate;

    @Override
    public void save(BizLogEntity bizLogEntity) {
        mongoTemplate.insert(bizLogEntity, DB_NAME);
        mongoTemplate.insert(bizLogEntity, HISTORY_DB_NAME);
    }



    @Override
    public List<? extends BizLogEntity> getLog() {
        return mongoTemplate.find(new Query(), BizLogEntity.class, DB_NAME);
    }

    @Override
    public List<? extends BizLogEntity> getLogByType(String type) {
        return mongoTemplate.find(
                new Query(Criteria.where("operate_type").is(type)).with(sort),
                BizLogEntity.class,
                DB_NAME);
    }

    @Override
    public List<? extends BizLogEntity> getLogAfter(Date dateTime) {
        Criteria criteria = Criteria.where("operate_datetime").gte(dateFormat.format(dateTime));
        return mongoTemplate.find(
                new Query(criteria).with(sort),
                BizLogEntity.class,
                DB_NAME);
    }

    @Override
    public List<? extends BizLogEntity> getLogBetween(Date begin, Date end) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("operate_datetime").gte(dateFormat.format(begin)),
                Criteria.where("operate_datetime").lte(dateFormat.format(end))
        );
        return mongoTemplate.find(
                new Query(criteria).with(sort),
                BizLogEntity.class,
                DB_NAME);
    }

    @Override
    public void cleanAll() {
        mongoTemplate.dropCollection(DB_NAME);
    }

    @Override
    public void cleanBefore(Date dateTime) {
        Criteria criteria = Criteria.where("operate_datetime").gte(dateFormat.format(dateTime));
        mongoTemplate.remove(new Query(criteria), DB_NAME);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("operate_datetime").gte(dateFormat.format(begin)),
                Criteria.where("operate_datetime").lte(dateFormat.format(end))
        );
        mongoTemplate.remove(new Query(criteria), DB_NAME);
    }

    @Override
    public void print() {
    }
}
