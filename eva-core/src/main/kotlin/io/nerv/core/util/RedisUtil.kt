package io.nerv.core.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations.TypedTuple
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author PKAQ
 * redis 操作工具类
 */
@Component
class RedisUtil {
    @Autowired
    private val jsonUtil: JsonUtil? = null

    @Autowired
    private val redisTemplate: RedisTemplate<Any, Any>? = null

    /**
     * 设置 String 类型 key-value
     * @param key
     * @param value
     */
    operator fun set(key: String, value: String) {
        redisTemplate!!.opsForValue()[key] = value
    }

    /**
     * 获取 String 类型 key-value
     * @param key
     * @return
     */
    operator fun get(key: String?): String {
        return redisTemplate!!.opsForValue()[key] as String
    }

    /**
     * @param key
     * @return
     */
    fun getObjectValue(key: String?): Any {
        return redisTemplate!!.opsForValue()[key]
    }

    /**
     * 模糊查询
     * @param key
     * @return
     */
    fun keys(key: String): Set<Any> {
        return redisTemplate!!.keys(key)
    }

    /**
     * 模糊查询
     * @param key
     * @return
     */
    fun getAll(key: String): Map<String, *> {
        val keys = redisTemplate!!.keys("$key*")
        val map: MutableMap<String, Any> = LinkedHashMap(keys.size)
        keys.stream().forEach { item: Any -> map[item.toString() + ""] = jsonUtil!!.parseObject<Map<*, *>>(this[item.toString() + ""], MutableMap::class.java)!! }
        return map
    }

    /**
     * 模糊查询
     * @param key
     * @return
     */
    fun getPureAll(key: String): Map<String, *> {
        val keys = redisTemplate!!.keys("$key*")
        val map: MutableMap<String, Any?> = LinkedHashMap(keys.size)
        val subIndex = key.length + 2
        keys.stream().forEach { item: Any ->
            var k = item.toString() + ""
            k = k.substring(subIndex)
            val value = getObjectValue(item.toString() + "")
            if (null != value && value is String) {
                map[k] = jsonUtil!!.parseObject<Map<*, *>>(value.toString(), MutableMap::class.java)
            } else {
                map[k] = value
            }
        }
        return map
    }

    /**
     * 设置 String 类型 key-value 并添加过期时间 (毫秒单位)
     * @param key
     * @param value
     * @param time 过期时间,毫秒单位
     */
    fun setForTimeMS(key: String, value: String, time: Long) {
        redisTemplate!!.opsForValue()[key, value, time] = TimeUnit.MILLISECONDS
    }

    /**
     * 设置 String 类型 key-value 并添加过期时间 (分钟单位)
     * @param key
     * @param value
     * @param time 过期时间,分钟单位
     */
    fun setForTimeMIN(key: String, value: String, time: Long) {
        redisTemplate!!.opsForValue()[key, value, time] = TimeUnit.MINUTES
    }

    /**
     * 设置 String 类型 key-value 并添加过期时间 (分钟单位)
     * @param key
     * @param value
     * @param time 过期时间,分钟单位
     */
    fun setForTimeCustom(key: String, value: String, time: Long, type: TimeUnit?) {
        redisTemplate!!.opsForValue()[key, value, time] = type
    }

    /**
     * 如果 key 存在则覆盖,并返回旧值.
     * 如果不存在,返回null 并添加
     * @param key
     * @param value
     * @return
     */
    fun getAndSet(key: String, value: String): String {
        return redisTemplate!!.opsForValue().getAndSet(key, value) as String
    }

    /**
     * 批量添加 key-value (重复的键会覆盖)
     * @param keyAndValue
     */
    fun batchSet(keyAndValue: Map<String, String>?) {
        redisTemplate!!.opsForValue().multiSet(keyAndValue)
    }

    /**
     * 批量添加 key-value 只有在键不存在时,才添加
     * map 中只要有一个key存在,则全部不添加
     * @param keyAndValue
     */
    fun batchSetIfAbsent(keyAndValue: Map<String, String>?) {
        redisTemplate!!.opsForValue().multiSetIfAbsent(keyAndValue)
    }

    /**
     * 对一个 key-value 的值进行加减操作,
     * 如果该 key 不存在 将创建一个key 并赋值该 number
     * 如果 key 存在,但 value 不是长整型 ,将报错
     * @param key
     * @param number
     */
    fun increment(key: String, number: Long): Long {
        return redisTemplate!!.opsForValue().increment(key, number)
    }

