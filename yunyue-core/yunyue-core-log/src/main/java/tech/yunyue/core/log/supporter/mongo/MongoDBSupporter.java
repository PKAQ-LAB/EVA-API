package tech.yunyue.core.log.supporter.mongo;

import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import tech.yunyue.core.log.base.LogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.events.LogEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 基于MongoDB的日志持久化类
 *
 * @author PKAQ
 */
public class MongoDBSupporter<T extends LogEntity, E extends LogEvent> implements LogSupporter<T,E> {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private MongoTemplate mongoTemplate;
    private Class<T> t;
    private static String DB_NAME;
    private static String HISTORY_DB_NAME;
    private static String DATE_TIME_FIELD;
    private Sort sort;
    public MongoDBSupporter(MongoTemplate mongoTemplate, Class<T> t, String orderField, String dbName, String hisDBName){
        this.mongoTemplate = mongoTemplate;
        this.t= t;
        DB_NAME = dbName;
        HISTORY_DB_NAME = hisDBName;
        DATE_TIME_FIELD = orderField;
        sort = Sort.by(Sort.Order.desc(orderField));
    }
    @Override
    public void save(T t){
        var  mongoObj = getActualTObj(t);
        mongoTemplate.insert(mongoObj, DB_NAME);
        if (StringUtils.isNotEmpty(HISTORY_DB_NAME)) {
            mongoTemplate.insert(mongoObj, HISTORY_DB_NAME);
        }
    }



    @Override
    public List<T> getLog() {
        return mongoTemplate.find(new Query(), t, DB_NAME);
    }

    @Override
    public List<T> getLogByType(String type) {
        return mongoTemplate.find(
                new Query(Criteria.where("operate_type").is(type)).with(sort),
                t,
                DB_NAME);
    }

    @Override
    public List<T> getLogAfter(Date dateTime) {
        Criteria criteria = Criteria.where(DATE_TIME_FIELD).gte(dateFormat.format(dateTime));
        return mongoTemplate.find(
                new Query(criteria).with(sort),
                t,
                DB_NAME);
    }

    @Override
    public List<T> getLogBetween(Date begin, Date end) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where(DATE_TIME_FIELD).gte(dateFormat.format(begin)),
                Criteria.where(DATE_TIME_FIELD).lte(dateFormat.format(end))
        );
        return mongoTemplate.find(
                new Query(criteria).with(sort),
                t,
                DB_NAME);
    }

    @Override
    public void cleanAll() {
        mongoTemplate.dropCollection(DB_NAME);
    }

    @Override
    public void cleanBefore(Date dateTime) {
        Criteria criteria = Criteria.where(DATE_TIME_FIELD).gte(dateFormat.format(dateTime));
        mongoTemplate.remove(new Query(criteria), DB_NAME);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where(DATE_TIME_FIELD).gte(dateFormat.format(begin)),
                Criteria.where(DATE_TIME_FIELD).lte(dateFormat.format(end))
        );
        mongoTemplate.remove(new Query(criteria), DB_NAME);
    }

    @Override
    public void print() {
    }
}
