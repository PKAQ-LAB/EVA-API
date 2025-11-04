package org.pkaq.core.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pkaq.core.constant.CommonConstant;


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

        return StringUtils.isBlank(device) ? CommonConstant.UNKNOWN : device;
    }

    /**
     * 获取请求版本号
     *
     * @param request
     * @return
     */
    public static String getVersion(HttpServletRequest request) {
        String version = request.getHeader(CommonConstant.VERSION);

        return StringUtils.isBlank(version) ? CommonConstant.UNKNOWN : version;
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
     * 获取请求模块id
     *
     * @param request
     * @return
     */
    public static long getModuleId(HttpServletRequest request) {
        String moduleId = request.getHeader(CommonConstant.MODULE_ID);
        return StringUtils.isBlank(moduleId) ? 0 : Long.parseLong(moduleId);
    }

    /**
     * 获取请求模块code
     *
     * @param request
     * @return
     */
    public static String getModuleCode(HttpServletRequest request) {
        String mcdoe = request.getHeader(CommonConstant.MODULE_CODE);
        return StringUtils.isBlank(mcdoe) ? "" : mcdoe;
    }
}
