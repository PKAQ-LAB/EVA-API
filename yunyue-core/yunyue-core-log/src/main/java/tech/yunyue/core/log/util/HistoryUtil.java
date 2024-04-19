package tech.yunyue.core.log.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.log.annotation.HistoryLog;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.util.*;

@Component
public class HistoryUtil<T> {
    @Autowired
    private MongoTemplate mongoTemplate;
    private HistoryUtil self;
    public static final Sort sort = Sort.by(Sort.Direction.DESC, "_id");

    /**
     * 存储历史数据到mongo中 entity对象需要有@TableName或者@HistoryLog标识表名
     *
     * @param entity
     */
    public void sava(T entity) {
        Class clazz = Optional.ofNullable(entity).orElseThrow(BizCodeEnum.HISTORY_LOG_CAN_NOT_NULL::newException).getClass();
        var tableName = getTableName(clazz);
        // 同类调用异步方法不生效 使用当前类的代理对象调用
        String createName = Optional.ofNullable(ThreadUserHelper.getUserName()).orElse(ThreadUserHelper.getAccount());
        getSelf().save(tableName, (HistoryLog) clazz.getAnnotation(HistoryLog.class), entity, createName);
    }

    /**
     * 把数据快照异步插入mongo
     */
    @Async("log_task")
    protected void save(String tableName, HistoryLog historyLog, T entity, String name) {
        // 在mongo表中设置mongo_mark_id字段 做为该表的数据标识
        // 因为有的实体类没有id属性 可以用别的唯一属性作为标识 不管用什么做标识 在mongo中就映射到mongo_mark_id属性上
        JSONObject object = JSONUtil.parseObj(entity);
        String markName = "id";
        if (historyLog != null) {
            markName = historyLog.mark();
        }
        object.set(LogConstant.MONGO_HISTORY_TABLE_MARK, object.get(markName));
        object.set(LogConstant.MONGO_CREATE_TIME, DateUtil.now());
        object.set(LogConstant.MONGO_CREATE_NAME, name);
        object.set(LogConstant.MONGO_EXPIRE_TIME, new Date());
        mongoTemplate.save(object, tableName);
    }

    /**
     * 根据数据id返回该条数据所有的修改记录 key是mongo表中的id 比较数据时需要该id
     *
     * @param id    需要查询历史快照的数据的标识 在@HistoryLog注解中以什么字段为标识则传什么 默认为id
     * @param clazz 需要转换的数据类型  有@TableName或者@HistoryLog标识表名
     * @return 有序map
     */
    public <T> List<JSONObject> getModifyRecords(String id, Class<T> clazz) {
        return getModifyRecords(getTableName(clazz), id);
    }

    /**
     * 根据集合名称和数据id返回该条数据所有的修改记录 key是mongo表中的id 比较数据时需要该id
     *
     * @param collectionName 集合名称
     * @param id             需要查询历史快照的数据的标识 在@HistoryLog注解中以什么字段为标识则传什么 默认为id
     * @return 有序map
     */
    public List<JSONObject> getModifyRecords(String collectionName, String id) {
        var query = Query.query(Criteria.where(LogConstant.MONGO_HISTORY_TABLE_MARK).is(id)).with(sort);
        query.fields().include(LogConstant.MONGO_HISTORY_TABLE_MARK, LogConstant.MONGO_CREATE_TIME, LogConstant.MONGO_CREATE_NAME);
        var list = mongoTemplate
                .find(query, Map.class, collectionName);
        return list.stream().map(e -> {
            e.put("mid", e.get("_id").toString());
            e.remove("_id");
            return JSONUtil.toBean(JSONUtil.parseObj(e), JSONObject.class);
        }).toList();
    }

    /**
     * 返回mid对应的修改记录和前一次修改记录  new对应本次  old对应前一次
     *
     * @param mid   mongo中的id
     * @param clazz 需要转换的数据类型 有@TableName或者@HistoryLog标识表名
     */
    public <T> Map<String, T> contrastLast(String mid, Class<T> clazz) {
        return contrastLast(getTableName(clazz), mid, clazz);
    }

    /**
     * 返回mid对应的修改记录和前一次修改记录  new对应本次  old对应前一次
     *
     * @param collectionName 集合名称
     * @param mid            mongo中的id
     * @param clazz          需要转换的数据类型
     */
    public <T> Map<String, T> contrastLast(String collectionName, String mid, Class<T> clazz) {
        Map<String, T> map = new HashMap<>(2);
        ObjectId objectId = new ObjectId(mid);
        // 根据mid查出本次的修改  不能直接转clazz
        var obj = mongoTemplate.findById(objectId, JSONObject.class, collectionName);
        if (obj != null) {
            map.put("new", JSONUtil.toBean(obj, clazz));
            String id = String.valueOf(obj.get(LogConstant.MONGO_HISTORY_TABLE_MARK));
            Query query = Query.query(Criteria.where(LogConstant.MONGO_HISTORY_TABLE_MARK).is(id).and("_id").lt(objectId)).with(sort).limit(1);
            obj = mongoTemplate.findOne(query, JSONObject.class, collectionName);
            map.put("old", JSONUtil.toBean(obj, clazz));
        }
        return map;
    }

    /**
     * 根据mid返回对应的修改记录
     *
     * @param mid   mongo中的id
     * @param clazz 需要转换的数据类型 有@TableName或者@HistoryLog标识表名
     */
    public <T> T getById(String mid, Class<T> clazz) {
        return getById(getTableName(clazz), mid, clazz);
    }

    /**
     * 根据mid返回对应的修改记录
     *
     * @param collectionName 集合名称
     * @param mid            mongo中的id
     * @param clazz          需要转换的数据类型
     */
    public <T> T getById(String collectionName, String mid, Class<T> clazz) {
        // 根据mid查出本次的修改  不能直接转clazz
        var obj = mongoTemplate.findById(new ObjectId(mid), JSONObject.class, collectionName);
        return JSONUtil.toBean(obj, clazz);
    }

    HistoryUtil getSelf() {
        if (self == null) {
            self = SpringUtil.getBean(HistoryUtil.class);
        }
        return self;
    }

    private String getTableName(Class clazz) {
        TableName tableName = (TableName) clazz.getAnnotation(TableName.class);
        HistoryLog historyLog = (HistoryLog) clazz.getAnnotation(HistoryLog.class);
        if (tableName == null && historyLog == null) {
            BizCodeEnum.CAN_NOT_INSERT_HISTORY.newException(clazz);
        }
        return Objects.isNull(historyLog) ? tableName.value() : historyLog.value();
    }
}
