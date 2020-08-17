package io.nerv.core.cache.config

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CachingConfigurerSupport
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Method

@Configuration
open class CacheConfiguration : CachingConfigurerSupport() {
    val log = LoggerFactory.getLogger(this.javaClass)
    /*
     * 定义缓存数据 key 生成策略的bean 包名+类名+方法名+所有参数
     */
    override fun keyGenerator(): KeyGenerator {
        return KeyGenerator { o: Any, method: Method, objects: Array<Any> ->
            //格式化缓存key字符串
            val sb = StringBuilder()
            //追加类名
            sb.append(o.javaClass.name)
            //追加方法名
            sb.append(method.name)
            //遍历参数并且追加
            for (obj in objects) {
                sb.append(obj.toString())
            }
            log.debug("调用缓存Key : $sb")
            sb.toString()
        }
    }
}