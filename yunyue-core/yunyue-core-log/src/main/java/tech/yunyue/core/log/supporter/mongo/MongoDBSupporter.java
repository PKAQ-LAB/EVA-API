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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
    private final MongoTemplate mongoTemplate;
    private final Class clazz;
    private final String dbName;
    private final String historyDbName;
    private final String dateTimeField;
    private final Sort sort;
    private final Type[] realTE;
    public MongoDBSupporter(TypeToken<MongoDBSupporter<T,E>> typeToken, MongoTemplate mongoTemplate, String orderField, String dbName, String hisDBName){
        this.mongoTemplate = mongoTemplate;
        this.dbName = dbName;
        this.historyDbName = hisDBName;
        this.dateTimeField = orderField;
        sort = Sort.by(Sort.Order.desc(orderField));
        this.realTE = ((ParameterizedType) typeToken.getType()).getActualTypeArguments();
        this.clazz = (Class)realTE[0];
    }
    @Override
    public Type[] getRealTE() {
        return realTE;
    }
    @Override
    public void save(T t){
        var  mongoObj = getActualTObj(t);
        mongoTemplate.insert(mongoObj, dbName);
        if (StringUtils.isNotEmpty(this.historyDbName)) {
            mongoTemplate.insert(mongoObj, this.historyDbName);
        }
    }



    @Override
    public List<T> getLog() {
        return mongoTemplate.find(new Query(), clazz, this.dbName);
    }

    @Override
    public List<T> getLogByType(String type) {
        return mongoTemplate.find(
                new Query(Criteria.where("operate_type").is(type)).with(sort),
                clazz,
                this.dbName);
    }

    @Override
    public List<T> getLogAfter(Date dateTime) {
        Criteria criteria = Criteria.where(this.dateTimeField).gte(dateFormat.format(dateTime));
        return mongoTemplate.find(
                new Query(criteria).with(sort),
                clazz,
                this.dbName);
    }

    @Override
    public List<T> getLogBetween(Date begin, Date end) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where(this.dateTimeField).gte(dateFormat.format(begin)),
                Criteria.where(this.dateTimeField).lte(dateFormat.format(end))
        );
        return mongoTemplate.find(
                new Query(criteria).with(sort),
                clazz,
                this.dbName);
    }

    @Override
    public void cleanAll() {
        mongoTemplate.dropCollection(this.dbName);
    }

    @Override
    public void cleanBefore(Date dateTime) {
        Criteria criteria = Criteria.where(this.dateTimeField).gte(dateFormat.format(dateTime));
        mongoTemplate.remove(new Query(criteria), this.dbName);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where(this.dateTimeField).gte(dateFormat.format(begin)),
                Criteria.where(this.dateTimeField).lte(dateFormat.format(end))
        );
        mongoTemplate.remove(new Query(criteria), this.dbName);
    }

    @Override
    public void print() {
    }
}
