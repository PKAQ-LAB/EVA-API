package io.nerv.web.sys.dict.cache.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.nerv.util.RedisUtil;
import io.nerv.web.sys.dict.cache.DictHelperProvider;
import io.nerv.web.sys.dict.cache.condition.RedisDictCacheCondition;
import io.nerv.web.sys.dict.service.DictService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 初始化字典信息
 * @author: S.PKAQ
 * @Datetime: 2019/3/15 13:18
 */
@Data
@Component
@Conditional(RedisDictCacheCondition.class)
public class DictRedisHelper implements DictHelperProvider {
    @Autowired
    private DictService dictService;

    @Autowired
    private RedisUtil redisUtil;

    private final static String DICT_CACHE_KEY="eva_dict:";

    final static Type type = new TypeReference<LinkedHashMap<String, String>>() {}.getType();
    /**
     * 初始化字典数据
     */
    @Override
    public void init() {
        var dictMap = dictService.initDictCache();
        dictMap.forEach((k, v) ->
            redisUtil.set(DICT_CACHE_KEY+k, JSON.toJSONString(v))
        );
    }

    /**
     * 根据传入的内容重新初始化字典
     * @param dictMap
     */
    @Override
    public void init(Map<String, LinkedHashMap<String, String>> dictMap) {
        redisUtil.set(DICT_CACHE_KEY, JSON.toJSONString(dictMap));
    }

    /**
     * 根据key查找值
     * @param code
     * @return
     */
    @Override
    public LinkedHashMap<String, String> get(String code){
        return JSON.parseObject(redisUtil.get(DICT_CACHE_KEY+code), type);
    }

    @Override
    public String get(String code, String key) {
        String value = null;
        Map<String, String> itemMap = JSON.parseObject(redisUtil.get(DICT_CACHE_KEY+code), type);
        if (null != itemMap){
            value = itemMap.get(key);
        }
        return value;
    }

    @Override
    public void remove(String code) {
        this.redisUtil.remove(DICT_CACHE_KEY+code);
    }

    @Override
    public void remove(String code, String key) {
        Map<String, String> itemMap = JSON.parseObject(this.redisUtil.get(DICT_CACHE_KEY+code), type);
        if (null != itemMap){
            itemMap.remove(key);
        }
    }

    @Override
    public void removeAll() {
       this.redisUtil.remove(DICT_CACHE_KEY+"*");
    }

    @Override
    public void update(String code, String key, String value) {
        Map<String, String> itemMap =  JSON.parseObject(this.redisUtil.get(DICT_CACHE_KEY+code), type);
        if (null != itemMap){
            itemMap.put(key, value);
            this.redisUtil.set(DICT_CACHE_KEY+code, JSON.toJSONString(itemMap));
        }
    }

    @Override
    public void update(String code, LinkedHashMap<String, String> item) {
        this.redisUtil.set(DICT_CACHE_KEY+code, JSON.toJSONString(item));
    }

    @Override
    public void add(String code, LinkedHashMap<String, String> item) {
        this.redisUtil.set(DICT_CACHE_KEY+code, JSON.toJSONString(item));
    }

    @Override
    public void add(String code, String key, String value) {
        this.update(code, key, value);
    }

    @Override
    public void reload() {
        this.removeAll();
        this.init();
    }

    @Override
    public void reload(Map<String, LinkedHashMap<String, String>> dictMap) {
        this.removeAll();
        this.init(dictMap);
    }
}