    /**
     * 对一个 key-value 的值进行加减操作,
     * 如果该 key 不存在 将创建一个key 并赋值该 number
     * 如果 key 存在,但 value 不是 纯数字 ,将报错
     * @param key
     * @param number
     */
    fun increment(key: String, number: Double): Double {
        return redisTemplate!!.opsForValue().increment(key, number)
    }

    /**
     * 给一个指定的 key 值附加过期时间
     * @param key
     * @param time
     * @param type
     * @return
     */
    fun expire(key: String, time: Long, type: TimeUnit?): Boolean {
        return redisTemplate!!.boundValueOps(key).expire(time, type)
    }

    /**
     * 移除指定key 的过期时间
     * @param key
     * @return
     */
    fun persist(key: String): Boolean {
        return redisTemplate!!.boundValueOps(key).persist()
    }

    /**
     * 获取指定key 的过期时间
     * @param key
     * @return
     */
    fun getExpire(key: String): Long {
        return redisTemplate!!.boundValueOps(key).expire
    }

    /**
     * 修改 key
     * @param key
     */
    fun rename(key: String, newKey: String) {
        redisTemplate!!.boundValueOps(key).rename(newKey)
    }

    /**
     * 删除 key-value
     * @param key
     * @return
     */
    fun delete(key: String): Boolean {
        return redisTemplate!!.delete(key)
    }
    //hash操作
    /**
     * 添加 Hash 键值对
     * @param key
     * @param hashKey
     * @param value
     */
    fun put(key: String, hashKey: String, value: String) {
        redisTemplate!!.opsForHash<Any, Any>().put(key, hashKey, value)
    }

    /**
     * 批量添加 hash 的 键值对
     * 有则覆盖,没有则添加
     * @param key
     * @param map
     */
    fun putAll(key: String, map: Map<String, String>?) {
        redisTemplate!!.opsForHash<Any, Any>().putAll(key, map)
    }

    /**
     * 添加 hash 键值对. 不存在的时候才添加
     * @param key
     * @param hashKey
     * @param value
     * @return
     */
    fun putIfAbsent(key: String, hashKey: String, value: String): Boolean {
        return redisTemplate!!.opsForHash<Any, Any>().putIfAbsent(key, hashKey, value)
    }

    /**
     * 删除指定 hash 的 HashKey
     * @param key
     * @param hashKeys
     * @return 删除成功的 数量
     */
    fun delete(key: String, vararg hashKeys: String?): Long {
        return redisTemplate!!.opsForHash<Any, Any>().delete(key, *hashKeys)
    }

    /**
     * 给指定 hash 的 hashkey 做增减操作
     * @param key
     * @param hashKey
     * @param number
     * @return
     */
    fun increment(key: String, hashKey: String, number: Long): Long {
        return redisTemplate!!.opsForHash<Any, Any>().increment(key, hashKey, number)
    }

    /**
     * 给指定 hash 的 hashkey 做增减操作
     * @param key
     * @param hashKey
     * @param number
     * @return
     */
    fun increment(key: String, hashKey: String, number: Double?): Double {
        return redisTemplate!!.opsForHash<Any, Any>().increment(key, hashKey, number!!)
    }

    /**
     * 获取指定 key 下的 hashkey
     * @param key
     * @param hashKey
     * @return
     */
    fun getHashKey(key: String, hashKey: String?): Any {
        return redisTemplate!!.opsForHash<Any, Any>()[key, hashKey]
    }

    /**
     * 获取 key 下的 所有  hashkey 和 value
     * @param key
     * @return
     */
    fun getHashEntries(key: String): Map<Any, Any> {
        return redisTemplate!!.opsForHash<Any, Any>().entries(key)
    }

    /**
     * 验证指定 key 下 有没有指定的 hashkey
     * @param key
     * @param hashKey
     * @return
     */
    fun hashKey(key: String, hashKey: String?): Boolean {
        return redisTemplate!!.opsForHash<Any, Any>().hasKey(key, hashKey)
    }

    /**
     * 获取 key 下的 所有 hashkey 字段名
     * @param key
     * @return
     */
    fun hashKeys(key: String): Set<Any> {
        return redisTemplate!!.opsForHash<Any, Any>().keys(key)
    }

