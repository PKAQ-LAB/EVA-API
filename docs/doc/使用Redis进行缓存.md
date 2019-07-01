# 在项目中使用redis缓存
**Redis的优势**

性能极高 – Redis能读的速度是110000次/s,写的速度是81000次/s 。
丰富的数据类型 – Redis支持二进制案例的 Strings, Lists, Hashes, Sets 及 Ordered Sets 数据类型操作。
原子 – Redis的所有操作都是原子性的，意思就是要么成功执行要么失败完全不执行。单个操作是原子性的。多个操作也支持事务，即原子性，通过MULTI和EXEC指令包起来。
丰富的特性 – Redis还支持 publish/subscribe, 通知, key 过期等等特性

一、加入依赖
```groovy
    implementation "io.nerv:eva-cache:${version}"
```

二、缓存配置

```yaml
spring:
  redis:
    host: 192.168.10.166
    port: 6379
    password: 8i9o0p
    timeout: 10000
    database: 0
  cache:
    type: redis
    cache-names: sys
```    
 
三、启动类配置
```java
    @EnableCaching
``` 

四、使用缓存

配置后项目中可使用`redisTemplate`进行redis操作;
对于需要进行缓存的数据可以使用如下注解进行缓存操作
- @Cacheable	主要针对方法配置，能够根据方法的请求参数对其进行缓存 注解会先查询是否已经有缓存，有会使用缓存，没有则会执行方法并缓存。此处的value是必需的，它指定了你的缓存存放在哪块命名空间。
- @CacheEvict	主要针对方法配置，能够根据一定的条件对缓存进行清空 
- @CachePut	    常用于更新@CachePut注解的作用 主要针对方法配置，能够根据方法的请求参数对其结果进行缓存，和 @Cacheable 不同的是，它每次都会触发真实方法的调用 。简单来说就是用户更新缓存数据。但需要注意的是该注解的value 和 key 必须与要更新的缓存相同，也就是与@Cacheable 相同。示例：
            
注意： 
1. 此处的value是必需的，它指定了你的缓存存放在哪块命名空间。
2. 此处的key是使用的spEL表达式，参考上章。这里有一个小坑，如果你把methodName换成method运行会报错，观察它们的返回类型，原因在于methodName是String而methoh是Method。
3. 此处的User实体类一定要实现序列化public class User implements Serializable，否则会报java.io.NotSerializableException异常。


配置@CacheConfig#
当我们需要缓存的地方越来越多，你可以使用@CacheConfig(cacheNames = {"myCache"})注解来统一指定value的值，这时可省略value，如果你在你的方法依旧写上了value，那么依然以方法的value值为准。

< --- 下面是分割线 --->

---
# 史上最全的Spring Boot Cache使用与整合
## 一：Spring缓存抽象#
Spring从3.1开始定义了`org.springframework.cache.Cache和org.springframework.cache.CacheManager`接口来统一不同的缓存技术；并支持使用JCache（JSR-107）注解简化我们开发；

Cache接口为缓存的组件规范定义，包含缓存的各种操作集合；

Cache接口下Spring提供了各种xxxCache的实现；如RedisCache，EhCacheCache ,ConcurrentMapCache等；

每次调用需要缓存功能的方法时，Spring会检查检查指定参数的指定的目标方法是否已经被调用过；如果有就直接从缓存中获取方法调用后的结果，如果没有就调用方法并缓存结果后返回给用户。下次调用直接从缓存中获取。

使用Spring缓存抽象时我们需要关注以下两点；

1、确定方法需要被缓存以及他们的缓存策略

2、从缓存中读取之前缓存存储的数据

