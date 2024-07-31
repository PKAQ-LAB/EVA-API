package tech.yunyue.core.log.supporter.mongo;

import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.condition.MongoSupporterCondition;
import tech.yunyue.core.log.config.LogConfig;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.events.LoginLogEvent;
import tech.yunyue.core.log.events.ReportLogEvent;
import tech.yunyue.core.log.supporter.mongo.entity.MongoBizLogEntity;
import tech.yunyue.core.log.supporter.mongo.entity.MongoErrorLogEntity;
import tech.yunyue.core.log.supporter.mongo.entity.MongoLoginLogEntity;
import tech.yunyue.core.log.supporter.mongo.entity.MongoReportLogEntity;
import tech.yunyue.core.properties.EvaConfig;

@Configuration
@ConditionalOnClass(org.springframework.data.mongodb.core.MongoTemplate.class)
@Conditional(MongoSupporterCondition.class)
public class MongoConfig implements LogConfig {
    @Autowired
    EvaConfig evaConfig;
    @Autowired
    MongoTemplate mongoTemplate;


    @Bean
    @Override
    public LogSupporter<MongoBizLogEntity, BizLogEvent> bizLogSupporter(){
        return new MongoDBSupporter<>(new TypeToken<MongoDBSupporter<MongoBizLogEntity, BizLogEvent>>(){}, mongoTemplate, LogConstant.BIZ_LOG_DATE_FIELD, LogConstant.BIZ_LOG_DB_NAME, LogConstant.BIZ_LOG_HISTORY_DB_NAME);
    }

    @Bean
    @Override
    public LogSupporter<MongoErrorLogEntity, ErrorLogEvent> errorLogSupporter(){
        return new MongoDBSupporter<>(new TypeToken<MongoDBSupporter<MongoErrorLogEntity, ErrorLogEvent>>(){}, mongoTemplate, LogConstant.ERROR_LOG_DATE_FIELD, LogConstant.ERROR_LOG_DB_NAME, LogConstant.ERROR_LOG_HISTORY_DB_NAME);
    }

    @Bean
    @Override
    public LogSupporter<MongoLoginLogEntity, LoginLogEvent> loginLogSupporter(){
        return new MongoDBSupporter<>(new TypeToken<MongoDBSupporter<MongoLoginLogEntity, LoginLogEvent>>(){}, mongoTemplate, LogConstant.LOGIN_LOG_DATE_FIELD, LogConstant.LOGIN_DB_NAME, LogConstant.LOGIN_HISTORY_DB_NAME);
    }

    @Bean
    @Override
    public LogSupporter<MongoReportLogEntity, ReportLogEvent> reportLogSupporter(){
        return new ReportPrintMongoDBSupporter(mongoTemplate);
    }
}