    /**
     * 获取指定 hash 下面的 键值对 数量
     * @param key
     * @return
     */
    fun hashSize(key: String): Long {
        return redisTemplate!!.opsForHash<Any, Any>().size(key)
    }
    //List 操作
    /**
     * 指定 list 从左入栈
     * @param key
     * @return 当前队列的长度
     */
    fun leftPush(key: String, value: Any): Long {
        return redisTemplate!!.opsForList().leftPush(key, value)
    }

    /**
     * 指定 list 从左出栈
     * 如果列表没有元素,会堵塞到列表一直有元素或者超时为止
     * @param key
     * @return 出栈的值
     */
    fun leftPop(key: String): Any {
        return redisTemplate!!.opsForList().leftPop(key)
    }

    /**
     * 从左边依次入栈
     * 导入顺序按照 Collection 顺序
     * 如: a b c => c b a
     * @param key
     * @param values
     * @return
     */
    fun leftPushAll(key: String?, values: Collection<Any?>?): Long {
        return redisTemplate!!.opsForList().leftPushAll(key, values)
    }

    /**
     * 指定 list 从右入栈
     * @param key
     * @return 当前队列的长度
     */
    fun rightPush(key: String, value: Any): Long {
        return redisTemplate!!.opsForList().rightPush(key, value)
    }

    /**
     * 指定 list 从右出栈
     * 如果列表没有元素,会堵塞到列表一直有元素或者超时为止
     * @param key
     * @return 出栈的值
     */
    fun rightPop(key: String): Any {
        return redisTemplate!!.opsForList().rightPop(key)
    }

    /**
     * 从右边依次入栈
     * 导入顺序按照 Collection 顺序
     * 如: a b c => a b c
     * @param key
     * @param values
     * @return
     */
    fun rightPushAll(key: String?, values: Collection<Any?>?): Long {
        return redisTemplate!!.opsForList().rightPushAll(key, values)
    }

    /**
     * 根据下标获取值
     * @param key
     * @param index
     * @return
     */
    fun popIndex(key: String, index: Long): Any {
        return redisTemplate!!.opsForList().index(key, index)
    }

    /**
     * 获取列表指定长度
     * @param key
     * @param index
     * @return
     */
    fun listSize(key: String, index: Long): Long {
        return redisTemplate!!.opsForList().size(key)
    }

    /**
     * 获取列表 指定范围内的所有值
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun listRange(key: String, start: Long, end: Long): List<Any> {
        return redisTemplate!!.opsForList().range(key, start, end)
    }

    /**
     * 删除 key 中 值为 value 的 count 个数.
     * @param key
     * @param count
     * @param value
     * @return 成功删除的个数
     */
    fun listRemove(key: String, count: Long, value: Any?): Long {
        return redisTemplate!!.opsForList().remove(key, count, value)
    }

    /**
     * 删除 列表 [start,end] 以外的所有元素
     * @param key
     * @param start
     * @param end
     */
    fun listTrim(key: String, start: Long, end: Long) {
        redisTemplate!!.opsForList().trim(key, start, end)
    }

    /**
     * 将 key 右出栈,并左入栈到 key2
     *
     * @param key 右出栈的列表
     * @param key2 左入栈的列表
     * @return 操作的值
     */
    fun rightPopAndLeftPush(key: String, key2: String): Any {
        return redisTemplate!!.opsForList().rightPopAndLeftPush(key, key2)
    }
    //set 操作  无序不重复集合
    /**
     * 添加 set 元素
     * @param key
     * @param values
     * @return
     */
    fun add(key: String, vararg values: String?): Long {
        return redisTemplate!!.opsForSet().add(key, *values)
    }

    /**
     * 获取两个集合的差集
     * @param key
     * @param otherkey
     * @return
     */
    fun difference(key: String, otherkey: String): Set<Any> {
        return redisTemplate!!.opsForSet().difference(key, otherkey)
    }

    /**
     * 获取 key 和 集合  collections 中的 key 集合的差集
     * @param key
     * @return
     */
    fun difference(key: String?, otherKeys: Collection<Any?>?): Set<Any> {
        return redisTemplate!!.opsForSet().difference(key, otherKeys)
    }

    /**
     * 将  key 与 otherkey 的差集 ,添加到新的 newKey 集合中
     * @param key
     * @param otherkey
     * @param newKey
     * @return 返回差集的数量
     */
    fun differenceAndStore(key: String, otherkey: String, newKey: String): Long {
        return redisTemplate!!.opsForSet().differenceAndStore(key, otherkey, newKey)
    }

