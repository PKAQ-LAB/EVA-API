package tech.yunyue.core.log.supporter.mongo;

import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.config.LogConfig;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.supporter.mongo.entity.MongoBizLogEntity;
import tech.yunyue.core.log.supporter.mongo.entity.MongoErrorLogEntity;
import tech.yunyue.core.properties.BizLog;
import tech.yunyue.core.properties.ErrorLog;
import tech.yunyue.core.properties.EvaConfig;

@Configuration
@ConditionalOnClass(org.springframework.data.mongodb.core.MongoTemplate.class)
public class MongoConfig implements LogConfig {
    @Autowired
    EvaConfig evaConfig;
    @Autowired
    MongoTemplate mongoTemplate;


    @ConditionalOnExpression("'mongo'.equals('${eva.bizlog.impl}')")
    @Bean
    @Override
    public LogSupporter<MongoBizLogEntity, BizLogEvent> bizLogSupporter(){
        BizLog bizLog = evaConfig.getBizlog();
        return new MongoDBSupporter<>(new TypeToken<MongoDBSupporter<MongoBizLogEntity, BizLogEvent>>(){}, mongoTemplate, bizLog.getDateField(), bizLog.getDbName(), bizLog.getHisDBName());
    }

    @ConditionalOnExpression("'mongo'.equals('${eva.errorlog.impl}')")
    @Bean
    @Override
    public LogSupporter<MongoErrorLogEntity, ErrorLogEvent> errorLogSupporter(){
        ErrorLog errorLog = evaConfig.getErrorLog();
        return new MongoDBSupporter<>(new TypeToken<MongoDBSupporter<MongoErrorLogEntity, ErrorLogEvent>>(){}, mongoTemplate, errorLog.getDateField(), errorLog.getDbName(), errorLog.getHisDBName());
    }

    //todo  登录登出日志
}
