package tech.yunyue.core.rabbitmq.log;

import cn.hutool.core.collection.CollectionUtil;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.BinLogEntity;
import tech.yunyue.core.log.enums.BinLogTypeEnum;
import tech.yunyue.core.properties.BizLog;
import tech.yunyue.core.properties.EvaConfig;

import java.util.*;

/**
 * 快照日志消息消费者
 */
@Component
@ConditionalOnClass(org.springframework.data.mongodb.core.MongoTemplate.class)
@RabbitListener(queues = "snapshot.record")
public class BinLogReceiver {
    @Autowired
    EvaConfig evaConfig;
    @Autowired
    MongoTemplate mongoTemplate;

    /**
     * 快照日志
     * @param maxwellData
     */
    @RabbitHandler
    public void process(BinLogEntity maxwellData) {
        BizLog log = evaConfig.getBizlog();
        // 只处理需要保存快照的表
        if (CollectionUtil.isEmpty(log.getSnapshotTableNames()) ||
                !log.getSnapshotTableNames().stream().allMatch(e->e.equals(maxwellData.getTable()))) return;
        // 只处理新增和修改
        Optional<BinLogTypeEnum> optionalType = BinLogTypeEnum.getEnumByType(maxwellData.getType());
        if(optionalType.isEmpty()) return;

        String collectionName = maxwellData.getTable();
        Map<String, Object> map = maxwellData.getData();
        if (optionalType.get().equals(BinLogTypeEnum.UPDATE)) {
            // 修改则保存修改新值  新值在data中  修改的key在old中
            map = maxwellData.getOld();
            map.put("ID", "");
            Map<String, Object> temp = new HashMap<>(map.size());
            map.forEach((k, v) -> temp.put(k, maxwellData.getData().get(k)));
            map = temp;
        }
        mongoTemplate.insert(map, collectionName);
    }
}