    /**
     * 将 key 和 集合  collections 中的 key 集合的差集 添加到  newkey 集合中
     * @param key
     * @param otherKeys
     * @param newKey
     * @return 返回差集的数量
     */
    fun differenceAndStore(key: String?, otherKeys: Collection<Any?>?, newKey: String?): Long {
        return redisTemplate!!.opsForSet().differenceAndStore(newKey, otherKeys, newKey)
    }

    /**
     * 删除一个或多个集合中的指定值
     * @param key
     * @param values
     * @return 成功删除数量
     */
    fun remove(key: String, vararg values: Any?): Long {
        return redisTemplate!!.opsForSet().remove(key, *values)
    }

    /**
     * 根据key通配符批量删除 如 key:*
     * @param prex
     */
    fun remove(prex: String) {
        val keys = redisTemplate!!.keys(prex)
        if (null != keys && !keys.isEmpty()) {
            redisTemplate.delete(keys)
        }
    }

    /**
     * 随机移除一个元素,并返回出来
     * @param key
     * @return
     */
    fun randomSetPop(key: String): Any {
        return redisTemplate!!.opsForSet().pop(key)
    }

    /**
     * 随机获取一个元素
     * @param key
     * @return
     */
    fun randomSet(key: String): Any {
        return redisTemplate!!.opsForSet().randomMember(key)
    }

    /**
     * 随机获取指定数量的元素,同一个元素可能会选中两次
     * @param key
     * @param count
     * @return
     */
    fun randomSet(key: String, count: Long): List<Any> {
        return redisTemplate!!.opsForSet().randomMembers(key, count)
    }

    /**
     * 随机获取指定数量的元素,去重(同一个元素只能选择两一次)
     * @param key
     * @param count
     * @return
     */
    fun randomSetDistinct(key: String, count: Long): Set<Any> {
        return redisTemplate!!.opsForSet().distinctRandomMembers(key, count)
    }

    /**
     * 将 key 中的 value 转入到 destKey 中
     * @param key
     * @param value
     * @param destKey
     * @return 返回成功与否
     */
    fun moveSet(key: String, value: Any, destKey: String): Boolean {
        return redisTemplate!!.opsForSet().move(key, value, destKey)
    }

    /**
     * 无序集合的大小
     * @param key
     * @return
     */
    fun setSize(key: String): Long {
        return redisTemplate!!.opsForSet().size(key)
    }

    /**
     * 判断 set 集合中 是否有 value
     * @param key
     * @param value
     * @return
     */
    fun isMember(key: String, value: Any?): Boolean {
        return redisTemplate!!.opsForSet().isMember(key, value)
    }

    /**
     * 返回 key 和 othere 的并集
     * @param key
     * @param otherKey
     * @return
     */
    fun unionSet(key: String, otherKey: String): Set<Any> {
        return redisTemplate!!.opsForSet().union(key, otherKey)
    }

    /**
     * 返回 key 和 otherKeys 的并集
     * @param key
     * @param otherKeys key 的集合
     * @return
     */
    fun unionSet(key: String?, otherKeys: Collection<Any?>?): Set<Any> {
        return redisTemplate!!.opsForSet().union(key, otherKeys)
    }

    /**
     * 将 key 与 otherKey 的并集,保存到 destKey 中
     * @param key
     * @param otherKey
     * @param destKey
     * @return destKey 数量
     */
    fun unionAndStoreSet(key: String, otherKey: String, destKey: String): Long {
        return redisTemplate!!.opsForSet().unionAndStore(key, otherKey, destKey)
    }

    /**
     * 将 key 与 otherKey 的并集,保存到 destKey 中
     * @param key
     * @param otherKeys
     * @param destKey
     * @return destKey 数量
     */
    fun unionAndStoreSet(key: String?, otherKeys: Collection<Any?>?, destKey: String?): Long {
        return redisTemplate!!.opsForSet().unionAndStore(key, otherKeys, destKey)
    }

    /**
     * 返回集合中所有元素
     * @param key
     * @return
     */
    fun members(key: String): Set<Any> {
        return redisTemplate!!.opsForSet().members(key)
    }
    //Zset 根据 socre 排序   不重复 每个元素附加一个 socre  double类型的属性(double 可以重复)
    /**
     * 添加 ZSet 元素
     * @param key
     * @param value
     * @param score
     */
    fun add(key: String, value: Any, score: Double): Boolean {
        return redisTemplate!!.opsForZSet().add(key, value, score)
    }

