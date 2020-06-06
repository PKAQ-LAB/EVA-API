package io.nerv.core.mvc.vo

/**
 * 通用分页对象
 * @param <T>
</T> */
class PageVo<T> {
    /**
     * 查询数据列表
     */
    val records: List<T> = emptyList()

    /**
     * 总页数
     */
    val total: Long = 0

    /**
     * 每页显示条数，默认 10
     */
    val size: Long = 10

    /**
     * 当前页
     */
    val current: Long = 1
}