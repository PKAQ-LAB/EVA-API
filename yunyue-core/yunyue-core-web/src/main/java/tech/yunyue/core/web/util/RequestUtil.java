package tech.yunyue.core.web.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import tech.yunyue.core.constant.CommonConstant;
import jakarta.servlet.http.HttpServletRequest;


/**
 * 请求处理工具类
 */
@Slf4j
public class RequestUtil {
    /**
     * 获取请求设备类型
     *
     * @param request
     * @return
     */
    public static String getDeivce(HttpServletRequest request) {
        String device = request.getHeader(CommonConstant.DEVICE);

        return StrUtil.isBlank(device) ? CommonConstant.UNKNOWN : device;
    }

    /**
     * 获取请求版本号
     *
     * @param request
     * @return
     */
    public static String getVersion(HttpServletRequest request) {
        String version = request.getHeader(CommonConstant.VERSION);

        return StrUtil.isBlank(version) ? CommonConstant.UNKNOWN : version;
    }

    /**
     * 根据给定的字符串格式化返回设备类型和版本号
     *
     * @param request
     * @param format
     * @return
     */
    public static String formatDeivceAndVersion(HttpServletRequest request, String format) {
        return String.format(format, getDeivce(request), getVersion(request));
    }

    /**
     * 获取请求ip
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;
        try {
             ip = request.getHeader("X-Real-IP");
            if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("x-forwarded-for");
            }
            if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StrUtil.isEmpty(ip) || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.error("IPUtils ERROR：",e);
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (!StrUtil.isEmpty(ip) && ip.length() > 15) {
            var index = ip.indexOf(",");
            if (index > 0) {
                ip = ip.substring(0, index);
            }
        }
        return ip;
    }
}