    /**
     * 批量添加 Zset <br></br>
     * Set<TypedTuple></TypedTuple><Object>> tuples = new HashSet<>();<br></br>
     * TypedTuple<Object> objectTypedTuple1 = new DefaultTypedTuple<Object>("zset-5",9.6);<br></br>
     * tuples.add(objectTypedTuple1);
     * @param key
     * @param tuples
     * @return
    </Object></Object></Object> */
    fun batchAddZset(key: String, tuples: Set<TypedTuple<Any>?>?): Long {
        return redisTemplate!!.opsForZSet().add(key, tuples)
    }

    /**
     * Zset 删除一个或多个元素
     * @param key
     * @param values
     * @return
     */
    fun removeZset(key: String, vararg values: String?): Long {
        return redisTemplate!!.opsForZSet().remove(key, *values)
    }

    /**
     * 对指定的 zset 的 value 值 , socre 属性做增减操作
     * @param key
     * @param value
     * @param score
     * @return
     */
    fun incrementScore(key: String, value: Any, score: Double): Double {
        return redisTemplate!!.opsForZSet().incrementScore(key, value, score)
    }

    /**
     * 获取 key 中指定 value 的排名(从0开始,从小到大排序)
     * @param key
     * @param value
     * @return
     */
    fun rank(key: String, value: Any?): Long {
        return redisTemplate!!.opsForZSet().rank(key, value)
    }

    /**
     * 获取 key 中指定 value 的排名(从0开始,从大到小排序)
     * @param key
     * @param value
     * @return
     */
    fun reverseRank(key: String, value: Any?): Long {
        return redisTemplate!!.opsForZSet().reverseRank(key, value)
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从小到大,带上分数)
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun rangeWithScores(key: String, start: Long, end: Long): Set<TypedTuple<Any>> {
        return redisTemplate!!.opsForZSet().rangeWithScores(key, start, end)
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从小到大,只有列名)
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun range(key: String, start: Long, end: Long): Set<Any> {
        return redisTemplate!!.opsForZSet().range(key, start, end)
    }

    /**
     * 获取分数范围内的 [min,max] 的排序结果集合 (从小到大,只有列名)
     * @param key
     * @param min
     * @param max
     * @return
     */
    fun rangeByScore(key: String, min: Double, max: Double): Set<Any> {
        return redisTemplate!!.opsForZSet().rangeByScore(key, min, max)
    }

    /**
     * 获取分数范围内的 [min,max] 的排序结果集合 (从小到大,集合带分数)
     * @param key
     * @param min
     * @param max
     * @return
     */
    fun rangeByScoreWithScores(key: String, min: Double, max: Double): Set<TypedTuple<Any>> {
        return redisTemplate!!.opsForZSet().rangeByScoreWithScores(key, min, max)
    }

    /**
     * 返回 分数范围内 指定 count 数量的元素集合, 并且从 offset 下标开始(从小到大,不带分数的集合)
     * @param key
     * @param min
     * @param max
     * @param offset 从指定下标开始
     * @param count 输出指定元素数量
     * @return
     */
    fun rangeByScore(key: String, min: Double, max: Double, offset: Long, count: Long): Set<Any> {
        return redisTemplate!!.opsForZSet().rangeByScore(key, min, max, offset, count)
    }

    /**
     * 返回 分数范围内 指定 count 数量的元素集合, 并且从 offset 下标开始(从小到大,带分数的集合)
     * @param key
     * @param min
     * @param max
     * @param offset 从指定下标开始
     * @param count 输出指定元素数量
     * @return
     */
    fun rangeByScoreWithScores(key: String, min: Double, max: Double, offset: Long, count: Long): Set<TypedTuple<Any>> {
        return redisTemplate!!.opsForZSet().rangeByScoreWithScores(key, min, max, offset, count)
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从大到小,只有列名)
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun reverseRange(key: String, start: Long, end: Long): Set<Any> {
        return redisTemplate!!.opsForZSet().reverseRange(key, start, end)
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从大到小,带上分数)
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun reverseRangeWithScores(key: String, start: Long, end: Long): Set<TypedTuple<Any>> {
        return redisTemplate!!.opsForZSet().reverseRangeWithScores(key, start, end)
    }

