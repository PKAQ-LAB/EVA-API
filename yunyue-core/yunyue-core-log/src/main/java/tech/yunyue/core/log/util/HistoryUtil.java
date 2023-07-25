package tech.yunyue.core.log.util;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.annotation.PostConstruct;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.log.annotation.HistoryLog;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class HistoryUtil<T> {
    @Autowired
    MongoTemplate template;
    public static MongoTemplate mongoTemplate;
    public HistoryUtil self;
    public static final Sort sort = Sort.by(Sort.Direction.DESC, "_id");

    @PostConstruct
    public void init(){
        mongoTemplate = template;
    }

    /**
     * 存储历史数据到mongo中 entity对象需要有@TableName或者@HistoryLog标识表名
     * @param entity
     */
    public void sava(T entity) {
        Class clazz = entity.getClass();
        TableName tableName = (TableName) clazz.getAnnotation(TableName.class);
        HistoryLog historyLog = (HistoryLog) clazz.getAnnotation(HistoryLog.class);
        // 需要把错误抛给业务 所以该方法不能是异步
        if (tableName == null && historyLog == null) {
            BizCodeEnum.CAN_NOT_INSERT_HISTORY.newException(entity.getClass());
        }
        // 同类调用异步方法不生效 使用当前类的代理对象调用
        getSelf().save(tableName, historyLog, entity);
    }

    /**
     * 把数据快照异步插入mongo
     */
    @Async("log_task")
    protected void save(TableName tableName, HistoryLog historyLog, T entity) {
        // mongo的表名
        String collectionName = Optional.ofNullable(historyLog).map(HistoryLog::value).orElse(tableName.value());
        // 在mongo表中设置mongo_mark_id字段 做为该表的数据标识
        // 因为有的实体类没有id属性 可以用别的唯一属性作为标识 不管用什么做标识 在mongo中就映射到mongo_mark_id属性上
        JSONObject object = JSONUtil.parseObj(entity);
        String markName = "id";
        if (historyLog != null) {
            markName = historyLog.mark();
        }
        object.set(CommonConstant.MONGO_HISTORY_TABLE_MARK, object.get(markName));
        mongoTemplate.save(object, collectionName);
    }

    /**
     * 根据集合名称和数据id返回该条数据所有的修改记录 key是mongo表中的id 比较数据时需要该id
     * @param collectionName  集合名称
     * @param id  需要查询历史快照的数据的标识 在@HistoryLog注解中以什么字段为标识则传什么 默认为id
     * @param clazz 需要转换的数据类型
     * @return 有序map
     */
    public static <T> LinkedHashMap<String, T> getModifyRecords(String collectionName, String id, Class<T> clazz){
        var list = mongoTemplate.find(Query.query(Criteria.where(CommonConstant.MONGO_HISTORY_TABLE_MARK).is(id)).with(sort), Map.class, collectionName);
        return list.stream().collect(Collectors.toMap(e -> e.get("_id").toString(), e -> JSONUtil.toBean(JSONUtil.parseObj(e), clazz), (k1,k2)->k2, LinkedHashMap::new ));
    }

    /**
     * 返回mid对应的修改记录和前一次修改记录  new对应本次  old对应前一次
     * @param collectionName 集合名称
     * @param mid mongo中的id
     * @param clazz 需要转换的数据类型
     */
    public static <T> Map<String,T> contrastLast(String collectionName, String mid, Class<T> clazz){
        Map<String, T> map = new HashMap<>(2);
        ObjectId objectId = new ObjectId(mid);
        // 根据mid查出本次的修改  不能直接转clazz
        var obj = mongoTemplate.findById(objectId, JSONObject.class, collectionName);
        if (obj != null) {
            map.put("new", JSONUtil.toBean(obj, clazz));
            String id = String.valueOf(obj.get(CommonConstant.MONGO_HISTORY_TABLE_MARK));
            Query query = Query.query(Criteria.where(CommonConstant.MONGO_HISTORY_TABLE_MARK).is(id).and("_id").lt(objectId)).with(sort).limit(1);
            obj = mongoTemplate.findOne(query, JSONObject.class, collectionName);
            map.put("old", JSONUtil.toBean(obj, clazz));
        }
        return map;
    }

    /**
     * 根据mid返回对应的修改记录
     * @param collectionName 集合名称
     * @param mid mongo中的id
     * @param clazz 需要转换的数据类型
     */
    public static <T> T getById(String collectionName, String mid, Class<T> clazz){
        // 根据mid查出本次的修改  不能直接转clazz
        var obj = mongoTemplate.findById(new ObjectId(mid), JSONObject.class, collectionName);
        return JSONUtil.toBean(obj, clazz);
    }

    HistoryUtil getSelf(){
        if (self == null){
            self = SpringUtil.getBean(HistoryUtil.class);
        }
        return self;
    }
}
