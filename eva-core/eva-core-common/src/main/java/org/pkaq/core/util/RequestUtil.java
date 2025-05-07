package org.pkaq.core.util;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
     * 获取请求模块id
     *
     * @param request
     * @return
     */
    public static String getModuleId(HttpServletRequest request) {
        String moduleId = request.getHeader(CommonConstant.MODULE_ID);
        return StrUtil.isBlank(moduleId) ? "" : moduleId;
    }

    /**
     * 获取请求模块code
     *
     * @param request
     * @return
     */
    public static String getModuleCode(HttpServletRequest request) {
        String mcdoe = request.getHeader(CommonConstant.MODULE_CODE);
        return StrUtil.isBlank(mcdoe) ? "" : mcdoe;
    }
}