## 二：几个重要概念&缓存注解#
|   名称   |   解释   |
| ---- | ---- |
|   Cache   |   缓存接口，定义缓存操作。实现有：RedisCache、EhCacheCache、ConcurrentMapCache等   |
|    CacheManager  |   缓存管理器，管理各种缓存（cache）组件   |
|   @Cacheable   |  主要针对方法配置，能够根据方法的请求参数对其进行缓存    |
|   @CacheEvict   |  清空缓存   |
|   @CachePut   |  保证方法被调用，又希望结果被缓存。与@Cacheable区别在于是否每次都调用方法，常用于更新    |
|   @EnableCaching   |  开启基于注解的缓存    |
|   keyGenerator   |  缓存数据时key生成策略    |
|   serialize   |  缓存数据时value序列化策略    |
|   @CacheConfig   |  统一配置本类的缓存注解的属性    |


|   名称   |   解释   |
| ---- | ---- |
|   value   |   缓存的名称，在 spring 配置文件中定义，必须指定至少一个  例如：@Cacheable(value=”mycache”) 或者 @Cacheable(value={”cache1”,”cache2”} |
|   key  |   缓存的 key，可以为空，如果指定要按照 SpEL 表达式编写， 如果不指定，则缺省按照方法的所有参数进行组合 例如：@Cacheable(value=”testcache”,key=”#id”) |
|   condition   |  缓存的条件，可以为空，使用 SpEL 编写，返回 true 或者 false，只有为 true 才进行缓存/清除缓存 例如：@Cacheable(value=”testcache”,condition=”#userName.length()>2”)   |
|   unless   |  否定缓存。当条件结果为TRUE时，就不会缓存。   @Cacheable(value=”testcache”,unless=”#userName.length()>2”)|
|   allEntries   |  (@CacheEvict )	是否清空所有缓存内容，缺省为 false，如果指定为 true，则方法调用后将立即清空所有缓存 例如：@CachEvict(value=”testcache”,allEntries=true)   |
|   beforeInvocation   |  (@CacheEvict)	是否在方法执行前就清空，缺省为 false，如果指定为 true，则在方法还没有执行的时候就清空缓存，缺省情况下，如果方法执行抛出异常，则不会清空缓存 例如：@CachEvict(value=”testcache”，beforeInvocation=true)    |


## 三：SpEL上下文数据#
Spring Cache提供了一些供我们使用的SpEL上下文数据，下表直接摘自Spring官方文档：

|  名称	|  位置	|  描述	|  示例  |
| ---- | ---- | ---- | ---- |
| methodName	| root对象| 	当前被调用的方法名| 	#root.methodname| 
| method	| root对象| 	当前被调用的方法| 	#root.method.name| 
| target	| root对象| 	当前被调用的目标对象实例| 	#root.target| 
| targetClass	| root对象| 	当前被调用的目标对象的类| 	#root.targetClass| 
| args	| root对象| 	当前被调用的方法的参数列表| 	#root.args[0]| 
| caches	| root对象| 	当前方法调用使用的缓存列表| 	#root.caches[0].name| 
| Argument | Name| 	执行上下文| 	当前被调用的方法的参数，如findArtisan(Artisan artisan),可以通过#artsian.id获得参数	#artsian.id| 
| result	| 执行上下文| 	方法执行后的返回值（仅当方法执行后的判断有效，如 unless cacheEvict的beforeInvocation=false）	#result| 
注意：

1.当我们要使用root对象的属性作为key时我们也可以将“#root”省略，因为Spring默认使用的就是root对象的属性。 如@Cacheable(key = "targetClass + methodName +#p0")

2.使用方法参数时我们可以直接使用“#参数名”或者“#p参数index”。 如：
```java
@Cacheable(value="users", key="#id")
@Cacheable(value="users", key="#p0")
```
SpEL提供了多种运算符

| 类型	|运算符|
| ---- | ---- |
| 关系	|<，>，<=，>=，==，!=，lt，gt，le，ge，eq，ne|
| 算术	|+，- ，* ，/，%，^|
| 逻辑	|&&，||，!，and，or，not，between，instanceof|
| 条件	|?: (ternary)，?: (elvis)|
| 正则表达式	|matches|
| 其他类型	|?.，?[…]，![…]，^[…]，$[…]|