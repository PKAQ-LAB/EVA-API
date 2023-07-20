package tech.yunyue.core.log.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ObjectOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ChangeLogUtil {
    @Autowired
    MongoTemplate template;
    public static MongoTemplate mongoTemplate;

    @PostConstruct
    public void init(){
        mongoTemplate = template;
    }

   public static List getModifyRecords(String collectionName, String id){
       var list = mongoTemplate.find(new Query(Criteria.where("ID").is(id)), Document.class, collectionName);
       var tsList = new ArrayList<>(list.size());
       list.forEach(e-> tsList.add(transformDucument(e)));
       return tsList;
    }

    public static JSONObject getChangeById(String collectionName, String mid){
        JSONObject json = new JSONObject();
        ObjectId objectId = new ObjectId(mid);
        // 根据mid查出本次的修改
        Document newObj = transformDucument(mongoTemplate.findById(objectId, Document.class, collectionName));
        json.set("new", newObj);

        // 查出本次之前的所有修改
        var match = Aggregation.match(Criteria.where("ID").is(newObj.getString("id")).and("_id").lt(objectId));
        var merge = ObjectOperators.MergeObjects.merge(Aggregation.ROOT);
        var group = Aggregation.group("$ID").and("merge",merge);
        var lastRecord = mongoTemplate.aggregate(Aggregation.newAggregation(match,group), collectionName, Document.class);
        json.set("old", formatDucument(lastRecord));
        return json;
    }

    // 把数据从result中取出来并把key转换成驼峰命名
    private static Document formatDucument(AggregationResults<Document> results){
        Document document = (Document) results.getMappedResults().stream().findFirst().orElse(new Document()).get("merge");
        return transformDucument(document);
    }

    /**
     * 把Document中的key转换成驼峰命名
     */
    private static Document transformDucument(Document document){
       Document newData = new Document();
       if (Objects.isNull(document) || document.isEmpty()) return newData;

       newData.put("mid",document.get("_id").toString());
       document.remove("_id");
       document.keySet().stream().forEach(k -> newData.put(StrUtil.toCamelCase(k.toLowerCase()), document.get(k)));
       return newData;
    }
}
