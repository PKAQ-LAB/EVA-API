package io.nerv.core.mvc.util

import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.metadata.OrderItem
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * 自定义分页模型
 * @author PKAQ
 */
open class Page<T> : IPage<T> {
    /**
     * 查询数据列表
     */
    var list: List<T> = emptyList()
        set

    /**
     * 总数
     */
    var total: Number = 0

    /**
     * 每页显示条数，默认 10
     */
    var size: Number = 10

    /**
     * 当前页
     */
    var current: Number = 1

    /**
     * 排序字段信息
     */
    var orders: MutableList<OrderItem> = ArrayList()

    /**
     * 自动优化 COUNT SQL
     */
    var optimizeCountSql = true

    /**
     * 是否进行 count 查询
     */
    var searchCount: Boolean = true

    /**
     * 是否命中count缓存
     */
    var hitCount = false

    constructor() {}
    constructor(current: Long, size: Long, isSearchCount: Boolean) : this(current, size, 0, isSearchCount) {}

    /**
     * 分页构造函数
     *
     * @param current 当前页
     * @param size    每页显示条数
     */
    @JvmOverloads
    constructor(current: Long, size: Long, total: Long = 0, isSearchCount: Boolean = true) {
        if (current > 1) {
            this.current = current
        }
        this.size = size
        this.total = total
        this.isSearchCount = isSearchCount
    }

    /**
     * 是否存在上一页
     *
     * @return true / false
     */
    fun hasPrevious(): Boolean {
        return current > 1
    }

    /**
     * 是否存在下一页
     *
     * @return true / false
     */
    operator fun hasNext(): Boolean {
        return current < this.pages
    }

    override fun getRecords(): List<T> {
        return null
    }

    override fun setRecords(data: List<T>): Page<T> {
        list = data
        return this
    }

    override fun getTotal(): Long {
        return total
    }

    override fun setTotal(total: Long): Page<T> {
        this.total = total
        return this
    }

    override fun getSize(): Long {
        return size
    }

    override fun setSize(size: Long): Page<T> {
        this.size = size
        return this
    }

    override fun getCurrent(): Long {
        return current
    }

    override fun setCurrent(current: Long): Page<T> {
        this.current = current
        return this
    }

    /**
     * 查找 order 中正序排序的字段数组
     *
     * @param filter 过滤器
     * @return 返回正序排列的字段数组
     */
    fun mapOrderToArray(filter: Predicate<OrderItem>): Array<String> {
        val columns: MutableList<String> = ArrayList(orders.size)
        orders.forEach(Consumer { i: OrderItem ->
            if (filter.test(i)) {
                columns.add(i.column)
            }
        })
        return columns.toTypedArray()
    }

    /**
     * 移除符合条件的条件
     *
     * @param filter 条件判断
     */
    fun removeOrder(filter: Predicate<OrderItem>) {
        for (i in orders.indices.reversed()) {
            if (filter.test(orders[i])) {
                orders.removeAt(i)
            }
        }
    }

    /**
     * 添加新的排序条件，构造条件可以使用工厂：[OrderItem.]
     *
     * @param items 条件
     * @return 返回分页参数本身
     */
    fun addOrder(vararg items: OrderItem?): Page<T> {
        orders.addAll(Arrays.asList(*items))
        return this
    }

    /**
     * 添加新的排序条件，构造条件可以使用工厂：[OrderItem.]
     *
     * @param items 条件
     * @return 返回分页参数本身
     */
    fun addOrder(items: List<OrderItem>?): Page<T> {
        orders.addAll(items!!)
        return this
    }

    override fun orders(): List<OrderItem> {
        return getOrders()
    }

    fun getOrders(): List<OrderItem> {
        return orders
    }

    fun setOrders(orders: MutableList<OrderItem>) {
        this.orders = orders
    }

    override fun optimizeCountSql(): Boolean {
        return optimizeCountSql
    }

    override fun isSearchCount(): Boolean {
        return if (total < 0) {
            false
        } else isSearchCount
    }

    fun setSearchCount(isSearchCount: Boolean): Page<T> {
        this.isSearchCount = isSearchCount
        return this
    }

    fun setOptimizeCountSql(optimizeCountSql: Boolean): Page<T> {
        this.optimizeCountSql = optimizeCountSql
        return this
    }

    override fun hitCount(hit: Boolean) {
        hitCount = hit
    }

    override fun isHitCount(): Boolean {
        return hitCount
    }

    companion object {
        const val serialVersionUID = 8545996863226528798L
    }
}