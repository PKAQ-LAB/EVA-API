package io.nerv.common.cache.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CacheConfiguration extends CachingConfigurerSupport {
    /*
     * 定义缓存数据 key 生成策略的bean 包名+类名+方法名+所有参数
     */
    public KeyGenerator keyGenerator() {
        return (o, method, objects) -> {
            //格式化缓存key字符串
            StringBuilder sb = new StringBuilder();
            //追加类名
            sb.append(o.getClass().getName());
            //追加方法名
            sb.append(method.getName());
            //遍历参数并且追加
            for (Object obj : objects) {
                sb.append(obj.toString());
            }
            log.debug("调用缓存Key : " + sb.toString());
            return sb.toString();
        };
    }
}