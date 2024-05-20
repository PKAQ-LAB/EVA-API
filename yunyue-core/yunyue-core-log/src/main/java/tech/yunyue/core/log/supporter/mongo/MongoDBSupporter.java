package tech.yunyue.core.log.supporter.mongo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import tech.yunyue.core.log.base.*;
import tech.yunyue.core.log.base.bo.LogQueryBo;
import tech.yunyue.core.log.events.LogEvent;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 基于MongoDB的日志持久化类
 *
 * @author PKAQ
 */
public class MongoDBSupporter<T extends LogEntity, E extends LogEvent> implements LogSupporter<T, E> {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final MongoTemplate mongoTemplate;
    private final Class clazz;
    private final String dbName;
    private final String historyDbName;
    private final String dateTimeField;
    private final Sort sort;
    private final Type[] realTE;

    public MongoDBSupporter(TypeToken<MongoDBSupporter<T, E>> typeToken, MongoTemplate mongoTemplate, String orderField, String dbName, String hisDBName) {
        this.mongoTemplate = mongoTemplate;
        this.dbName = dbName;
        this.historyDbName = hisDBName;
        this.dateTimeField = orderField;
        sort = Sort.by(Sort.Order.desc(orderField));
        this.realTE = ((ParameterizedType) typeToken.getType()).getActualTypeArguments();
        this.clazz = (Class) realTE[0];
    }

    @Override
    public Type[] getRealTE() {
        return realTE;
    }

    @Override
    public void save(T t) {
        var mongoObj = getActualTObj(t);
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
    public T getLogById(String id) {
        return (T) mongoTemplate.findById(id, clazz, this.dbName);
    }

    @Override
    public IPage<T> getLogByQuery(LogQueryBo<T> queryBo) {
        return getLogByQuery(queryBo, clazz);
    }

    @Override
    public <U> IPage<U> getLogByQuery(LogQueryBo<T> queryBo, Class<U> uClass) {
        // 总数
        long totalCount;

        // 构造查询条件
        Query query = new Query();
        List<Criteria> list = new ArrayList<>();
        // 租户
        if (CharSequenceUtil.isNotBlank(ThreadUserHelper.getTenantId())) {
            list.add(Criteria.where("tenant_id").is(ThreadUserHelper.getTenantId()));
        }
        // 页面查询条件
        Optional.ofNullable(queryBo.getBegin()).ifPresent(begin -> list.add(Criteria.where(dateTimeField).gte(dateFormat.format(begin))));
        Optional.ofNullable(queryBo.getEnd()).ifPresent(end -> list.add(Criteria.where(dateTimeField).lte(dateFormat.format(end))));
        Optional.ofNullable(queryBo.getLogEntity()).ifPresent(logEntity -> {
            addEqCriteria("create_id", logEntity.getCreateId(), list);
            addEqCriteria("post_id", logEntity.getPostId(), list);
            addEqCriteria("org_id", logEntity.getOrgId(), list);
            if (logEntity instanceof BizLogEntity log) {
                addEqCriteria("operate_type", log.getOperateType(), list);
                addEqCriteria("b_id", log.getBId(), list);
                addEqCriteria("m_code", log.getMCode(), list);
                addLikeCriteria("operator", log.getOperator(), list);
                addLikeCriteria("class_name", log.getClassName(), list);
                addLikeCriteria("method", log.getMethod(), list);
                addLikeCriteria("device", log.getDevice(), list);
                addLikeCriteria("version", log.getVersion(), list);
                addLikeCriteria("params", log.getParams(), list);
            } else if (logEntity instanceof ErrorlogEntity log) {
                addLikeCriteria("ip", log.getIp(), list);
                addLikeCriteria("class_name", log.getClassName(), list);
                addLikeCriteria("method", log.getMethod(), list);
                addLikeCriteria("login_user", log.getLoginUser(), list);
                addLikeCriteria("params", log.getParams(), list);
            } else if (logEntity instanceof LoginlogEntity log) {
                addEqCriteria("operate_type", log.getOperateType(), list);
                addLikeCriteria("operator", log.getOperator(), list);
                addLikeCriteria("operator_name", log.getOperatorName(), list);
                addLikeCriteria("device", log.getDevice(), list);
                addLikeCriteria("version", log.getVersion(), list);
            } else if (logEntity instanceof ReportLogEntity log) {
                addEqCriteria("operate_type", log.getOperateType(), list);
                addLikeCriteria("report_id", log.getReportId(), list);
                addLikeCriteria("report_name", log.getReportName(), list);
                addLikeCriteria("report_code", log.getReportCode(), list);
                addLikeCriteria("description", log.getDescription(), list);
            }
        });
        if (list.isEmpty()) {
            // 查询总数，当查询条件为空时用estimatedDocumentCount统计数量以优化查询速度
            totalCount = mongoTemplate.getCollection(this.dbName).estimatedDocumentCount();
        } else {
            query.addCriteria(new Criteria().andOperator(list));
            totalCount = mongoTemplate.count(query, this.dbName);
        }

        // 增加分页条件
        query.with(PageRequest.of(queryBo.getPageNo() - 1, queryBo.getPageSize()));
        // 排序
        query.with(sort);
        // 构造分页返回
        IPage<U> pageVo = new Page<>(queryBo.getPageNo(), queryBo.getPageSize());
        pageVo.setRecords(mongoTemplate.find(query, uClass, this.dbName));
        pageVo.setTotal(totalCount);
        return pageVo;
    }


    /**
     * 如果value不为空 则往集合中添加一个key=value的Criteria
     */
    private void addEqCriteria(String key, String value, List<Criteria> list) {
        if (CharSequenceUtil.isNotBlank(value)) {
            list.add(Criteria.where(key).is(value));
        }
    }

    /**
     * 如果value不为空 则往集合中添加一个key like %value%的Criteria
     */
    private void addLikeCriteria(String key, String value, List<Criteria> list) {
        if (CharSequenceUtil.isNotBlank(value)) {
            list.add(Criteria.where(key).regex(Pattern.compile(value, Pattern.CASE_INSENSITIVE)));
        }
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