    /**
     * 获取分数范围内的 [min,max] 的排序结果集合 (从大到小,集合不带分数)
     * @param key
     * @param min
     * @param max
     * @return
     */
    fun reverseRangeByScore(key: String, min: Double, max: Double): Set<Any> {
        return redisTemplate!!.opsForZSet().reverseRangeByScore(key, min, max)
    }

    /**
     * 获取分数范围内的 [min,max] 的排序结果集合 (从大到小,集合带分数)
     * @param key
     * @param min
     * @param max
     * @return
     */
    fun reverseRangeByScoreWithScores(key: String, min: Double, max: Double): Set<TypedTuple<Any>> {
        return redisTemplate!!.opsForZSet().reverseRangeByScoreWithScores(key, min, max)
    }

    /**
     * 返回 分数范围内 指定 count 数量的元素集合, 并且从 offset 下标开始(从大到小,不带分数的集合)
     * @param key
     * @param min
     * @param max
     * @param offset 从指定下标开始
     * @param count 输出指定元素数量
     * @return
     */
    fun reverseRangeByScore(key: String, min: Double, max: Double, offset: Long, count: Long): Set<Any> {
        return redisTemplate!!.opsForZSet().reverseRangeByScore(key, min, max, offset, count)
    }

    /**
     * 返回 分数范围内 指定 count 数量的元素集合, 并且从 offset 下标开始(从大到小,带分数的集合)
     * @param key
     * @param min
     * @param max
     * @param offset 从指定下标开始
     * @param count 输出指定元素数量
     * @return
     */
    fun reverseRangeByScoreWithScores(key: String, min: Double, max: Double, offset: Long, count: Long): Set<TypedTuple<Any>> {
        return redisTemplate!!.opsForZSet().reverseRangeByScoreWithScores(key, min, max, offset, count)
    }

    /**
     * 返回指定分数区间 [min,max] 的元素个数
     * @param key
     * @param min
     * @param max
     * @return
     */
    fun countZSet(key: String, min: Double, max: Double): Long {
        return redisTemplate!!.opsForZSet().count(key, min, max)
    }

    /**
     * 返回 zset 集合数量
     * @param key
     * @return
     */
    fun sizeZset(key: String): Long {
        return redisTemplate!!.opsForZSet().size(key)
    }

    /**
     * 获取指定成员的 score 值
     * @param key
     * @param value
     * @return
     */
    fun score(key: String, value: Any?): Double {
        return redisTemplate!!.opsForZSet().score(key, value)
    }

    /**
     * 删除指定索引位置的成员,其中成员分数按( 从小到大 )
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun removeRange(key: String, start: Long, end: Long): Long {
        return redisTemplate!!.opsForZSet().removeRange(key, start, end)
    }

    /**
     * 删除指定 分数范围 内的成员 [main,max],其中成员分数按( 从小到大 )
     * @param key
     * @param min
     * @param max
     * @return
     */
    fun removeRangeByScore(key: String, min: Double, max: Double): Long {
        return redisTemplate!!.opsForZSet().removeRangeByScore(key, min, max)
    }

    /**
     * key 和 other 两个集合的并集,保存在 destKey 集合中, 列名相同的 score 相加
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    fun unionAndStoreZset(key: String, otherKey: String, destKey: String): Long {
        return redisTemplate!!.opsForZSet().unionAndStore(key, otherKey, destKey)
    }

    /**
     * key 和 otherKeys 多个集合的并集,保存在 destKey 集合中, 列名相同的 score 相加
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    fun unionAndStoreZset(key: String?, otherKeys: Collection<String?>?, destKey: String?): Long {
        return redisTemplate!!.opsForZSet().unionAndStore(key, otherKeys, destKey)
    }

    /**
     * key 和 otherKey 两个集合的交集,保存在 destKey 集合中
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    fun intersectAndStore(key: String, otherKey: String, destKey: String): Long {
        return redisTemplate!!.opsForZSet().intersectAndStore(key, otherKey, destKey)
    }

    /**
     * key 和 otherKeys 多个集合的交集,保存在 destKey 集合中
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    fun intersectAndStore(key: String?, otherKeys: Collection<String?>?, destKey: String?): Long {
        return redisTemplate!!.opsForZSet().intersectAndStore(key, otherKeys, destKey)
    }
}