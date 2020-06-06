package io.nerv.core.util

import cn.hutool.core.util.StrUtil
import io.nerv.core.constant.CommonConstant
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

/**
 * 请求处理工具类
 */
@Component
class RequestUtil {
    /**
     * 获取请求设备类型
     * @param request
     * @return
     */
    fun getDeivce(request: HttpServletRequest): String {
        val device = request.getHeader(CommonConstant.DEVICE)
        return if (StrUtil.isBlank(device)) CommonConstant.UNKNOWN else device
    }

    /**
     * 获取请求版本号
     * @param request
     * @return
     */
    fun getVersion(request: HttpServletRequest): String {
        val version = request.getHeader(CommonConstant.VERSION)
        return if (StrUtil.isBlank(version)) CommonConstant.UNKNOWN else version
    }

    /**
     * 根据给定的字符串格式化返回设备类型和版本号
     * @param request
     * @param format
     * @return
     */
    fun formatDeivceAndVersion(request: HttpServletRequest, format: String?): String {
        return String.format(format!!, getDeivce(request), getVersion(request))
    }
}