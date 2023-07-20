package tech.yunyue.blacklist.cache;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import tech.yunyue.core.constant.CommonConstant;

import java.util.*;

/**
 * 黑名单信息缓存类
 */
@Data
@Component
public class BlackListCacheHelper {
    private Cache cache;

    @Autowired
    public BlackListCacheHelper(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(CommonConstant.CACHE_SYSDATA);
    }
    public List<String> getAll() {
        return cache.get(CommonConstant.BLACKDATA_KEY,List.class);
    }

    /**
     * 新增黑名单 重新保存
     */
    public void add(String target) {
        List<String> list = getAll();
        list.add(target);
        cachePut(list);
    }
    /**
     * 修改黑名单 重新保存
     */
    public void add(String oldTarget, String newTarget) {
        List<String> list = getAll();
        list.remove(oldTarget);
        list.add(newTarget);
        cachePut(list);
    }

    /**
     * 删除一个黑名单 重新保存
     */
    public void remove(String target) {
        List<String> list = getAll();
        list.remove(target);
        cachePut(list);
    }

    public void removeAll() {
        this.cache.clear();
    }

    /**
     * 写入缓存
     */
    public void cachePut(List<String> list) {
        if(CollectionUtil.isEmpty(list)){
            list = CollectionUtil.newArrayList();
        }
        this.cache.put(CommonConstant.BLACKDATA_KEY, list);
    }
}
