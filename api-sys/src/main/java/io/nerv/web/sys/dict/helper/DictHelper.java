package io.nerv.web.sys.dict.helper;

import io.nerv.web.sys.dict.service.DictService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 初始化字典信息
 * @author: S.PKAQ
 * @Datetime: 2019/3/15 13:18
 */
@Data
@Component
public class DictHelper implements DictHelperProvider{
    @Autowired
    private DictService dictService;

    private Map<String, Map<String, String>> dictMap;

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
    public void init(Map<String, Map<String, String>> dictMap) {
        this.dictMap = Collections.synchronizedMap(new HashMap<>(dictMap));
    }

    /**
     * 根据key查找值
     * @param code
     * @return
     */
    @Override
    public Map<String, String> get(String code){
        return this.dictMap.get(code);
    }

    @Override
    public String get(String code, String key) {
        String value = null;
        Map<String, String> itemMap =  this.dictMap.get(key);
        if (null != itemMap){
            value = itemMap.get(code);
        }
        return value;
    }

    @Override
    public void remove(String key) {
        this.dictMap.remove(key);
    }

    @Override
    public void remove(String code, String key) {
        Map<String, String> itemMap =  this.dictMap.get(key);
        if (null != itemMap){
            itemMap.remove(code);
        }
    }

    @Override
    public void removeAll() {
       this.dictMap.clear();
    }

    @Override
    public void update(String code, String key, String value) {
        Map<String, String> itemMap =  this.dictMap.get(key);
        if (null != itemMap){
            itemMap.put(key, value);
        }
    }

    @Override
    public void add(String code, Map<String, String> item) {
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
    public void reload(Map<String, Map<String, String>> dictMap) {
        this.dictMap.clear();
        this.init(dictMap);
    }
}
