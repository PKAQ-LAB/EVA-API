package io.nerv.web.sys.dict.cache.helper;

import io.nerv.core.cache.DefaultCacheCondition;
import io.nerv.web.sys.dict.cache.DictHelperProvider;
import io.nerv.web.sys.dict.service.DictService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 初始化字典信息
 * @author: S.PKAQ
 * @Datetime: 2019/3/15 13:18
 */
@Data
@Component
@Conditional(DefaultCacheCondition.class)
public class DictMapHelper implements DictHelperProvider {
    @Autowired
    private DictService dictService;

    private Map<String, LinkedHashMap<String, String>> dictMap;

    /**
     * 初始化字典数据
     */
    @Override
    public void init() {
        if (null == dictMap){
            dictMap = Collections.synchronizedMap(dictService.initDictCache());
        }
    }

    /**
     * 根据传入的内容重新初始化字典
     * @param dictMap
     */
    @Override
    public void init(Map<String, LinkedHashMap<String, String>> dictMap) {
        this.dictMap = Collections.synchronizedMap(new LinkedHashMap<>(dictMap));
    }

    @Override
    public Map<String, LinkedHashMap<String, String>> getAll() {
        return this.dictMap;
    }

    /**
     * 根据key查找值
     * @param code
     * @return
     */
    @Override
    public LinkedHashMap<String, String> get(String code){
        return this.dictMap.get(code);
    }

    @Override
    public String get(String code, String key) {
        String value = null;
        Map<String, String> itemMap =  this.dictMap.get(code);
        if (null != itemMap){
            value = itemMap.get(key);
        }
        return value;
    }

    @Override
    public void remove(String code) {
        this.dictMap.remove(code);
    }

    @Override
    public void remove(String code, String key) {
        Map<String, String> itemMap =  this.dictMap.get(code);
        if (null != itemMap){
            itemMap.remove(key);
        }
    }

    @Override
    public void removeAll() {
       this.dictMap.clear();
    }

    @Override
    public void update(String code, String key, String value) {
        Map<String, String> itemMap =  this.dictMap.get(code);
        if (null != itemMap){
            itemMap.put(key, value);
        }
    }

    @Override
    public void update(String code, LinkedHashMap<String, String> item) {
        this.dictMap.put(code, item);
    }

    @Override
    public void add(String code, LinkedHashMap<String, String> item) {
        this.dictMap.put(code, item);
    }

    @Override
    public void add(String code, String key, String value) {
        this.update(code, key, value);
    }

    @Override
    public void reload() {
        this.dictMap.clear();
        this.init();
    }

    @Override
    public void reload(Map<String, LinkedHashMap<String, String>> dictMap) {
        this.dictMap.clear();
        this.init(dictMap);
    }
}
