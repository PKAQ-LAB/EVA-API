package io.nerv.web.sys.dict.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 字典缓存接口定义
 * @author: S.PKAQ
 * @Datetime: 2019/3/16 10:43
 */
public interface DictHelperProvider {
    /**
     * 初始化字典
     */
    void init();
    /**
     * 根据传入的内容重新初始化字典
     * @param dictMap
     */
    void init(Map<String, LinkedHashMap<String, String>> dictMap);
    /**
     * 根据code获取 字典项map
     * @param code
     * @return
     */
    LinkedHashMap<String, String> get(String code);

    /**
     * 根据code和字典项key获取value
     * @param code
     * @param key
     * @return
     */
    String get(String code, String key);

    /**
     * 删除指定字典
     * @param code
     * @return
     */
    void remove(String code);

    /**
     * 删除指定key对应的code值
     * @param code
     * @param key
     * @return
     */
    void remove(String code, String key);

    /**
     * 删除所有字典
     * @return
     */
    void removeAll();

    /**
     * 更新指定key对应的code值
     * @param key
     * @param code
     * @return
     */
    void update(String code, String key, String value);

    /**
     * 新增字典
     * @param code
     * @param item
     * @return
     */
    void add(String code, LinkedHashMap<String, String> item);

    /**
     * 新增一条字典项
     * @param code
     * @param key
     * @param value
     * @return
     */
    void add(String code, String key, String value);
    /**
     * 重新加载字典
     */
    void reload();
    /**
     * 重新加载所有字典
     * @param dictMap
     * @return
     */
    void reload(Map<String, LinkedHashMap<String, String>> dictMap);
}
