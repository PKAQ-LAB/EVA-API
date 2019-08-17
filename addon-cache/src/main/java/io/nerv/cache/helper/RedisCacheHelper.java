package io.nerv.cache.helper;

import cn.hutool.cache.Cache;
import cn.hutool.cache.impl.CacheObj;
import cn.hutool.core.lang.func.Func0;
import io.nerv.cache.condition.RedisCacheCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * redis 缓存实现类
 */
@Component
@Conditional(RedisCacheCondition.class)
public class RedisCacheHelper implements Cache {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public long timeout() {
        return 0;
    }

    @Override
    public void put(Object key, Object object) {
        redisTemplate.opsForValue().set(key, object);
    }

    @Override
    public void put(Object key, Object object, long timeout) {
        redisTemplate.opsForValue().set(key, object, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public Object get(Object key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Object get(Object key, Func0 supplier) {
        try {
            supplier.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Object get(Object key, boolean isUpdateLastAccess) {
        if (isUpdateLastAccess){
            long timeout = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
            redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
        }
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public Iterator<CacheObj> cacheObjIterator() {
        return null;
    }

    @Override
    public int prune() {
        return 0;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public void remove(Object key) {
        this.redisTemplate.delete(key);
    }

    @Override
    public void clear() {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.redisTemplate.hasKey(key);
    }
}
