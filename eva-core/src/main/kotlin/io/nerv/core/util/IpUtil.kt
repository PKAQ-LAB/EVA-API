package io.nerv.core.util

import javax.servlet.http.HttpServletRequest

/**
 * 获取IP
 */
object IpUtil {
    @kotlin.jvm.JvmStatic
    fun getIPAddress(request: HttpServletRequest): String? {
        var ip: String? = null

        //X-Forwarded-For：Squid 服务代理
        var ipAddresses = request.getHeader("X-Forwarded-For")
        if (ipAddresses == null || ipAddresses.length == 0 || "unknown".equals(ipAddresses, ignoreCase = true)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP")
        }
        if (ipAddresses == null || ipAddresses.length == 0 || "unknown".equals(ipAddresses, ignoreCase = true)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ipAddresses == null || ipAddresses.length == 0 || "unknown".equals(ipAddresses, ignoreCase = true)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ipAddresses == null || ipAddresses.length == 0 || "unknown".equals(ipAddresses, ignoreCase = true)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP")
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length != 0) {
            ip = ipAddresses.split(",".toRegex()).toTypedArray()[0]
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length == 0 || "unknown".equals(ipAddresses, ignoreCase = true)) {
            ip = request.remoteAddr
        }
        return ip
    }
}