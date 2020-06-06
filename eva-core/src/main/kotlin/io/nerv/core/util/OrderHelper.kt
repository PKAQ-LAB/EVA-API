package io.nerv.core.util

import java.util.*

/**
 * 订单帮助类
 * @author: S.PKAQ
 * @Datetime: 2018/3/26 23:52
 */
object OrderHelper {
    private const val prefix = "O"

    /**
     * 生成订单号
     * @param userCode
     * @return
     */
    fun getOrderNumber(userCode: String?): String {
        val timestamp = System.currentTimeMillis()
        val sb = StringBuilder()
        sb.append(prefix)
        sb.append(timestamp)
        sb.append(userCode)
        sb.append(Random().nextInt())
        return sb.toString()
    }
}