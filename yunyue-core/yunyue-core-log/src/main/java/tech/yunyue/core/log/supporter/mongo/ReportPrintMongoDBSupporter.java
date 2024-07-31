package tech.yunyue.core.log.supporter.mongo;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import tech.yunyue.core.log.base.bo.LogQueryBo;
import tech.yunyue.core.log.base.bo.ReportPrintCountQueryBo;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.enums.ReportLogTypeEnum;
import tech.yunyue.core.log.events.ReportLogEvent;
import tech.yunyue.core.log.supporter.mongo.entity.MongoReportLogEntity;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 报表打印日志的基于MongoDB的日志持久化类
 *
 * @author mja
 */
public class ReportPrintMongoDBSupporter extends MongoDBSupporter<MongoReportLogEntity, ReportLogEvent> {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final MongoTemplate mongoTemplate;
    private final String dbName;
    private final String historyDbName;
    private final String dateTimeField;
    private final Sort sort;

    public ReportPrintMongoDBSupporter(MongoTemplate mongoTemplate) {
        super(new TypeToken<MongoDBSupporter<MongoReportLogEntity, ReportLogEvent>>() {
        }, mongoTemplate, LogConstant.LOGIN_LOG_DATE_FIELD, LogConstant.REPORT_DB_NAME, LogConstant.REPORT_HISTORY_DB_NAME);
        this.mongoTemplate = mongoTemplate;
        this.dbName = LogConstant.REPORT_DB_NAME;
        this.historyDbName = LogConstant.REPORT_HISTORY_DB_NAME;
        this.dateTimeField = LogConstant.LOGIN_LOG_DATE_FIELD;
        sort = Sort.by(Sort.Order.desc(dateTimeField));
    }


    @Override
    public void save(MongoReportLogEntity reportLog) {
        // 保存报表日志
        var tenantId = ThreadUserHelper.getTenantId();
        var uId = ThreadUserHelper.getUserId();
        var uName = ThreadUserHelper.getUserName();
        var uPostId = ThreadUserHelper.getPostId();
        var uOrgId = ThreadUserHelper.getOrgId();
        reportLog.setTenantId(tenantId);
        reportLog.setOperateDatetime(DateUtil.now());
        reportLog.setCreateName(uName);
        reportLog.setCreateId(uId);
        reportLog.setOrgId(uOrgId);
        reportLog.setPostId(uPostId);
        if(CharSequenceUtil.isBlank(reportLog.getMCode())){
            reportLog.setMCode(ThreadUserHelper.getMcode());
        }
        super.save(reportLog);

        // 保存报表打印次数 存在则次数+1 否则新增记录
        var reportCode = reportLog.getReportCode();
        var bizId = reportLog.getBizId();
        var operateType = switch (ReportLogTypeEnum.getByCode(reportLog.getOperateType())) {
            case EXPORT, EXPORT_EXCEL, EXPORT_PDF, EXPORT_WORD -> ReportLogTypeEnum.EXPORT.getCode();
            case PRINT, PRINT_NOW, PRINT_ALL -> ReportLogTypeEnum.PRINT.getCode();
        };
        Query query = new Query(Criteria.where("report_code").is(reportCode)
                .and("biz_id").is(bizId)
                .and("tenant_id").is(tenantId)
                .and("operate_type").is(operateType));
        Update update = new Update()
                .set("report_code", reportCode)
                .set("biz_id", bizId)
                .set("operate_id", uId)
                .set("operate_name", uName)
                .set(dateTimeField, DateUtil.now())
                .set("tenant_id", tenantId)
                .set("operate_type", operateType)
                .inc("count", 1);
        mongoTemplate.upsert(query, update, LogConstant.REPORT_COUNT_DB_NAME);
        mongoTemplate.upsert(query, update, LogConstant.REPORT_COUNT_HISTORY_DB_NAME);
    }

    @Override
    public <U> IPage<U> getLogByQuery(LogQueryBo<MongoReportLogEntity> queryBo, Class<U> uClass) {
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
            var log = queryBo.getLogEntity();
            addEqCriteria("m_code", log.getMCode(), list);
            addEqCriteria("biz_id", log.getBizId(), list);
            addEqCriteria("operate_type", log.getOperateType(), list);
            addEqCriteria("report_id", log.getReportId(), list);
            addEqCriteria("report_name", log.getReportName(), list);
            addEqCriteria("report_code", log.getReportCode(), list);
            addLikeCriteria("description", log.getDescription(), list);
        });
        var dbName = queryBo instanceof ReportPrintCountQueryBo ? LogConstant.REPORT_COUNT_DB_NAME : this.dbName;
        if (list.isEmpty()) {
            // 查询总数，当查询条件为空时用estimatedDocumentCount统计数量以优化查询速度
            totalCount = mongoTemplate.getCollection(dbName).estimatedDocumentCount();
        } else {
            query.addCriteria(new Criteria().andOperator(list));
            totalCount = mongoTemplate.count(query, dbName);
        }

        // 增加分页条件
        query.with(PageRequest.of(queryBo.getPageNo() - 1, queryBo.getPageSize()));
        // 排序
        query.with(sort);
        // 构造分页返回
        IPage<U> pageVo = new Page<>(queryBo.getPageNo(), queryBo.getPageSize());
        pageVo.setRecords(mongoTemplate.find(query, uClass, dbName));
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
}
